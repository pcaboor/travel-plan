export type TravelerStats = {
  participations: number;
  cancellations: number;
  reportsFiled: number;
  feedbackGiven: number;
  paymentProviders: string[];
};

export type ManagerDashboard = {
  income: number;
  trips: number;
  travelers: number;
  averageRating: number;
};

export type ManagerScore = {
  managerId: string;
  income: number;
  trips: number;
  travelers: number;
  averageRating: number;
  reportCount: number;
  score: number;
};
