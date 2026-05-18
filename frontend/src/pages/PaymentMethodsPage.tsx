import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";

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

import { usePaymentMethods } from "@/features/payment-methods/api";
import { PaymentMethodDialog } from "@/features/payment-methods/PaymentMethodDialog";
import { DeletePaymentMethodDialog } from "@/features/payment-methods/DeletePaymentMethodDialog";
import type { PaymentMethodResponse } from "@/features/payment-methods/types";
import { hasRole, useAuth } from "@/lib/auth";

export function PaymentMethodsPage() {
  const { userId = "" } = useParams<{ userId: string }>();
  const { user: currentUser } = useAuth();
  const isAdmin = hasRole(currentUser, "ADMIN");
  const { data, isLoading, error } = usePaymentMethods(userId);

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<PaymentMethodResponse | null>(null);
  const [deleting, setDeleting] = useState<PaymentMethodResponse | null>(null);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <Button variant="ghost" size="sm" asChild className="-ml-3">
            <Link to="/users">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to users
            </Link>
          </Button>
          <h1 className="text-2xl font-bold tracking-tight">Payment methods</h1>
          <p className="text-sm text-muted-foreground">
            For user <code className="rounded bg-muted px-1 py-0.5 text-xs">{userId}</code>
          </p>
        </div>
        {isAdmin && (
          <Button onClick={() => setCreating(true)}>
            <Plus className="mr-2 h-4 w-4" /> New payment method
          </Button>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Registered methods</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {error instanceof Error ? error.message : "Failed to load payment methods"}
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Provider</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Last 4</TableHead>
                <TableHead>Expires</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-muted-foreground">
                    Loading…
                  </TableCell>
                </TableRow>
              )}
              {!isLoading && (!data || data.length === 0) && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-muted-foreground">
                    No payment methods yet.
                  </TableCell>
                </TableRow>
              )}
              {data?.map((method) => (
                <TableRow key={method.id}>
                  <TableCell className="font-medium">{method.provider}</TableCell>
                  <TableCell>{method.type.replace("_", " ")}</TableCell>
                  <TableCell>{method.lastFour ?? "—"}</TableCell>
                  <TableCell>{method.expiresAt ?? "—"}</TableCell>
                  <TableCell>
                    <Badge variant={method.status === "ACTIVE" ? "default" : "secondary"}>
                      {method.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    {isAdmin && (
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="sm" onClick={() => setEditing(method)}>
                          Edit
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setDeleting(method)}
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <PaymentMethodDialog open={creating} onOpenChange={setCreating} userId={userId} />
      <PaymentMethodDialog
        open={Boolean(editing)}
        onOpenChange={(o) => !o && setEditing(null)}
        userId={userId}
        method={editing}
      />
      <DeletePaymentMethodDialog
        open={Boolean(deleting)}
        onOpenChange={(o) => !o && setDeleting(null)}
        userId={userId}
        method={deleting}
      />
    </div>
  );
}
