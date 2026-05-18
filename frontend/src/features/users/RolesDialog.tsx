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

import { useAssignRoles } from "./api";
import { ALL_ROLES, type UserResponse } from "./types";

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  user: UserResponse | null;
};

export function RolesDialog({ open, onOpenChange, user }: Props) {
  const [selected, setSelected] = useState<string[]>([]);
  const mutation = useAssignRoles();

  useEffect(() => {
    if (open && user) {
      setSelected(user.roles);
    }
  }, [open, user]);

  if (!user) return null;

  const toggle = (role: string) => {
    setSelected((prev) =>
      prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role],
    );
  };

  const submit = async () => {
    if (selected.length === 0) return;
    await mutation.mutateAsync({ id: user.id, roles: selected });
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Assign roles</DialogTitle>
          <DialogDescription>
            Replace the roles currently granted to <strong>{user.email}</strong>. At
            least one role must remain.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2">
          <Label>Roles</Label>
          <div className="flex flex-wrap gap-2">
            {ALL_ROLES.map((role) => {
              const active = selected.includes(role);
              return (
                <button
                  type="button"
                  key={role}
                  onClick={() => toggle(role)}
                  className={
                    "rounded-md border px-3 py-1 text-xs transition-colors " +
                    (active
                      ? "border-primary bg-primary text-primary-foreground"
                      : "border-input bg-background hover:bg-accent")
                  }
                >
                  {role}
                </button>
              );
            })}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={submit} disabled={mutation.isPending || selected.length === 0}>
            {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Save roles
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
