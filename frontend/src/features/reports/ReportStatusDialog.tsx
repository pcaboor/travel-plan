import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useUpdateReportStatus } from "./api";
import { REPORT_STATUSES, type ReportResponse, type ReportStatus } from "./types";

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  report: ReportResponse | null;
};

export function ReportStatusDialog({ open, onOpenChange, report }: Props) {
  const mutation = useUpdateReportStatus();
  const [status, setStatus] = useState<ReportStatus>("REVIEWED");

  useEffect(() => {
    if (report) setStatus(report.status);
  }, [report]);

  if (!report) return null;

  const submit = async () => {
    await mutation.mutateAsync({ id: report.id, status });
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Review report</DialogTitle>
          <DialogDescription>
            {report.targetType} · <span className="font-mono text-xs">{report.targetId}</span>
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="rounded-md border bg-muted/40 px-3 py-2 text-sm">{report.reason}</div>
          <div className="space-y-2">
            <Label>Status</Label>
            <Select value={status} onValueChange={(v) => setStatus(v as ReportStatus)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {REPORT_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>
                    {s}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {mutation.isError && (
            <p className="text-sm text-destructive">
              {mutation.error instanceof Error ? mutation.error.message : "Failed to update report"}
            </p>
          )}
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={submit} disabled={mutation.isPending}>
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
