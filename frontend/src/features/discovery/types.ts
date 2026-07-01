export type TravelHit = {
  id: string;
  title: string | null;
  description: string | null;
  price: number | null;
  currency: string | null;
  status: string | null;
};

export type RecommendationHit = {
  id: string;
  title: string | null;
  status: string | null;
  score: number;
};

export type SubscriptionResponse = {
  bookingId: string;
  travelRefId: string;
  status: string;
  amount: number;
  currency: string;
  travelStartDate: string | null;
};
