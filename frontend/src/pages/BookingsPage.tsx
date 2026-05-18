import { useState } from "react";
import { Ban, Loader2 } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

import { useBookings, useCancelByTravel } from "@/features/bookings/api";
import type { BookingStatus } from "@/features/bookings/types";
import { hasRole, useAuth } from "@/lib/auth";

const variantFor = (status: BookingStatus) => {
  switch (status) {
    case "CONFIRMED":
    case "COMPLETED":
      return "default" as const;
    case "CANCELLED":
    case "REFUNDED":
      return "destructive" as const;
    default:
      return "secondary" as const;
  }
};

export function BookingsPage() {
  const { user } = useAuth();
  const canCancel = hasRole(user, "ADMIN", "MANAGER");
  const [page, setPage] = useState(0);
  const { data, isLoading, isFetching, error } = useBookings(page, 25);

  const [travelRef, setTravelRef] = useState("");
  const [cancelResult, setCancelResult] = useState<string | null>(null);
  const cancelMutation = useCancelByTravel();

  const submitCancel = async () => {
    setCancelResult(null);
    if (!travelRef) return;
    try {
      const res = await cancelMutation.mutateAsync(travelRef.trim());
      setCancelResult(`${res.cancelled} booking(s) cancelled.`);
      setTravelRef("");
    } catch (err) {
      setCancelResult(err instanceof Error ? err.message : "Cancellation failed");
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Bookings</h1>
        <p className="text-sm text-muted-foreground">
          {data?.totalElements ?? 0} total bookings · read-only
        </p>
      </div>

      {canCancel && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Cancel bookings by travel</CardTitle>
            <CardDescription>
              Set all bookings for a given travel reference to CANCELLED. Used when
              a travel is deleted from the catalog.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
              <div className="flex-1 space-y-2">
                <Label htmlFor="travelRef">Travel ID</Label>
                <Input
                  id="travelRef"
                  placeholder="Travel UUID"
                  value={travelRef}
                  onChange={(e) => setTravelRef(e.target.value)}
                />
              </div>
              <Button
                onClick={submitCancel}
                disabled={!travelRef || cancelMutation.isPending}
              >
                {cancelMutation.isPending ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <Ban className="mr-2 h-4 w-4" />
                )}
                Cancel matching bookings
              </Button>
            </div>
            {cancelResult && (
              <p className="text-sm text-muted-foreground">{cancelResult}</p>
            )}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">All bookings</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error instanceof Error ? error.message : "Failed to load bookings"}
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Travel ref</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
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
              {!isLoading && data?.content.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    No bookings yet.
                  </TableCell>
                </TableRow>
              )}
              {data?.content.map((booking) => (
                <TableRow key={booking.id}>
                  <TableCell className="font-mono text-xs">{booking.userId}</TableCell>
                  <TableCell className="font-mono text-xs">{booking.travelRefId}</TableCell>
                  <TableCell>
                    {booking.amount} {booking.currency}
                  </TableCell>
                  <TableCell>
                    <Badge variant={variantFor(booking.status)}>{booking.status}</Badge>
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {new Date(booking.createdAt).toLocaleString()}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          <div className="flex items-center justify-between pt-4 text-sm text-muted-foreground">
            <span>
              Page {data ? data.number + 1 : 1} of {data?.totalPages ?? 1}
              {isFetching && " · loading…"}
            </span>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={!data || data.number + 1 >= data.totalPages}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
