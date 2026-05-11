"""
Bizcopay Fraud Detection - Model Training Script

Dataset: Kaggle Credit Card Fraud Detection
  https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud

Setup:
  1. Download creditcard.csv from Kaggle and place it in this directory (ml/)
  2. Create a virtual environment:
       python -m venv venv
       venv\\Scripts\\activate          # Windows
       source venv/bin/activate        # Linux/Mac
  3. Install dependencies:
       pip install -r requirements.txt
  4. Run:
       python train.py

Output: model/model.pkl and model/scaler.pkl
"""

import os
import pickle
from collections import deque

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

FEATURES = ["Amount", "hour", "velocity_60s"]
FRAUD_SCORE_THRESHOLD = 0.3  # lower = higher recall on fraud cases

print("Loading dataset...")
if not os.path.exists("creditcard.csv"):
    raise FileNotFoundError(
        "creditcard.csv not found.\n"
        "Download it from https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud "
        "and place it in the ml/ directory."
    )

df = pd.read_csv("creditcard.csv")

# Extract hour of day from Time (seconds elapsed since first transaction in dataset)
df["hour"] = ((df["Time"] % 86400) // 3600).astype(int)

# Compute velocity_60s: how many transactions occurred in the 60 seconds before each row.
# This approximates the "rapid successive transactions" feature used in live scoring.
print("Computing transaction velocity (this takes ~30 seconds)...")
df = df.sort_values("Time").reset_index(drop=True)
times = df["Time"].values
velocity = []
window = deque()
for t in times:
    while window and window[0] < t - 60:
        window.popleft()
    velocity.append(len(window))
    window.append(t)
df["velocity_60s"] = velocity

X = df[FEATURES]
y = df["Class"]

total = len(df)
fraud_count = int(y.sum())
print(f"\nDataset: {total:,} transactions | {fraud_count:,} fraud ({y.mean() * 100:.3f}%)")

# Stratified split preserves the fraud ratio in both train and test sets
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)

print("\nTraining Random Forest classifier (class_weight='balanced')...")
model = RandomForestClassifier(
    n_estimators=100,
    class_weight="balanced",  # compensates for severe class imbalance (~0.17% fraud)
    random_state=42,
    n_jobs=-1,
)
model.fit(X_train_scaled, y_train)

y_prob = model.predict_proba(X_test_scaled)[:, 1]
y_pred = (y_prob > FRAUD_SCORE_THRESHOLD).astype(int)

print(f"\nClassification Report (decision threshold = {FRAUD_SCORE_THRESHOLD}):")
print(classification_report(y_test, y_pred, target_names=["Legit", "Fraud"]))
print(f"ROC-AUC Score: {roc_auc_score(y_test, y_prob):.4f}")
print(f"\nFeature importances:")
for name, imp in sorted(zip(FEATURES, model.feature_importances_), key=lambda x: -x[1]):
    print(f"  {name}: {imp:.4f}")

os.makedirs("model", exist_ok=True)
with open("model/model.pkl", "wb") as f:
    pickle.dump(model, f)
with open("model/scaler.pkl", "wb") as f:
    pickle.dump(scaler, f)

print("\nSaved: model/model.pkl and model/scaler.pkl")
print("Next: run the API server with  uvicorn app:app --reload")
