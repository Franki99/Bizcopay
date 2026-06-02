"""
Bizcopay Fraud Detection Microservice

Run after training the model:
    uvicorn app:app --host 0.0.0.0 --port 8000 --reload

POST /predict   — score a single transaction
GET  /health    — liveness probe
"""

import pickle

import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Bizcopay Fraud Detection Service", version="1.0.0")

# Load model and scaler once at startup
try:
    with open("model/model.pkl", "rb") as f:
        _model = pickle.load(f)
    with open("model/scaler.pkl", "rb") as f:
        _scaler = pickle.load(f)
except FileNotFoundError:
    raise RuntimeError(
        "Model files not found. Run  python train.py  first."
    )

# Risk thresholds (fraud probability → label)
# Option B: lower MEDIUM threshold so UNUSUAL_HOUR triggers MEDIUM in demo
HIGH_THRESHOLD = 0.7
MEDIUM_THRESHOLD = 0.08


class TransactionFeatures(BaseModel):
    amount: float = Field(..., gt=0, description="Transaction amount")
    hour: int = Field(..., ge=0, le=23, description="Hour of day (0-23)")
    velocity_60s: int = Field(..., ge=0, description="Payer transactions in last 60 seconds")


class FraudPrediction(BaseModel):
    risk: str        # LOW | MEDIUM | HIGH
    score: float     # fraud probability 0.0 – 1.0


@app.post("/predict", response_model=FraudPrediction)
def predict(tx: TransactionFeatures) -> FraudPrediction:
    features = np.array([[tx.amount, tx.hour, tx.velocity_60s]])
    features_scaled = _scaler.transform(features)
    score = float(_model.predict_proba(features_scaled)[0][1])

    if score >= HIGH_THRESHOLD:
        risk = "HIGH"
    elif score >= MEDIUM_THRESHOLD:
        risk = "MEDIUM"
    else:
        risk = "LOW"

    return FraudPrediction(risk=risk, score=round(score, 4))


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "model": "RandomForestClassifier"}
