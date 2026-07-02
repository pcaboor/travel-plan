import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { SubscriptionResponse } from "./types";

export function useMySubscriptions() {
  return useQuery({
    queryKey: ["my-subscriptions"],
    queryFn: () => apiFetch<SubscriptionResponse[]>("/api/subscriptions/me"),
  });
}

export function useUnsubscribe() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (travelId: string) =>
      apiFetch<SubscriptionResponse>(`/api/subscriptions/${travelId}/unsubscribe`, {
        method: "POST",
      }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["my-subscriptions"] });
      void qc.invalidateQueries({ queryKey: ["my-stats"] });
    },
  });
}

export function useConfirmSubscription() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (travelId: string) =>
      apiFetch<SubscriptionResponse>(`/api/subscriptions/${travelId}/confirm`, {
        method: "POST",
      }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["my-subscriptions"] });
      void qc.invalidateQueries({ queryKey: ["my-stats"] });
    },
  });
}
