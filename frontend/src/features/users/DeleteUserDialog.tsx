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

import { useDeleteUser } from "./api";
import type { UserResponse } from "./types";

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  user: UserResponse | null;
};

export function DeleteUserDialog({ open, onOpenChange, user }: Props) {
  const mutation = useDeleteUser();

  if (!user) return null;

  const submit = async () => {
    await mutation.mutateAsync(user.id);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Delete user?</DialogTitle>
          <DialogDescription>
            <strong>{user.email}</strong> will be permanently deleted along with all
            associated payment methods and bookings. This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={submit} disabled={mutation.isPending}>
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Delete user
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
