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

import numpy as np

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


def initial_distribution(state: str) -> list[float]:
    """One-hot distribution alpha^(0) concentrated on the given state."""
    dist = [0.0, 0.0, 0.0]
    dist[_STATE_INDEX[state]] = 1.0
    return dist


def _step(
    dist: list[float], matrix: list[list[float | None]]
) -> list[float] | None:
    """One step alpha -> alpha P. None if probability mass would flow
    into an unestimated row (that step is then not computable)."""
    out = [0.0, 0.0, 0.0]
    for i, mass in enumerate(dist):
        if mass == 0.0:
            continue
        row = matrix[i]
        if any(v is None for v in row):
            return None
        for j in range(3):
            out[j] += mass * row[j]  # type: ignore[operator]
    return out


def distribution_trajectory(
    matrix: list[list[float | None]], initial_state: str, max_h: int
) -> list[list[float] | None]:
    """alpha^(0), alpha^(1), ..., alpha^(max_h) starting from initial_state.

    Entries become None once the distribution can no longer be computed
    because an unestimated row is involved.
    """
    trajectory: list[list[float] | None] = [initial_distribution(initial_state)]
    for _ in range(max_h):
        prev = trajectory[-1]
        trajectory.append(None if prev is None else _step(prev, matrix))
    return trajectory


def stationary_distribution(
    matrix: list[list[float | None]],
) -> list[float] | None:
    """Stationary distribution pi with pi = pi P and sum(pi) = 1.

    Solved as a linear system, so periodic chains are handled too.
    Returns None when any row is unestimated or no valid solution exists.
    """
    if any(v is None for row in matrix for v in row):
        return None
    p = np.array(matrix, dtype=float)
    a = p.T - np.eye(3)
    a[-1, :] = 1.0
    b = np.array([0.0, 0.0, 1.0])
    try:
        pi = np.linalg.solve(a, b)
    except np.linalg.LinAlgError:
        return None
    if np.any(pi < -1e-9):
        return None
    pi = np.clip(pi, 0.0, None)
    total = float(pi.sum())
    if total <= 0.0:
        return None
    return [float(x) for x in pi / total]


def total_variation_distance(
    dist: list[float], target: list[float]
) -> float:
    """Total variation distance (half the L1 distance), in [0, 1]."""
    return 0.5 * sum(abs(a - b) for a, b in zip(dist, target))
