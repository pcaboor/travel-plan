import { CalendarCheck, Flag, MessageSquare, XCircle } from "lucide-react";

import { StatCard } from "@/components/StatCard";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMyStats } from "@/features/stats/api";

export function MyStatsPage() {
  const { data, isLoading, error } = useMyStats();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">My stats</h1>
        <p className="text-sm text-muted-foreground">Your activity on Travel Plan.</p>
      </div>

      {error && (
        <div className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
          {error instanceof Error ? error.message : "Failed to load stats"}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon={CalendarCheck} label="Participations" value={data?.participations} loading={isLoading} />
        <StatCard icon={XCircle} label="Cancellations" value={data?.cancellations} loading={isLoading} />
        <StatCard icon={MessageSquare} label="Feedback given" value={data?.feedbackGiven} loading={isLoading} />
        <StatCard icon={Flag} label="Reports filed" value={data?.reportsFiled} loading={isLoading} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Preferred payment methods</CardTitle>
        </CardHeader>
        <CardContent>
          {data && data.paymentProviders.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {data.paymentProviders.map((p) => (
                <Badge key={p} variant="secondary">{p}</Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No payment method registered yet.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
