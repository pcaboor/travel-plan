export type BookingStatus =
  | "PENDING"
  | "CONFIRMED"
  | "CANCELLED"
  | "COMPLETED"
  | "REFUNDED";

export type BookingResponse = {
  id: string;
  userId: string;
  travelRefId: string;
  paymentMethodId: string | null;
  amount: number;
  currency: string;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
};

export type BookingPage = {
  content: BookingResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};
