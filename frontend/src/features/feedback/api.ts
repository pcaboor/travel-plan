import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { FeedbackCreateInput, FeedbackResponse } from "./types";

export function useMyFeedback() {
  return useQuery({
    queryKey: ["my-feedback"],
    queryFn: () => apiFetch<FeedbackResponse[]>("/api/feedback/me"),
  });
}

export function useCreateFeedback() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: FeedbackCreateInput) =>
      apiFetch<FeedbackResponse>("/api/feedback", { method: "POST", body }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["my-feedback"] });
      void qc.invalidateQueries({ queryKey: ["my-stats"] });
    },
  });
}
