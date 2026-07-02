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
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";

import { useCreateFeedback } from "./api";

const schema = z.object({
  rating: z.number().min(1).max(5),
  comment: z.string().max(2000).optional(),
});

type FormValues = z.infer<typeof schema>;

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  travelId: string;
  travelTitle?: string | null;
};

export function FeedbackDialog({ open, onOpenChange, travelId, travelTitle }: Props) {
  const mutation = useCreateFeedback();
  const {
    handleSubmit,
    register,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { rating: 5, comment: "" },
  });

  useEffect(() => {
    if (open) reset({ rating: 5, comment: "" });
  }, [open, reset]);

  const rating = watch("rating");

  const onSubmit = handleSubmit(async (values) => {
    await mutation.mutateAsync({
      travelId,
      rating: values.rating,
      comment: values.comment || undefined,
    });
    onOpenChange(false);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Leave feedback</DialogTitle>
          <DialogDescription>
            Rate {travelTitle ? `“${travelTitle}”` : "this trip"} and share your experience.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label>Rating</Label>
            <Select
              value={String(rating)}
              onValueChange={(v) => setValue("rating", Number(v), { shouldDirty: true })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {[1, 2, 3, 4, 5].map((n) => (
                  <SelectItem key={n} value={String(n)}>
                    {"★".repeat(n)} ({n})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="comment">Comment</Label>
            <Textarea
              id="comment"
              rows={4}
              placeholder="What did you enjoy? Anything to improve?"
              {...register("comment")}
            />
            {errors.comment && (
              <p className="text-xs text-destructive">{errors.comment.message}</p>
            )}
          </div>
          {mutation.isError && (
            <p className="text-sm text-destructive">
              {mutation.error instanceof Error ? mutation.error.message : "Failed to submit feedback"}
            </p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Submit
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
