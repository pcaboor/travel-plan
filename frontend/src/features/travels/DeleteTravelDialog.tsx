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

import { useDeleteTravel } from "./api";
import type { TravelResponse } from "./types";
import { apiFetch } from "@/lib/api";

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  travel: TravelResponse | null;
};

export function DeleteTravelDialog({ open, onOpenChange, travel }: Props) {
  const mutation = useDeleteTravel();

  if (!travel) return null;

  const submit = async () => {
    await mutation.mutateAsync(travel.id);
    try {
      await apiFetch(`/api/admin/bookings/cancel-by-travel/${travel.id}`, {
        method: "POST",
      });
    } catch {
      // The user is informed via the booking page if cancellation later fails.
    }
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Delete travel?</DialogTitle>
          <DialogDescription>
            <strong>{travel.title}</strong> and all its destinations, activities,
            accommodations and transportations will be removed. Existing bookings
            referencing this travel will be cancelled.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={submit} disabled={mutation.isPending}>
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Delete travel
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
