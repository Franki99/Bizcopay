# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bizcopay is a final-year project: an NFC-based contactless payment system with a closed-loop digital wallet and real-time fraud detection. Three components:

- **`backend/`** — Node.js + Express + TypeScript REST API with Socket.IO (the only component scaffolded so far)
- **`android/`** — Kotlin Android app (not yet created)
- **`admin/`** — Next.js admin panel (not yet created)

## Git Workflow

After completing any meaningful unit of work (a feature, a fix, a scaffold), commit and push immediately. Never let working code sit uncommitted.

```bash
git add <specific files>
git commit -m "type: short description"
git push origin main
```

Commit message types: `feat`, `fix`, `refactor`, `test`, `chore`. Examples:
- `feat: add fraud detection rule engine`
- `fix: cast Prisma Json field to InputJsonValue in fraud log`
- `chore: scaffold backend with Express, Prisma, Socket.IO`

## Backend Commands

All commands run from `backend/`:

```bash
npm run dev          # Start dev server with hot reload (tsx watch)
npm run build        # TypeScript compile → dist/
npm start            # Run compiled dist/server.js
npm test             # Jest test suite
npm run db:migrate   # Apply Prisma migrations (also runs prisma generate)
npm run db:generate  # Regenerate Prisma client after schema changes
npm run db:studio    # Open Prisma Studio (visual DB browser)
```

**Single test file:**
```bash
npx jest src/modules/auth/auth.service.test.ts
npx jest --testNamePattern="should register user"
```

**First-time setup:**
```bash
cp .env.example .env        # fill in JWT_SECRET (min 32 chars)
docker-compose up -d        # start PostgreSQL 16 on port 5432
npm install
npm run db:migrate
npm run dev
```

## Backend Architecture

### Module Structure

Each feature under `src/modules/` follows the same three-file pattern: `*.routes.ts` → `*.controller.ts` → `*.service.ts`. Controllers handle request/response only; all business logic lives in services. Validation (Zod schemas) is defined and exported from services, then called in controllers.

### Request Lifecycle

```
Request → auth.middleware.ts (JWT verify) → requireRole() → Controller → Service → Prisma → DB
                                                                              ↓
                                                                       Socket.IO emit
```

### Auth & Roles

- JWT payload: `{ userId, role }`. Role is one of `PAYER | MERCHANT | ADMIN`.
- `authenticate` middleware attaches `req.user`. Protected routes always call it first.
- `requireRole(...roles)` is composed on top for endpoint-level access control.
- PINs are 4-digit numeric strings, bcrypt-hashed, never returned in responses. The `sanitize()` helper strips `pin` from all user objects.

### Transaction Lifecycle

1. **Merchant** calls `POST /api/transactions` → `PENDING` record created with 5-min expiry
2. **Merchant's Android app** reads payer's NFC UID and calls `POST /api/transactions/:id/nfc`
3. Backend runs fraud detection (`fraud.service.ts`) against the resolved payer:
   - `HIGH` → transaction fails immediately, emit `payment:failed`
   - `MEDIUM` → status → `AWAITING_PIN`, emit `payment:pending_pin` to payer
   - `LOW` → auto-approve if balance sufficient, emit `payment:approved`
4. **Payer** enters PIN → `POST /api/transactions/:id/approve` → PIN verified → balance deducted atomically

Payment debit/credit is wrapped in `prisma.$transaction()` to guarantee atomicity.

### Socket.IO Rooms

| Room | Who joins | Events received |
|---|---|---|
| `user:{userId}` | Auto on connect (JWT auth in socket middleware) | `payment:pending_pin` |
| `transaction:{id}` | Client calls `join:transaction` event | `payment:approved`, `payment:failed`, `payment:pending_pin` |

Socket connections require a valid JWT in `socket.handshake.auth.token`.

### Fraud Detection Rules (fraud.service.ts)

Three rules evaluated in order on every NFC resolve:

1. `HIGH_AMOUNT` — amount > 500 → **HIGH**
2. `RAPID_TRANSACTIONS` — ≥3 active transactions from same payer in last 60s → **HIGH**
3. `UNUSUAL_HOUR` — transaction between 00:00–05:00 → **MEDIUM**

Triggered rules create a `FraudLog` record. Constants are top-level `const`s in the file — adjust thresholds there.

### Environment Variables

Validated at startup by Zod in `src/config/env.ts`. Process exits immediately if any required var is missing or malformed.

| Variable | Description |
|---|---|
| `DATABASE_URL` | PostgreSQL connection string |
| `JWT_SECRET` | Min 32 characters |
| `PORT` | Default 3000 |
| `CORS_ORIGIN` | Default `*` |

### Error Handling

`AppError(statusCode, message)` for expected errors. Throw it anywhere in services — the central error middleware in `src/middleware/error.middleware.ts` catches it. Zod validation errors are automatically caught and return 400 with field-level detail.

## Database Schema (Prisma)

Key relationships:
- `User` 1:1 `Wallet` (auto-created on registration)
- `User` 1:many `NfcToken` (card/ring/bracelet UIDs)
- `Transaction` has nullable `payerId` (set after NFC tap) and non-null `merchantId`
- `Transaction` 1:1 optional `FraudLog` (only created when a rule fires)

After any schema change: `npm run db:migrate` (dev) or `npx prisma migrate deploy` (prod).
