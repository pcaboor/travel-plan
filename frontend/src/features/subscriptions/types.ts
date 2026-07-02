export type SubscriptionResponse = {
  bookingId: string;
  travelRefId: string;
  status: string;
  amount: number;
  currency: string;
  travelStartDate: string | null;
};
