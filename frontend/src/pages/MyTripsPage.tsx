import { useState } from "react";
import { CheckCircle2, Flag, MessageSquare, XCircle } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { FeedbackDialog } from "@/features/feedback/FeedbackDialog";
import { ReportDialog } from "@/features/reports/ReportDialog";
import {
  useConfirmSubscription,
  useMySubscriptions,
  useUnsubscribe,
} from "@/features/subscriptions/api";
import type { SubscriptionResponse } from "@/features/subscriptions/types";

const ACTIVE = new Set(["PENDING", "CONFIRMED"]);
const FEEDBACK_OK = new Set(["CONFIRMED", "COMPLETED"]);

function statusVariant(status: string): "default" | "secondary" | "outline" {
  if (status === "CONFIRMED" || status === "COMPLETED") return "default";
  if (status === "PENDING") return "secondary";
  return "outline";
}

const shortId = (id: string) => id.slice(0, 8);

export function MyTripsPage() {
  const { data, isLoading, error } = useMySubscriptions();
  const unsubscribe = useUnsubscribe();
  const confirm = useConfirmSubscription();

  const [feedbackFor, setFeedbackFor] = useState<SubscriptionResponse | null>(null);
  const [reportFor, setReportFor] = useState<SubscriptionResponse | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const onUnsubscribe = async (s: SubscriptionResponse) => {
    setMessage(null);
    try {
      await unsubscribe.mutateAsync(s.travelRefId);
      setMessage("You have unsubscribed from this trip.");
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Unsubscribe failed");
    }
  };

  const onConfirm = async (s: SubscriptionResponse) => {
    setMessage(null);
    try {
      await confirm.mutateAsync(s.travelRefId);
      setMessage("Subscription confirmed.");
    } catch (err) {
      setMessage(
        err instanceof Error ? err.message : "Confirmation failed — complete the payment first.",
      );
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">My trips</h1>
        <p className="text-sm text-muted-foreground">
          Manage your subscriptions, confirm payment, leave feedback or report an issue.
        </p>
      </div>

      {error && (
        <div className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
          {error instanceof Error ? error.message : "Failed to load your trips"}
        </div>
      )}
      {message && (
        <div className="rounded-md border bg-muted/40 px-3 py-2 text-sm text-muted-foreground">
          {message}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Subscriptions</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Trip</TableHead>
                <TableHead>Departure</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    Loading…
                  </TableCell>
                </TableRow>
              )}
              {!isLoading && (!data || data.length === 0) && (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    No subscriptions yet — head to Discover to find a trip.
                  </TableCell>
                </TableRow>
              )}
              {data?.map((s) => (
                <TableRow key={s.bookingId}>
                  <TableCell className="font-mono text-xs">{shortId(s.travelRefId)}</TableCell>
                  <TableCell>{s.travelStartDate ?? "—"}</TableCell>
                  <TableCell>
                    {s.amount} {s.currency}
                  </TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(s.status)}>{s.status}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex flex-wrap justify-end gap-1">
                      {s.status === "PENDING" && (
                        <Button
                          variant="ghost"
                          size="sm"
                          disabled={confirm.isPending}
                          onClick={() => onConfirm(s)}
                        >
                          <CheckCircle2 className="mr-1 h-4 w-4" /> Confirm
                        </Button>
                      )}
                      {ACTIVE.has(s.status) && (
                        <Button
                          variant="ghost"
                          size="sm"
                          disabled={unsubscribe.isPending}
                          onClick={() => onUnsubscribe(s)}
                        >
                          <XCircle className="mr-1 h-4 w-4" /> Unsubscribe
                        </Button>
                      )}
                      {FEEDBACK_OK.has(s.status) && (
                        <Button variant="ghost" size="sm" onClick={() => setFeedbackFor(s)}>
                          <MessageSquare className="mr-1 h-4 w-4" /> Feedback
                        </Button>
                      )}
                      <Button variant="ghost" size="sm" onClick={() => setReportFor(s)}>
                        <Flag className="mr-1 h-4 w-4 text-destructive" /> Report
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <FeedbackDialog
        open={Boolean(feedbackFor)}
        onOpenChange={(o) => !o && setFeedbackFor(null)}
        travelId={feedbackFor?.travelRefId ?? ""}
      />
      <ReportDialog
        open={Boolean(reportFor)}
        onOpenChange={(o) => !o && setReportFor(null)}
        defaultTargetType="TRAVEL"
        defaultTargetId={reportFor?.travelRefId ?? ""}
        contextLabel={reportFor ? `trip ${shortId(reportFor.travelRefId)}` : undefined}
      />
    </div>
  );
}
