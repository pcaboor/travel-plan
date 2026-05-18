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

import {
  useCreatePaymentMethod,
  useUpdatePaymentMethod,
} from "./api";
import {
  PROVIDERS,
  STATUSES,
  TYPES,
  type PaymentMethodResponse,
  type PaymentMethodStatus,
  type PaymentMethodType,
  type PaymentProvider,
} from "./types";

const schema = z.object({
  provider: z.enum(["STRIPE", "PAYPAL"]),
  type: z.enum(["CARD", "BANK_ACCOUNT", "WALLET"]),
  providerToken: z.string().optional(),
  lastFour: z
    .string()
    .optional()
    .refine((v) => !v || /^\d{4}$/.test(v), "Must be 4 digits"),
  expiresAt: z.string().optional(),
  status: z.enum(["ACTIVE", "EXPIRED", "REVOKED"]).optional(),
});

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  userId: string;
  method?: PaymentMethodResponse | null;
};

type FormValues = z.infer<typeof schema>;

export function PaymentMethodDialog({ open, onOpenChange, userId, method }: Props) {
  const editing = Boolean(method);
  const createMutation = useCreatePaymentMethod(userId);
  const updateMutation = useUpdatePaymentMethod(userId);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      provider: "STRIPE",
      type: "CARD",
      providerToken: "",
      lastFour: "",
      expiresAt: "",
      status: "ACTIVE",
    },
  });

  useEffect(() => {
    if (open) {
      reset(
        method
          ? {
              provider: method.provider,
              type: method.type,
              providerToken: "",
              lastFour: method.lastFour ?? "",
              expiresAt: method.expiresAt ?? "",
              status: method.status,
            }
          : {
              provider: "STRIPE",
              type: "CARD",
              providerToken: "",
              lastFour: "",
              expiresAt: "",
              status: "ACTIVE",
            },
      );
    }
  }, [open, method, reset]);

  const provider = watch("provider");
  const type = watch("type");
  const status = watch("status") ?? "ACTIVE";

  const onSubmit = handleSubmit(async (values) => {
    if (editing && method) {
      await updateMutation.mutateAsync({
        id: method.id,
        body: {
          lastFour: values.lastFour || undefined,
          expiresAt: values.expiresAt || undefined,
          status: values.status,
        },
      });
    } else {
      await createMutation.mutateAsync({
        provider: values.provider,
        type: values.type,
        providerToken: values.providerToken || undefined,
        lastFour: values.lastFour || undefined,
        expiresAt: values.expiresAt || undefined,
        status: values.status,
      });
    }
    onOpenChange(false);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{editing ? "Edit payment method" : "New payment method"}</DialogTitle>
          <DialogDescription>
            {editing
              ? "Adjust card details, expiry or status. Provider and type are immutable."
              : "Register a payment method received from Stripe or PayPal tokenisation."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          {!editing && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Provider</Label>
                <Select
                  value={provider}
                  onValueChange={(v) => setValue("provider", v as PaymentProvider, { shouldDirty: true })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {PROVIDERS.map((p) => (
                      <SelectItem key={p} value={p}>
                        {p}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Type</Label>
                <Select
                  value={type}
                  onValueChange={(v) => setValue("type", v as PaymentMethodType, { shouldDirty: true })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TYPES.map((t) => (
                      <SelectItem key={t} value={t}>
                        {t.replace("_", " ")}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}
          {!editing && (
            <div className="space-y-2">
              <Label htmlFor="providerToken">Provider token</Label>
              <Input id="providerToken" placeholder="tok_… or paypal-billing-agreement"
                {...register("providerToken")} />
            </div>
          )}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="lastFour">Last 4 digits</Label>
              <Input
                id="lastFour"
                inputMode="numeric"
                maxLength={4}
                {...register("lastFour")}
              />
              {errors.lastFour && (
                <p className="text-xs text-destructive">{errors.lastFour.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="expiresAt">Expires</Label>
              <Input id="expiresAt" type="date" {...register("expiresAt")} />
            </div>
          </div>
          <div className="space-y-2">
            <Label>Status</Label>
            <Select
              value={status}
              onValueChange={(v) => setValue("status", v as PaymentMethodStatus, { shouldDirty: true })}
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
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {editing ? "Save changes" : "Create"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
