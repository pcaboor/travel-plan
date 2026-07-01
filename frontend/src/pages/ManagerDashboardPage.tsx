import { Coins, Route, Star, Users } from "lucide-react";

import { StatCard } from "@/components/StatCard";
import { useManagerDashboard } from "@/features/stats/api";

export function ManagerDashboardPage() {
  const { data, isLoading, error } = useManagerDashboard();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-sm text-muted-foreground">Your travels at a glance.</p>
      </div>

      {error && (
        <div className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
          {error instanceof Error ? error.message : "Failed to load dashboard"}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={Coins}
          label="Income"
          value={data ? `${data.income} €` : undefined}
          loading={isLoading}
        />
        <StatCard icon={Route} label="Trips" value={data?.trips} loading={isLoading} />
        <StatCard icon={Users} label="Travelers" value={data?.travelers} loading={isLoading} />
        <StatCard
          icon={Star}
          label="Avg rating"
          value={data ? data.averageRating.toFixed(1) : undefined}
          loading={isLoading}
        />
      </div>
    </div>
  );
}
