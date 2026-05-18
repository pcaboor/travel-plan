export type PaymentProvider = "STRIPE" | "PAYPAL";
export type PaymentMethodType = "CARD" | "BANK_ACCOUNT" | "WALLET";
export type PaymentMethodStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

export type PaymentMethodResponse = {
  id: string;
  userId: string;
  provider: PaymentProvider;
  type: PaymentMethodType;
  lastFour: string | null;
  expiresAt: string | null;
  status: PaymentMethodStatus;
  createdAt: string;
  updatedAt: string;
};

export type PaymentMethodCreateInput = {
  provider: PaymentProvider;
  type: PaymentMethodType;
  providerToken?: string;
  lastFour?: string;
  expiresAt?: string;
  status?: PaymentMethodStatus;
};

export type PaymentMethodUpdateInput = {
  lastFour?: string;
  expiresAt?: string;
  status?: PaymentMethodStatus;
};

export const PROVIDERS: PaymentProvider[] = ["STRIPE", "PAYPAL"];
export const TYPES: PaymentMethodType[] = ["CARD", "BANK_ACCOUNT", "WALLET"];
export const STATUSES: PaymentMethodStatus[] = ["ACTIVE", "EXPIRED", "REVOKED"];
