import math

import pytest

from app import backtest


def test_cycle_sequence() -> None:
    # U -> F -> D -> U -> F -> D -> U: after 3 training transitions the
    # transition matrix is the cyclic one-hot matrix, so the Markov model
    # predicts every following state correctly.
    states = ["U", "F", "D", "U", "F", "D", "U"]
    result = backtest.run_backtest(states, min_train=3)

    assert result["observations"] == 7
    assert result["steps"] == 3

    markov = result["models"]["markov"]
    assert markov["predictions"] == 3
    assert markov["skipped"] == 0
    assert markov["accuracy"] == 1.0
    assert markov["confusion_matrix"] == [[1, 0, 0], [0, 1, 0], [0, 0, 1]]
    assert markov["brier_score"] == 0.0
    assert markov["log_loss"] == 0.0
    assert markov["precision_macro"] == 1.0
    assert markov["recall_macro"] == 1.0

    # Majority predicts U at all three steps (ties resolve to U first)
    # and is right only at the last step.
    majority = result["models"]["majority"]
    assert majority["predictions"] == 3
    assert majority["accuracy"] == pytest.approx(1 / 3)
    assert majority["confusion_matrix"] == [[1, 0, 0], [1, 0, 0], [1, 0, 0]]
    assert majority["brier_score"] == pytest.approx((0.875 + 0.96 + 2 / 3) / 3)
    assert majority["log_loss"] == pytest.approx(
        (-math.log(0.25) - math.log(0.2) - math.log(1 / 3)) / 3
    )
    assert majority["precision"]["U"] == pytest.approx(1 / 3)
    assert majority["precision"]["F"] == 0.0
    assert majority["recall"]["U"] == 1.0
    assert majority["recall"]["D"] == 0.0

    # Persistence is wrong at every step of the cycle.
    persistence = result["models"]["persistence"]
    assert persistence["predictions"] == 3
    assert persistence["accuracy"] == 0.0
    assert persistence["confusion_matrix"] == [[0, 0, 1], [1, 0, 0], [0, 1, 0]]
    assert persistence["brier_score"] == 2.0
    assert persistence["log_loss"] == pytest.approx(-math.log(1e-15))


def test_unestimated_current_row_is_skipped() -> None:
    # At k=4 the current state D has never been a "from" state in the
    # training transitions, so its row is unestimated and the Markov
    # model must skip the step instead of inventing a distribution.
    states = ["U", "U", "U", "U", "D", "U"]
    result = backtest.run_backtest(states, min_train=3)

    assert result["steps"] == 2
    markov = result["models"]["markov"]
    assert markov["predictions"] == 1
    assert markov["skipped"] == 1
    assert markov["accuracy"] == 0.0
    # The single prediction was U (row [1, 0, 0]) while the actual was D.
    assert markov["confusion_matrix"] == [[0, 0, 0], [0, 0, 0], [1, 0, 0]]

    # Baselines still predict at both steps.
    assert result["models"]["majority"]["predictions"] == 2
    assert result["models"]["persistence"]["predictions"] == 2


def test_no_steps_when_sequence_too_short() -> None:
    result = backtest.run_backtest(["U", "F", "D"], min_train=30)
    assert result["steps"] == 0
    for model in result["models"].values():
        assert model["predictions"] == 0
        assert model["accuracy"] is None
        assert model["brier_score"] is None
        assert model["log_loss"] is None
