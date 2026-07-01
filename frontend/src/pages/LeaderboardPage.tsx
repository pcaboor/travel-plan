import { Trophy } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useLeaderboard } from "@/features/stats/api";

export function LeaderboardPage() {
  const { data, isLoading, error } = useLeaderboard();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Manager leaderboard</h1>
        <p className="text-sm text-muted-foreground">
          Managers ranked by performance score (rating, income, activity, reports).
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Ranking</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error instanceof Error ? error.message : "Failed to load leaderboard"}
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead>
                <TableHead>Manager</TableHead>
                <TableHead>Score</TableHead>
                <TableHead>Income</TableHead>
                <TableHead>Trips</TableHead>
                <TableHead>Travelers</TableHead>
                <TableHead>Rating</TableHead>
                <TableHead>Reports</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-muted-foreground">
                    Loading…
                  </TableCell>
                </TableRow>
              )}
              {!isLoading && data?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-muted-foreground">
                    No managers with activity yet.
                  </TableCell>
                </TableRow>
              )}
              {data?.map((m, index) => (
                <TableRow key={m.managerId}>
                  <TableCell>
                    {index === 0 ? (
                      <Trophy className="h-4 w-4 text-yellow-500" />
                    ) : (
                      index + 1
                    )}
                  </TableCell>
                  <TableCell className="font-mono text-xs">{m.managerId}</TableCell>
                  <TableCell>
                    <Badge>{m.score.toFixed(2)}</Badge>
                  </TableCell>
                  <TableCell>{m.income} €</TableCell>
                  <TableCell>{m.trips}</TableCell>
                  <TableCell>{m.travelers}</TableCell>
                  <TableCell>{m.averageRating.toFixed(1)}</TableCell>
                  <TableCell>{m.reportCount}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
