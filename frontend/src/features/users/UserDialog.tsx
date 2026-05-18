import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useCreateUser, useUpdateUser } from "./api";
import { ALL_ROLES, STATUSES, type UserResponse, type UserStatus } from "./types";

const createSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, "At least 8 characters"),
  firstName: z.string().max(100).optional(),
  lastName: z.string().max(100).optional(),
  status: z.enum(["ACTIVE", "SUSPENDED", "DELETED"]).optional(),
  roles: z.array(z.string()).optional(),
});

const editSchema = z.object({
  firstName: z.string().max(100).optional(),
  lastName: z.string().max(100).optional(),
  status: z.enum(["ACTIVE", "SUSPENDED", "DELETED"]).optional(),
});

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  user?: UserResponse | null;
};

export function UserDialog({ open, onOpenChange, user }: Props) {
  const editing = Boolean(user);
  const createMutation = useCreateUser();
  const updateMutation = useUpdateUser();

  type FormValues = z.infer<typeof createSchema>;
  const form = useForm<FormValues>({
    resolver: zodResolver(editing ? editSchema : createSchema),
    defaultValues: editing
      ? {
          email: user?.email ?? "",
          password: "",
          firstName: user?.firstName ?? "",
          lastName: user?.lastName ?? "",
          status: user?.status ?? "ACTIVE",
          roles: user?.roles ?? [],
        }
      : { email: "", password: "", firstName: "", lastName: "", status: "ACTIVE", roles: [] },
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = form;

  useEffect(() => {
    if (open) {
      reset(
        editing
          ? {
              email: user?.email ?? "",
              password: "",
              firstName: user?.firstName ?? "",
              lastName: user?.lastName ?? "",
              status: user?.status ?? "ACTIVE",
              roles: user?.roles ?? [],
            }
          : { email: "", password: "", firstName: "", lastName: "", status: "ACTIVE", roles: [] },
      );
    }
  }, [open, editing, user, reset]);

  const selectedRoles = watch("roles") ?? [];
  const selectedStatus = (watch("status") ?? "ACTIVE") as UserStatus;

  const onSubmit = handleSubmit(async (values) => {
    try {
      if (editing && user) {
        await updateMutation.mutateAsync({
          id: user.id,
          body: {
            firstName: values.firstName || undefined,
            lastName: values.lastName || undefined,
            status: values.status,
          },
        });
      } else {
        await createMutation.mutateAsync({
          email: values.email,
          password: values.password!,
          firstName: values.firstName || undefined,
          lastName: values.lastName || undefined,
          status: values.status,
          roles: values.roles && values.roles.length > 0 ? values.roles : undefined,
        });
      }
      onOpenChange(false);
    } catch {
      // Error handling could be improved with a toast; left silent for brevity.
    }
  });

  const toggleRole = (role: string) => {
    const next = selectedRoles.includes(role)
      ? selectedRoles.filter((r) => r !== role)
      : [...selectedRoles, role];
    setValue("roles", next, { shouldDirty: true });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{editing ? "Edit user" : "New user"}</DialogTitle>
          <DialogDescription>
            {editing
              ? "Update profile and status. Password and roles are managed separately."
              : "Create a user account with initial roles."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          {!editing && (
            <>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" {...register("email")} />
                {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input id="password" type="password" {...register("password")} />
                {errors.password && (
                  <p className="text-xs text-destructive">{errors.password.message}</p>
                )}
              </div>
            </>
          )}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="firstName">First name</Label>
              <Input id="firstName" {...register("firstName")} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lastName">Last name</Label>
              <Input id="lastName" {...register("lastName")} />
            </div>
          </div>

          <div className="space-y-2">
            <Label>Status</Label>
            <Select
              value={selectedStatus}
              onValueChange={(value) => setValue("status", value as UserStatus, { shouldDirty: true })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>
                    {s}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {!editing && (
            <div className="space-y-2">
              <Label>Roles</Label>
              <div className="flex flex-wrap gap-2">
                {ALL_ROLES.map((role) => {
                  const active = selectedRoles.includes(role);
                  return (
                    <button
                      type="button"
                      key={role}
                      onClick={() => toggleRole(role)}
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
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {editing ? "Save changes" : "Create user"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
