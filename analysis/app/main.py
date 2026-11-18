"""Analysis service entry point.

STEP 1 skeleton: only the internal health endpoint exists.
Markov estimation endpoints are added in later steps.
"""

import os

from fastapi import FastAPI, Header, HTTPException

app = FastAPI(title="Markov Stock Analyzer 2 - Analysis Service")

INTERNAL_API_TOKEN = os.environ.get("INTERNAL_API_TOKEN", "")


@app.get("/internal/v1/health")
def health(x_internal_token: str = Header(default="")) -> dict[str, str]:
    if INTERNAL_API_TOKEN and x_internal_token != INTERNAL_API_TOKEN:
        raise HTTPException(status_code=401, detail="invalid internal token")
    return {"status": "UP"}
