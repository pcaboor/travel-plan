import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type {
  TravelMutationInput,
  TravelPage,
  TravelResponse,
} from "./types";

const BASE = "/api/travels";

export function useTravels(page: number, size: number) {
  return useQuery({
    queryKey: ["travels", page, size],
    queryFn: () =>
      apiFetch<TravelPage>(BASE, { query: { page, size, sort: "title,asc" } }),
    placeholderData: (prev) => prev,
  });
}

export function useTravel(id: string | undefined) {
  return useQuery({
    queryKey: ["travels", id],
    queryFn: () => apiFetch<TravelResponse>(`${BASE}/${id}`),
    enabled: Boolean(id),
  });
}

export function useCreateTravel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: TravelMutationInput) =>
      apiFetch<TravelResponse>(BASE, { method: "POST", body }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["travels"] }),
  });
}

export function useUpdateTravel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: TravelMutationInput }) =>
      apiFetch<TravelResponse>(`${BASE}/${id}`, { method: "PUT", body }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["travels"] }),
  });
}

export function useDeleteTravel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["travels"] }),
  });
}
