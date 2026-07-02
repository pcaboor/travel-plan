import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useReports } from "@/features/reports/api";
import { ReportStatusDialog } from "@/features/reports/ReportStatusDialog";
import {
  REPORT_STATUSES,
  type ReportResponse,
  type ReportStatus,
} from "@/features/reports/types";

function statusVariant(status: ReportStatus): "default" | "secondary" | "destructive" | "outline" {
  switch (status) {
    case "OPEN":
      return "destructive";
    case "ACTIONED":
      return "default";
    case "DISMISSED":
      return "outline";
    default:
      return "secondary";
  }
}

export function ReportsPage() {
  const [filter, setFilter] = useState<ReportStatus | "">("");
  const { data, isLoading, error } = useReports(filter);
  const [reviewing, setReviewing] = useState<ReportResponse | null>(null);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Reports</h1>
          <p className="text-sm text-muted-foreground">
            Review traveler reports and moderate flagged content.
          </p>
        </div>
        <Select
          value={filter || "ALL"}
          onValueChange={(v) => setFilter(v === "ALL" ? "" : (v as ReportStatus))}
        >
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All statuses</SelectItem>
            {REPORT_STATUSES.map((s) => (
              <SelectItem key={s} value={s}>
                {s}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {error && (
        <div className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
          {error instanceof Error ? error.message : "Failed to load reports"}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Filed reports</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Target</TableHead>
                <TableHead>Reason</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-muted-foreground">
                    Loading…
                  </TableCell>
                </TableRow>
              )}
              {!isLoading && (!data || data.length === 0) && (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-muted-foreground">
                    No reports for this filter.
                  </TableCell>
                </TableRow>
              )}
              {data?.map((r) => (
                <TableRow key={r.id}>
                  <TableCell>
                    <div className="font-medium">{r.targetType}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      {r.targetId.slice(0, 8)}
                    </div>
                  </TableCell>
                  <TableCell className="max-w-md truncate">{r.reason}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(r.status)}>{r.status}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => setReviewing(r)}>
                      Review
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <ReportStatusDialog
        open={Boolean(reviewing)}
        onOpenChange={(o) => !o && setReviewing(null)}
        report={reviewing}
      />
    </div>
  );
}
