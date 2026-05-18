import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type {
  UserCreateInput,
  UserPage,
  UserResponse,
  UserUpdateInput,
} from "./types";

const BASE = "/api/admin/users";

export function useUsers(page: number, size: number) {
  return useQuery({
    queryKey: ["users", page, size],
    queryFn: () =>
      apiFetch<UserPage>(BASE, {
        query: { page, size, sort: "createdAt,desc" },
      }),
    placeholderData: (prev) => prev,
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: UserCreateInput) =>
      apiFetch<UserResponse>(BASE, { method: "POST", body }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UserUpdateInput }) =>
      apiFetch<UserResponse>(`${BASE}/${id}`, { method: "PUT", body }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: ({ id, newPassword }: { id: string; newPassword: string }) =>
      apiFetch<void>(`${BASE}/${id}/password`, {
        method: "PUT",
        body: { newPassword },
      }),
  });
}

export function useAssignRoles() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, roles }: { id: string; roles: string[] }) =>
      apiFetch<UserResponse>(`${BASE}/${id}/roles`, {
        method: "PUT",
        body: { roles },
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}

export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}
