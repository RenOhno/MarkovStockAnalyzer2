import pytest

from app import markov


class TestComputeReturns:
    def test_simple(self) -> None:
        returns = markov.compute_returns([100.0, 101.0, 101.0])
        assert returns == pytest.approx([0.01, 0.0])

    def test_empty_and_single(self) -> None:
        assert markov.compute_returns([]) == []
        assert markov.compute_returns([100.0]) == []


class TestClassifyState:
    def test_up_and_down(self) -> None:
        assert markov.classify_state(0.02) == "U"
        assert markov.classify_state(-0.02) == "D"

    def test_flat_inside_threshold(self) -> None:
        assert markov.classify_state(0.0) == "F"
        assert markov.classify_state(0.004) == "F"
        assert markov.classify_state(-0.004) == "F"

    def test_exactly_threshold_is_flat(self) -> None:
        # Boundary rule: r == +/-theta is F, only strictly beyond is U/D.
        assert markov.classify_state(0.005) == "F"
        assert markov.classify_state(-0.005) == "F"

    def test_just_beyond_threshold(self) -> None:
        assert markov.classify_state(0.0050001) == "U"
        assert markov.classify_state(-0.0050001) == "D"

    def test_custom_theta(self) -> None:
        assert markov.classify_state(0.008, theta=0.01) == "F"
        assert markov.classify_state(0.012, theta=0.01) == "U"


class TestTransitionCounts:
    def test_counts(self) -> None:
        states = ["U", "F", "D", "U", "U"]
        counts = markov.transition_counts(states)
        # U->F, F->D, D->U, U->U
        assert counts == [[1, 1, 0], [0, 0, 1], [1, 0, 0]]

    def test_row_totals(self) -> None:
        states = ["U", "U", "F", "F", "D"]
        counts = markov.transition_counts(states)
        assert sum(sum(row) for row in counts) == len(states) - 1

    def test_empty(self) -> None:
        assert markov.transition_counts([]) == [[0, 0, 0], [0, 0, 0], [0, 0, 0]]


class TestEstimateTransitionMatrix:
    def test_mle_values(self) -> None:
        # The seminar example: from U, n_UU=6, n_UF=2, n_UD=2 -> (0.6, 0.2, 0.2)
        counts = [[6, 2, 2], [0, 0, 0], [1, 1, 2]]
        matrix = markov.estimate_transition_matrix(counts)
        assert matrix[0] == pytest.approx([0.6, 0.2, 0.2])
        assert matrix[2] == pytest.approx([0.25, 0.25, 0.5])

    def test_rows_sum_to_one(self) -> None:
        counts = [[3, 1, 6], [2, 2, 2], [0, 5, 5]]
        for row in markov.estimate_transition_matrix(counts):
            assert sum(v for v in row if v is not None) == pytest.approx(1.0)

    def test_zero_row_is_unestimated(self) -> None:
        counts = [[6, 2, 2], [0, 0, 0], [1, 1, 2]]
        matrix = markov.estimate_transition_matrix(counts)
        assert matrix[1] == [None, None, None]


class TestCountStates:
    def test_counts(self) -> None:
        assert markov.count_states(["U", "F", "U", "D", "U"]) == {
            "U": 3,
            "F": 1,
            "D": 1,
        }


# Toyota 7203 transition matrix estimated in STEP 2 (rows U, F, D)
TOYOTA_MATRIX = [
    [142 / 358, 88 / 358, 128 / 358],
    [95 / 250, 77 / 250, 78 / 250],
    [121 / 322, 86 / 322, 115 / 322],
]


class TestDistributionTrajectory:
    def test_first_step_is_matrix_row(self) -> None:
        trajectory = markov.distribution_trajectory(TOYOTA_MATRIX, "F", 10)
        assert trajectory[0] == [0.0, 1.0, 0.0]
        assert trajectory[1] == pytest.approx(TOYOTA_MATRIX[1])

    def test_distributions_sum_to_one(self) -> None:
        trajectory = markov.distribution_trajectory(TOYOTA_MATRIX, "U", 30)
        for dist in trajectory:
            assert dist is not None
            assert sum(dist) == pytest.approx(1.0)

    def test_converges_to_stationary(self) -> None:
        stationary = markov.stationary_distribution(TOYOTA_MATRIX)
        trajectory = markov.distribution_trajectory(TOYOTA_MATRIX, "F", 60)
        final = trajectory[-1]
        assert stationary is not None and final is not None
        assert markov.total_variation_distance(final, stationary) < 0.01

    def test_unestimated_row_blocks_trajectory(self) -> None:
        matrix = [[0.5, 0.5, 0.0], [None, None, None], [0.0, 0.0, 1.0]]
        trajectory = markov.distribution_trajectory(matrix, "U", 3)
        assert trajectory[0] == [1.0, 0.0, 0.0]
        # Step 1 puts mass 0.5 on F, whose row is unestimated -> step 2 is None
        assert trajectory[1] == pytest.approx([0.5, 0.5, 0.0])
        assert trajectory[2] is None
        assert trajectory[3] is None


class TestStationaryDistribution:
    def test_toyota_stationary(self) -> None:
        # Matches the v1 trial: the horizon distribution from FLAT converged
        # to approximately (38.50%, 26.99%, 34.51%).
        stationary = markov.stationary_distribution(TOYOTA_MATRIX)
        assert stationary == pytest.approx([0.3850, 0.2699, 0.3451], abs=1e-3)

    def test_periodic_chain(self) -> None:
        # Deterministic cycle U -> F -> D -> U: stationary is uniform even
        # though power iteration would not converge.
        matrix = [[0.0, 1.0, 0.0], [0.0, 0.0, 1.0], [1.0, 0.0, 0.0]]
        stationary = markov.stationary_distribution(matrix)
        assert stationary == pytest.approx([1 / 3, 1 / 3, 1 / 3])

    def test_unestimated_row_returns_none(self) -> None:
        matrix = [[0.5, 0.5, 0.0], [None, None, None], [0.0, 0.0, 1.0]]
        assert markov.stationary_distribution(matrix) is None

    def test_is_fixed_point(self) -> None:
        stationary = markov.stationary_distribution(TOYOTA_MATRIX)
        assert stationary is not None
        nxt = markov._step(stationary, TOYOTA_MATRIX)
        assert nxt == pytest.approx(stationary)
