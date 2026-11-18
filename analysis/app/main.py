"""Analysis service entry point.

Endpoints:
- GET  /internal/v1/health            health check
- POST /internal/v1/transition-matrix state classification + transition
  counts / MLE transition probabilities for a ticker and period
"""

import os
from datetime import date

from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

from app import markov, prices

app = FastAPI(title="Markov Stock Analyzer 2 - Analysis Service")

INTERNAL_API_TOKEN = os.environ.get("INTERNAL_API_TOKEN", "")


def require_token(x_internal_token: str) -> None:
    if INTERNAL_API_TOKEN and x_internal_token != INTERNAL_API_TOKEN:
        raise HTTPException(status_code=401, detail="invalid internal token")


@app.get("/internal/v1/health")
def health(x_internal_token: str = Header(default="")) -> dict[str, str]:
    require_token(x_internal_token)
    return {"status": "UP"}


class TransitionMatrixRequest(BaseModel):
    ticker: str
    start: date
    end: date  # exclusive, following the yfinance convention
    theta: float = Field(default=markov.DEFAULT_THETA, gt=0)


class TransitionMatrixResponse(BaseModel):
    ticker: str
    start: date
    end: date
    theta: float
    data_start: date
    data_end: date
    observations: int
    state_counts: dict[str, int]
    transition_counts: list[list[int]]
    transition_probabilities: list[list[float | None]]
    current_state: str


@app.post("/internal/v1/transition-matrix", response_model=TransitionMatrixResponse)
def transition_matrix(
    req: TransitionMatrixRequest, x_internal_token: str = Header(default="")
) -> TransitionMatrixResponse:
    require_token(x_internal_token)
    try:
        closes = prices.fetch_adjusted_closes(req.ticker, req.start, req.end)
    except prices.PriceDataError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    if len(closes) < 3:
        raise HTTPException(
            status_code=422, detail="not enough price data to estimate transitions"
        )
    values = [c for _, c in closes]
    states = markov.classify_states(markov.compute_returns(values), req.theta)
    counts = markov.transition_counts(states)
    return TransitionMatrixResponse(
        ticker=req.ticker,
        start=req.start,
        end=req.end,
        theta=req.theta,
        data_start=closes[0][0],
        data_end=closes[-1][0],
        observations=len(states),
        state_counts=markov.count_states(states),
        transition_counts=counts,
        transition_probabilities=markov.estimate_transition_matrix(counts),
        current_state=states[-1],
    )
