"""Price data access (Yahoo Finance via yfinance).

Kept separate from the estimation logic so tests can replace it.
Adjusted closes are used (auto_adjust=True), matching the research spec.
Note: yfinance treats `end` as exclusive.
"""

from __future__ import annotations

from datetime import date

import yfinance as yf


class PriceDataError(Exception):
    """Raised when no usable price data is returned."""


def fetch_adjusted_closes(
    ticker: str, start: date, end: date
) -> list[tuple[date, float]]:
    """Adjusted closes for [start, end) as (date, close) pairs, date ascending."""
    history = yf.Ticker(ticker).history(
        start=start.isoformat(), end=end.isoformat(), auto_adjust=True
    )
    if history.empty:
        raise PriceDataError(f"no price data for ticker {ticker!r}")
    closes = [(idx.date(), float(row["Close"])) for idx, row in history.iterrows()]
    closes.sort(key=lambda item: item[0])
    return closes
