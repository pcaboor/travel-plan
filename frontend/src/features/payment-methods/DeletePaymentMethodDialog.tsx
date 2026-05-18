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

import { useDeletePaymentMethod } from "./api";
import type { PaymentMethodResponse } from "./types";

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  userId: string;
  method: PaymentMethodResponse | null;
};

export function DeletePaymentMethodDialog({ open, onOpenChange, userId, method }: Props) {
  const mutation = useDeletePaymentMethod(userId);

  if (!method) return null;

  const submit = async () => {
    await mutation.mutateAsync(method.id);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Delete payment method?</DialogTitle>
          <DialogDescription>
            The {method.provider} {method.type.toLowerCase()} ending in{" "}
            <strong>{method.lastFour ?? "—"}</strong> will be removed. Past bookings
            keep their history but lose the payment link.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={submit} disabled={mutation.isPending}>
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Delete
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
