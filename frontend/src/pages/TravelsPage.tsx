import { useState } from "react";
import { Plus, Trash2 } from "lucide-react";

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

import { useTravels } from "@/features/travels/api";
import { TravelDialog } from "@/features/travels/TravelDialog";
import { DeleteTravelDialog } from "@/features/travels/DeleteTravelDialog";
import type { TravelResponse } from "@/features/travels/types";
import { hasRole, useAuth } from "@/lib/auth";

export function TravelsPage() {
  const { user } = useAuth();
  const canWrite = hasRole(user, "ADMIN", "MANAGER");
  const canDelete = hasRole(user, "ADMIN");
  const [page, setPage] = useState(0);
  const { data, isLoading, isFetching, error } = useTravels(page, 25);

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<TravelResponse | null>(null);
  const [deleting, setDeleting] = useState<TravelResponse | null>(null);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Travels</h1>
          <p className="text-sm text-muted-foreground">
            {data?.totalElements ?? 0} total travels
          </p>
        </div>
        {canWrite && (
          <Button onClick={() => setCreating(true)}>
            <Plus className="mr-2 h-4 w-4" />
            New travel
          </Button>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Catalog</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error instanceof Error ? error.message : "Failed to load travels"}
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Title</TableHead>
                <TableHead>Dates</TableHead>
                <TableHead>Duration</TableHead>
                <TableHead>Price</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Stops</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground">
                    Loading…
                  </TableCell>
                </TableRow>
              )}
              {!isLoading && data?.content.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-muted-foreground">
                    No travels yet.
                  </TableCell>
                </TableRow>
              )}
              {data?.content.map((travel) => (
                <TableRow key={travel.id}>
                  <TableCell className="font-medium">{travel.title}</TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {travel.startDate && travel.endDate
                      ? `${travel.startDate} → ${travel.endDate}`
                      : "—"}
                  </TableCell>
                  <TableCell>{travel.durationDays ?? "—"} d</TableCell>
                  <TableCell>
                    {travel.price != null ? `${travel.price} ${travel.currency}` : "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={travel.status === "PUBLISHED" ? "default" : "secondary"}>
                      {travel.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-sm">
                    {travel.destinations.length} dest. · {travel.activities.length} act.
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      {canWrite && (
                        <Button variant="ghost" size="sm" onClick={() => setEditing(travel)}>
                          Edit
                        </Button>
                      )}
                      {canDelete && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setDeleting(travel)}
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      )}
                    </div>
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

      <TravelDialog open={creating} onOpenChange={setCreating} />
      <TravelDialog
        open={Boolean(editing)}
        onOpenChange={(o) => !o && setEditing(null)}
        travel={editing}
      />
      <DeleteTravelDialog
        open={Boolean(deleting)}
        onOpenChange={(o) => !o && setDeleting(null)}
        travel={deleting}
      />
    </div>
  );
}
