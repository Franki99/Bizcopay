-- CreateEnum
CREATE TYPE "SpendingCategory" AS ENUM ('FOOD', 'TRANSPORT', 'SHOPPING', 'ENTERTAINMENT', 'HEALTH', 'EDUCATION', 'BILLS', 'OTHER');

-- AlterTable
ALTER TABLE "Transaction" ADD COLUMN     "category" "SpendingCategory",
ALTER COLUMN "currency" SET DEFAULT 'RWF';

-- AlterTable
ALTER TABLE "Wallet" ALTER COLUMN "currency" SET DEFAULT 'RWF';
