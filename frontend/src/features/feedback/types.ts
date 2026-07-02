export type FeedbackResponse = {
  id: string;
  travelRefId: string;
  authorUserId: string;
  rating: number;
  comment: string | null;
  createdAt: string;
};

export type FeedbackCreateInput = {
  travelId: string;
  rating: number;
  comment?: string;
};
