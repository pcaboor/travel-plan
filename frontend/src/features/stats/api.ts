import { useQuery } from "@tanstack/react-query";

import { apiFetch } from "@/lib/api";
import type { ManagerDashboard, ManagerScore, TravelerStats } from "./types";

export function useMyStats() {
  return useQuery({
    queryKey: ["my-stats"],
    queryFn: () => apiFetch<TravelerStats>("/api/stats/me"),
  });
}

export function useManagerDashboard() {
  return useQuery({
    queryKey: ["manager-dashboard"],
    queryFn: () => apiFetch<ManagerDashboard>("/api/stats/manager/me"),
  });
}

export function useLeaderboard() {
  return useQuery({
    queryKey: ["leaderboard"],
    queryFn: () => apiFetch<ManagerScore[]>("/api/stats/managers/leaderboard"),
  });
}
