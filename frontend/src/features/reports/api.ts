import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { ReportCreateInput, ReportResponse, ReportStatus } from "./types";

export function useMyReports() {
  return useQuery({
    queryKey: ["my-reports"],
    queryFn: () => apiFetch<ReportResponse[]>("/api/reports/me"),
  });
}

export function useReports(status: ReportStatus | "") {
  return useQuery({
    queryKey: ["reports", status || "ALL"],
    queryFn: () =>
      apiFetch<ReportResponse[]>("/api/reports", { query: { status: status || undefined } }),
  });
}

export function useCreateReport() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: ReportCreateInput) =>
      apiFetch<ReportResponse>("/api/reports", { method: "POST", body }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["my-reports"] });
      void qc.invalidateQueries({ queryKey: ["my-stats"] });
    },
  });
}

export function useUpdateReportStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: ReportStatus }) =>
      apiFetch<ReportResponse>(`/api/reports/${id}/status`, {
        method: "POST",
        body: { status },
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["reports"] }),
  });
}
