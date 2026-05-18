import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { BookingPage } from "./types";

const BASE = "/api/admin/bookings";

export function useBookings(page: number, size: number) {
  return useQuery({
    queryKey: ["bookings", page, size],
    queryFn: () =>
      apiFetch<BookingPage>(BASE, { query: { page, size, sort: "createdAt,desc" } }),
    placeholderData: (prev) => prev,
  });
}

export function useCancelByTravel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (travelRefId: string) =>
      apiFetch<{ cancelled: number }>(`${BASE}/cancel-by-travel/${travelRefId}`, {
        method: "POST",
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["bookings"] }),
  });
}
