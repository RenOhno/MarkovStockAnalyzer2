from datetime import date, timedelta

import pytest
from fastapi.testclient import TestClient

from app import main, prices

client = TestClient(main.app)


def fake_closes_cycle(
    ticker: str, start: date, end: date
) -> list[tuple[date, float]]:
    # Returns +1%, 0%, -1% repeated 15 times -> states U, F, D, U, F, D, ...
    closes = [(date(2026, 1, 5), 100.0)]
    price = 100.0
    day = date(2026, 1, 5)
    for r in [0.01, 0.0, -0.01] * 15:
        price *= 1.0 + r
        day += timedelta(days=1)
        closes.append((day, price))
    return closes


def test_backtest(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(prices, "fetch_adjusted_closes", fake_closes_cycle)
    response = client.post(
        "/internal/v1/backtest",
        json={"ticker": "TEST", "start": "2026-01-05", "end": "2026-03-01"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["observations"] == 45
    assert body["min_train"] == 30
    assert body["steps"] == 14

    # The cycle is deterministic, so the Markov model is always right
    # and Persistence (same state again) is always wrong.
    markov = body["models"]["markov"]
    assert markov["predictions"] == 14
    assert markov["skipped"] == 0
    assert markov["accuracy"] == 1.0
    assert markov["brier_score"] == 0.0
    persistence = body["models"]["persistence"]
    assert persistence["accuracy"] == 0.0
    majority = body["models"]["majority"]
    assert majority["predictions"] == 14
    assert 0.0 <= majority["accuracy"] <= 1.0


def test_backtest_not_enough_data(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        prices,
        "fetch_adjusted_closes",
        lambda ticker, start, end: [
            (date(2026, 1, 5) + timedelta(days=i), 100.0 + i) for i in range(10)
        ],
    )
    response = client.post(
        "/internal/v1/backtest",
        json={"ticker": "TEST", "start": "2026-01-05", "end": "2026-01-15"},
    )
    assert response.status_code == 422


def test_backtest_no_data(monkeypatch: pytest.MonkeyPatch) -> None:
    def raise_error(ticker: str, start: date, end: date) -> list[tuple[date, float]]:
        raise prices.PriceDataError("no price data")

    monkeypatch.setattr(prices, "fetch_adjusted_closes", raise_error)
    response = client.post(
        "/internal/v1/backtest",
        json={"ticker": "NONE", "start": "2026-01-05", "end": "2026-03-01"},
    )
    assert response.status_code == 404
