from datetime import date

import pytest
from fastapi.testclient import TestClient

from app import main, prices

client = TestClient(main.app)


def fake_closes(ticker: str, start: date, end: date) -> list[tuple[date, float]]:
    # Returns: +1.0%, 0.0%, -1.0%, +1.0% -> states U, F, D, U
    return [
        (date(2026, 1, 5), 100.0),
        (date(2026, 1, 6), 101.0),
        (date(2026, 1, 7), 101.0),
        (date(2026, 1, 8), 99.99),
        (date(2026, 1, 9), 100.99),
    ]


def test_transition_matrix(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(prices, "fetch_adjusted_closes", fake_closes)
    response = client.post(
        "/internal/v1/transition-matrix",
        json={"ticker": "TEST", "start": "2026-01-05", "end": "2026-01-10"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["observations"] == 4
    assert body["state_counts"] == {"U": 2, "F": 1, "D": 1}
    # Transitions: U->F, F->D, D->U
    assert body["transition_counts"] == [[0, 1, 0], [0, 0, 1], [1, 0, 0]]
    assert body["transition_probabilities"] == [
        [0.0, 1.0, 0.0],
        [0.0, 0.0, 1.0],
        [1.0, 0.0, 0.0],
    ]
    assert body["current_state"] == "U"
    assert body["theta"] == 0.005


def test_not_enough_data(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        prices,
        "fetch_adjusted_closes",
        lambda ticker, start, end: [(date(2026, 1, 5), 100.0), (date(2026, 1, 6), 101.0)],
    )
    response = client.post(
        "/internal/v1/transition-matrix",
        json={"ticker": "TEST", "start": "2026-01-05", "end": "2026-01-07"},
    )
    assert response.status_code == 422


def test_no_data(monkeypatch: pytest.MonkeyPatch) -> None:
    def raise_error(ticker: str, start: date, end: date) -> list[tuple[date, float]]:
        raise prices.PriceDataError("no price data")

    monkeypatch.setattr(prices, "fetch_adjusted_closes", raise_error)
    response = client.post(
        "/internal/v1/transition-matrix",
        json={"ticker": "NONE", "start": "2026-01-05", "end": "2026-01-10"},
    )
    assert response.status_code == 404
