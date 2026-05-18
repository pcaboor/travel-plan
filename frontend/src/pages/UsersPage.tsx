import { useState } from "react";
import { Link } from "react-router-dom";
import { CreditCard, KeyRound, Plus, ShieldCheck, Trash2 } from "lucide-react";

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

import { useUsers } from "@/features/users/api";
import { UserDialog } from "@/features/users/UserDialog";
import { RolesDialog } from "@/features/users/RolesDialog";
import { PasswordDialog } from "@/features/users/PasswordDialog";
import { DeleteUserDialog } from "@/features/users/DeleteUserDialog";
import type { UserResponse } from "@/features/users/types";
import { hasRole, useAuth } from "@/lib/auth";

export function UsersPage() {
  const { user: currentUser } = useAuth();
  const isAdmin = hasRole(currentUser, "ADMIN");
  const [page, setPage] = useState(0);
  const size = 25;
  const { data, isLoading, isFetching, error } = useUsers(page, size);

  const [editing, setEditing] = useState<UserResponse | null>(null);
  const [creating, setCreating] = useState(false);
  const [rolesUser, setRolesUser] = useState<UserResponse | null>(null);
  const [passwordUser, setPasswordUser] = useState<UserResponse | null>(null);
  const [deletingUser, setDeletingUser] = useState<UserResponse | null>(null);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Users</h1>
          <p className="text-sm text-muted-foreground">
            {data?.totalElements ?? 0} total users
          </p>
        </div>
        {isAdmin && (
          <Button onClick={() => setCreating(true)}>
            <Plus className="mr-2 h-4 w-4" /> New user
          </Button>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">All users</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error instanceof Error ? error.message : "Failed to load users"}
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Email</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Roles</TableHead>
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
              {!isLoading && data?.content.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    No users yet.
                  </TableCell>
                </TableRow>
              )}
              {data?.content.map((user) => (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">{user.email}</TableCell>
                  <TableCell>
                    {[user.firstName, user.lastName].filter(Boolean).join(" ") || "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={user.status === "ACTIVE" ? "default" : "secondary"}>
                      {user.status}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {user.roles.map((role) => (
                        <Badge key={role} variant="outline">
                          {role}
                        </Badge>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        asChild
                        title="Payment methods"
                      >
                        <Link to={`/users/${user.id}/payment-methods`}>
                          <CreditCard className="h-4 w-4" />
                        </Link>
                      </Button>
                      {isAdmin && (
                        <>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setEditing(user)}
                            title="Edit"
                          >
                            Edit
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setRolesUser(user)}
                            title="Roles"
                          >
                            <ShieldCheck className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setPasswordUser(user)}
                            title="Reset password"
                          >
                            <KeyRound className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setDeletingUser(user)}
                            title="Delete"
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </>
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

      <UserDialog open={creating} onOpenChange={setCreating} />
      <UserDialog
        open={Boolean(editing)}
        onOpenChange={(o) => !o && setEditing(null)}
        user={editing}
      />
      <RolesDialog
        open={Boolean(rolesUser)}
        onOpenChange={(o) => !o && setRolesUser(null)}
        user={rolesUser}
      />
      <PasswordDialog
        open={Boolean(passwordUser)}
        onOpenChange={(o) => !o && setPasswordUser(null)}
        user={passwordUser}
      />
      <DeleteUserDialog
        open={Boolean(deletingUser)}
        onOpenChange={(o) => !o && setDeletingUser(null)}
        user={deletingUser}
      />
    </div>
  );
}
