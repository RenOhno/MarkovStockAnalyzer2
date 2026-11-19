"""Walk-forward backtest of the Markov predictor against baselines.

Research spec:
- Expanding window (walk-forward): at each step the transition matrix is
  estimated only from the transitions observed so far, and the next
  state's distribution is the current state's row of that matrix
- At least DEFAULT_MIN_TRAIN transitions of training before the first
  prediction ("minimum training count 30" in the research spec)
- The predicted label is the argmax of the predicted distribution
  (ties resolved in state order U, F, D)
- If the current state's row is unestimated (n_i = 0), the Markov model
  makes no prediction for that step (counted as skipped); the row is
  never filled with a uniform distribution
- Baselines, evaluated on every step:
  - Majority: the predicted distribution is the empirical distribution
    of the training states, the label its argmax
  - Persistence: the predicted distribution is the one-hot distribution
    of the current state, the label the current state itself
- Metrics: Accuracy, per-state and macro Precision / Recall, confusion
  matrix (rows = actual, columns = predicted), Brier score, Log Loss
  (probabilities clipped at LOG_LOSS_EPS so one-hot baselines stay finite)
"""

from __future__ import annotations

import math

from app import markov

DEFAULT_MIN_TRAIN = 30
LOG_LOSS_EPS = 1e-15

_INDEX = {s: i for i, s in enumerate(markov.STATES)}
_MODEL_NAMES = ("markov", "majority", "persistence")


def _argmax(dist: list[float]) -> int:
    return max(range(3), key=lambda j: dist[j])


def _metrics(
    samples: list[tuple[list[float], int]], skipped: int
) -> dict[str, object]:
    """Evaluation metrics for one model's (distribution, actual) samples."""
    confusion = [[0] * 3 for _ in range(3)]
    correct = 0
    brier_sum = 0.0
    log_loss_sum = 0.0
    for dist, actual in samples:
        predicted = _argmax(dist)
        confusion[actual][predicted] += 1
        if predicted == actual:
            correct += 1
        brier_sum += sum(
            (p - (1.0 if j == actual else 0.0)) ** 2 for j, p in enumerate(dist)
        )
        log_loss_sum += -math.log(max(dist[actual], LOG_LOSS_EPS))

    n = len(samples)
    if n == 0:
        return {
            "predictions": 0,
            "skipped": skipped,
            "accuracy": None,
            "precision": {s: 0.0 for s in markov.STATES},
            "recall": {s: 0.0 for s in markov.STATES},
            "precision_macro": None,
            "recall_macro": None,
            "confusion_matrix": confusion,
            "brier_score": None,
            "log_loss": None,
        }

    precision: dict[str, float] = {}
    recall: dict[str, float] = {}
    for c, state in enumerate(markov.STATES):
        predicted_as_c = sum(confusion[row][c] for row in range(3))
        actual_c = sum(confusion[c])
        precision[state] = (
            confusion[c][c] / predicted_as_c if predicted_as_c > 0 else 0.0
        )
        recall[state] = confusion[c][c] / actual_c if actual_c > 0 else 0.0

    return {
        "predictions": n,
        "skipped": skipped,
        "accuracy": correct / n,
        "precision": precision,
        "recall": recall,
        "precision_macro": sum(precision.values()) / 3,
        "recall_macro": sum(recall.values()) / 3,
        "confusion_matrix": confusion,
        "brier_score": brier_sum / n,
        "log_loss": log_loss_sum / n,
    }


def run_backtest(
    states: list[str], min_train: int = DEFAULT_MIN_TRAIN
) -> dict[str, object]:
    """Walk-forward backtest over a state sequence.

    Step k (min_train <= k < len(states) - 1) trains on the first k
    transitions (states[0..k]) and predicts states[k + 1] from states[k].
    """
    samples: dict[str, list[tuple[list[float], int]]] = {
        name: [] for name in _MODEL_NAMES
    }
    markov_skipped = 0

    for k in range(min_train, len(states) - 1):
        train_states = states[: k + 1]
        matrix = markov.estimate_transition_matrix(
            markov.transition_counts(train_states)
        )
        current = states[k]
        actual = _INDEX[states[k + 1]]

        row = matrix[_INDEX[current]]
        if any(v is None for v in row):
            markov_skipped += 1
        else:
            samples["markov"].append(([float(v) for v in row], actual))

        counts = markov.count_states(train_states)
        total = len(train_states)
        majority_dist = [counts[s] / total for s in markov.STATES]
        samples["majority"].append((majority_dist, actual))

        samples["persistence"].append(
            (markov.initial_distribution(current), actual)
        )

    return {
        "observations": len(states),
        "min_train": min_train,
        "steps": max(0, len(states) - 1 - min_train),
        "models": {
            "markov": _metrics(samples["markov"], markov_skipped),
            "majority": _metrics(samples["majority"], 0),
            "persistence": _metrics(samples["persistence"], 0),
        },
    }
