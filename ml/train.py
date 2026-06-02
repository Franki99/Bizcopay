"""
Bizcopay Fraud Detection - Model Training Script

Trains a Random Forest classifier on synthetic transaction data designed to
reflect the three fraud patterns specific to the Bizcopay payment system:
  1. HIGH_AMOUNT   — transaction amount above the risk threshold
  2. RAPID_TX      — payer makes 3+ transactions within 60 seconds
  3. UNUSUAL_HOUR  — transaction between midnight and 05:00

Synthetic data is used because the Kaggle Credit Card Fraud Detection
dataset's fraud labels are driven by 28 PCA-transformed behavioural features
that are not available in this system. Generating labelled data from domain
rules and adding statistical noise is a standard approach when a labelled
dataset matching the target feature space does not exist.

Setup:
  python -m venv venv
  venv\\Scripts\\activate        # Windows
  source venv/bin/activate      # Linux/Mac
  pip install -r requirements.txt
  python train.py
"""

import os
import pickle

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

FEATURES = ["amount", "hour", "velocity_60s"]
RANDOM_STATE = 42
rng = np.random.default_rng(RANDOM_STATE)

# ── Fraud thresholds (mirror fraud.service.ts constants) ─────────────────────
# Amounts are in RWF (Rwandan Franc). 1 USD ≈ 1,300 RWF.
AMOUNT_HIGH = 50_000    # > this → HIGH fraud signal (~$38)
RAPID_TX_LIMIT = 3      # >= this in 60 s → HIGH fraud signal
UNUSUAL_HOUR_END = 5    # 00:00–05:00 → MEDIUM fraud signal


def generate_dataset(n_legit: int = 80_000, n_fraud: int = 8_000) -> pd.DataFrame:
    """
    Build a labelled dataset (Class=0 legit, Class=1 fraud) whose statistical
    properties reflect the Bizcopay domain. Amounts are in RWF.
    """
    rows = []

    # ── Legitimate transactions ───────────────────────────────────────────────
    # Amounts: log-normal centred around ~5,000 RWF, clipped below the risk threshold
    legit_amounts = rng.lognormal(mean=8.5, sigma=1.0, size=n_legit)
    legit_amounts = np.clip(legit_amounts, 100.0, AMOUNT_HIGH - 1)

    # Hour: weighted toward business hours (08:00–20:00)
    hour_weights = np.array([
        0.5, 0.4, 0.3, 0.3, 0.3, 0.4,   # 00-05
        0.8, 1.5, 2.5, 3.0, 3.0, 3.0,   # 06-11
        3.0, 3.0, 3.0, 2.8, 2.5, 2.5,   # 12-17
        2.5, 2.2, 1.8, 1.2, 0.9, 0.7,   # 18-23
    ])
    hour_weights /= hour_weights.sum()
    legit_hours = rng.choice(24, size=n_legit, p=hour_weights)

    # Velocity: mostly 0 or 1
    legit_velocity = rng.poisson(lam=0.4, size=n_legit)
    legit_velocity = np.clip(legit_velocity, 0, RAPID_TX_LIMIT - 1)

    for a, h, v in zip(legit_amounts, legit_hours, legit_velocity):
        rows.append({"amount": a, "hour": int(h), "velocity_60s": int(v), "Class": 0})

    # ── Fraudulent transactions ───────────────────────────────────────────────
    # Three fraud archetypes, roughly equal in size, with Gaussian noise on
    # features so the model learns smooth probability boundaries.

    n_each = n_fraud // 3
    n_extra = n_fraud - 2 * n_each  # assign remainder to type 1

    # Type 1 — HIGH_AMOUNT: amount well above threshold (~75,000 RWF), any hour
    t1_amounts = rng.normal(loc=75_000, scale=15_000, size=n_each + n_extra)
    t1_amounts = np.clip(t1_amounts, AMOUNT_HIGH + 1, 200_000)
    t1_hours   = rng.choice(24, size=n_each + n_extra)
    t1_velocity = rng.poisson(lam=0.5, size=n_each + n_extra)
    for a, h, v in zip(t1_amounts, t1_hours, t1_velocity):
        rows.append({"amount": a, "hour": int(h), "velocity_60s": int(v), "Class": 1})

    # Type 2 — RAPID_TRANSACTIONS: high velocity, any hour, normal-ish amount
    t2_amounts   = rng.lognormal(mean=8.5, sigma=1.0, size=n_each)
    t2_amounts   = np.clip(t2_amounts, 100.0, AMOUNT_HIGH - 1)
    t2_hours     = rng.choice(24, size=n_each)
    t2_velocity  = rng.integers(low=RAPID_TX_LIMIT, high=10, size=n_each)
    for a, h, v in zip(t2_amounts, t2_hours, t2_velocity):
        rows.append({"amount": a, "hour": int(h), "velocity_60s": int(v), "Class": 1})

    # Type 3 — UNUSUAL_HOUR: overnight + elevated amount
    t3_hours    = rng.integers(low=0, high=UNUSUAL_HOUR_END, size=n_each)
    t3_amounts  = rng.normal(loc=30_000, scale=12_000, size=n_each)
    t3_amounts  = np.clip(t3_amounts, 5_000.0, AMOUNT_HIGH - 1)
    t3_velocity = rng.poisson(lam=0.6, size=n_each)
    for a, h, v in zip(t3_amounts, t3_hours, t3_velocity):
        rows.append({"amount": a, "hour": int(h), "velocity_60s": int(v), "Class": 1})

    df = pd.DataFrame(rows).sample(frac=1, random_state=RANDOM_STATE).reset_index(drop=True)
    return df


print("Generating synthetic training data...")
df = generate_dataset()
y = df["Class"]
total = len(df)
print(f"Dataset: {total:,} transactions | {int(y.sum()):,} fraud ({y.mean() * 100:.1f}%)")

X = df[FEATURES]
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=RANDOM_STATE, stratify=y
)

scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled  = scaler.transform(X_test)

print("Training Random Forest classifier...")
model = RandomForestClassifier(
    n_estimators=100,
    class_weight="balanced",
    random_state=RANDOM_STATE,
    n_jobs=-1,
)
model.fit(X_train_scaled, y_train)

y_prob = model.predict_proba(X_test_scaled)[:, 1]
y_pred = (y_prob > 0.3).astype(int)

print("\nClassification Report (threshold = 0.3):")
print(classification_report(y_test, y_pred, target_names=["Legit", "Fraud"]))
print(f"ROC-AUC Score: {roc_auc_score(y_test, y_prob):.4f}")

print("\nFeature importances:")
for name, imp in sorted(zip(FEATURES, model.feature_importances_), key=lambda x: -x[1]):
    print(f"  {name}: {imp:.4f}")

os.makedirs("model", exist_ok=True)
with open("model/model.pkl", "wb") as f:
    pickle.dump(model, f)
with open("model/scaler.pkl", "wb") as f:
    pickle.dump(scaler, f)

print("\nSaved: model/model.pkl  model/scaler.pkl")
print("Next: uvicorn app:app --reload")
