import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type {
  PaymentMethodCreateInput,
  PaymentMethodResponse,
  PaymentMethodUpdateInput,
} from "./types";

const base = (userId: string) => `/api/admin/users/${userId}/payment-methods`;

export function usePaymentMethods(userId: string) {
  return useQuery({
    queryKey: ["payment-methods", userId],
    queryFn: () => apiFetch<PaymentMethodResponse[]>(base(userId)),
    enabled: Boolean(userId),
  });
}

export function useCreatePaymentMethod(userId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: PaymentMethodCreateInput) =>
      apiFetch<PaymentMethodResponse>(base(userId), { method: "POST", body }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["payment-methods", userId] }),
  });
}

export function useUpdatePaymentMethod(userId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: PaymentMethodUpdateInput }) =>
      apiFetch<PaymentMethodResponse>(`${base(userId)}/${id}`, {
        method: "PUT",
        body,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["payment-methods", userId] }),
  });
}

export function useDeletePaymentMethod(userId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`${base(userId)}/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["payment-methods", userId] }),
  });
}
