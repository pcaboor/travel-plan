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
import { Textarea } from "@/components/ui/textarea";

import { useCreateReport } from "./api";
import { REPORT_TARGET_TYPES, type ReportTargetType } from "./types";

const schema = z.object({
  targetType: z.enum(["TRAVEL", "MANAGER", "TRAVELER"]),
  targetId: z.string().uuid("Must be a valid identifier"),
  reason: z.string().min(1, "Please describe the issue").max(2000),
});

type FormValues = z.infer<typeof schema>;

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  defaultTargetType?: ReportTargetType;
  defaultTargetId?: string;
  contextLabel?: string;
};

export function ReportDialog({
  open,
  onOpenChange,
  defaultTargetType = "TRAVEL",
  defaultTargetId = "",
  contextLabel,
}: Props) {
  const mutation = useCreateReport();
  const {
    handleSubmit,
    register,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { targetType: defaultTargetType, targetId: defaultTargetId, reason: "" },
  });

  useEffect(() => {
    if (open) {
      reset({ targetType: defaultTargetType, targetId: defaultTargetId, reason: "" });
    }
  }, [open, defaultTargetType, defaultTargetId, reset]);

  const targetType = watch("targetType");

  const onSubmit = handleSubmit(async (values) => {
    await mutation.mutateAsync(values);
    onOpenChange(false);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Report an issue</DialogTitle>
          <DialogDescription>
            {contextLabel
              ? `Flag a problem with ${contextLabel} for the moderation team.`
              : "Flag a travel, manager or traveler for the moderation team."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label>Target type</Label>
              <Select
                value={targetType}
                onValueChange={(v) =>
                  setValue("targetType", v as ReportTargetType, { shouldDirty: true })
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {REPORT_TARGET_TYPES.map((t) => (
                    <SelectItem key={t} value={t}>
                      {t}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="targetId">Target ID</Label>
              <Input id="targetId" placeholder="UUID" {...register("targetId")} />
              {errors.targetId && (
                <p className="text-xs text-destructive">{errors.targetId.message}</p>
              )}
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="reason">Reason</Label>
            <Textarea
              id="reason"
              rows={4}
              placeholder="Describe what went wrong…"
              {...register("reason")}
            />
            {errors.reason && (
              <p className="text-xs text-destructive">{errors.reason.message}</p>
            )}
          </div>
          {mutation.isError && (
            <p className="text-sm text-destructive">
              {mutation.error instanceof Error ? mutation.error.message : "Failed to submit report"}
            </p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Submit report
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
