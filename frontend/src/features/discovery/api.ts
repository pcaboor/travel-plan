import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { RecommendationHit, SubscriptionResponse, TravelHit } from "./types";

export function useTravelSearch(query: string) {
  const q = query.trim();
  return useQuery({
    queryKey: ["travel-search", q],
    queryFn: () => apiFetch<TravelHit[]>("/api/travels/search", { query: { q } }),
    enabled: q.length > 0,
  });
}

export function useAutocomplete(query: string) {
  const q = query.trim();
  return useQuery({
    queryKey: ["travel-autocomplete", q],
    queryFn: () => apiFetch<TravelHit[]>("/api/travels/autocomplete", { query: { q } }),
    enabled: q.length >= 2,
  });
}

export function useRecommendations() {
  return useQuery({
    queryKey: ["recommendations"],
    queryFn: () => apiFetch<RecommendationHit[]>("/api/travels/recommendations"),
  });
}

export function useSubscribe() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (travelId: string) =>
      apiFetch<SubscriptionResponse>(`/api/subscriptions/${travelId}`, { method: "POST" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-stats"] }),
  });
}
