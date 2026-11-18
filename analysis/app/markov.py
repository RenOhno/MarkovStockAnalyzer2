"""Core Markov-chain estimation logic.

Pure functions only: no I/O, no network. Prices are adjusted closes.

Research spec:
- Daily return r_t = C_t / C_{t-1} - 1
- States U (up), F (flat), D (down) with threshold theta (default 0.005):
  r_t > theta -> U, r_t < -theta -> D, otherwise F
  (a return of exactly +/-theta is classified as F)
- Transition probabilities are maximum-likelihood estimates p_ij = n_ij / n_i
- Rows with n_i = 0 are "unestimated" and represented as None,
  never filled with a uniform distribution
"""

from __future__ import annotations

STATES = ("U", "F", "D")
_STATE_INDEX = {s: i for i, s in enumerate(STATES)}
DEFAULT_THETA = 0.005


def compute_returns(closes: list[float]) -> list[float]:
    """Daily returns r_t = C_t / C_{t-1} - 1 for consecutive closes."""
    return [c / p - 1.0 for p, c in zip(closes, closes[1:])]


def classify_state(r: float, theta: float = DEFAULT_THETA) -> str:
    if r > theta:
        return "U"
    if r < -theta:
        return "D"
    return "F"


def classify_states(returns: list[float], theta: float = DEFAULT_THETA) -> list[str]:
    return [classify_state(r, theta) for r in returns]


def count_states(states: list[str]) -> dict[str, int]:
    counts = {s: 0 for s in STATES}
    for s in states:
        counts[s] += 1
    return counts


def transition_counts(states: list[str]) -> list[list[int]]:
    """3x3 transition counts n_ij in state order (U, F, D)."""
    counts = [[0] * 3 for _ in range(3)]
    for a, b in zip(states, states[1:]):
        counts[_STATE_INDEX[a]][_STATE_INDEX[b]] += 1
    return counts


def estimate_transition_matrix(
    counts: list[list[int]],
) -> list[list[float | None]]:
    """MLE p_ij = n_ij / n_i. Rows with n_i = 0 become [None, None, None]."""
    matrix: list[list[float | None]] = []
    for row in counts:
        n_i = sum(row)
        if n_i == 0:
            matrix.append([None, None, None])
        else:
            matrix.append([n / n_i for n in row])
    return matrix
