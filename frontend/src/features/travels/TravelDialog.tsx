import { useEffect } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, Plus, X } from "lucide-react";

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

import { useCreateTravel, useUpdateTravel } from "./api";
import {
  ACCOMMODATION_TYPES,
  TRANSPORTATION_TYPES,
  TRAVEL_STATUSES,
  type AccommodationType,
  type TransportationType,
  type TravelResponse,
  type TravelStatus,
} from "./types";

const schema = z.object({
  title: z.string().min(1, "Title is required").max(255),
  description: z.string().max(2000).optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  durationDays: z.coerce.number().int().positive().optional(),
  price: z.coerce.number().min(0).optional(),
  currency: z.string().length(3).optional().or(z.literal("")),
  status: z.enum(["DRAFT", "PUBLISHED", "ARCHIVED"]),
  destinations: z
    .array(
      z.object({
        name: z.string().min(1),
        country: z.string().optional(),
        latitude: z.coerce.number().optional(),
        longitude: z.coerce.number().optional(),
        order: z.coerce.number().int().positive(),
      }),
    )
    .default([]),
  activities: z
    .array(
      z.object({
        name: z.string().min(1),
        description: z.string().optional(),
        category: z.string().optional(),
        durationMinutes: z.coerce.number().int().nonnegative().optional(),
      }),
    )
    .default([]),
  accommodations: z
    .array(
      z.object({
        name: z.string().min(1),
        type: z.enum([
          "HOTEL",
          "HOSTEL",
          "APARTMENT",
          "BNB",
          "RESORT",
          "CAMPING",
          "OTHER",
        ]),
        address: z.string().optional(),
      }),
    )
    .default([]),
  transportations: z
    .array(
      z.object({
        type: z.enum(["FLIGHT", "TRAIN", "BUS", "CAR", "BOAT", "OTHER"]),
        provider: z.string().optional(),
        departureLocation: z.string().optional(),
        arrivalLocation: z.string().optional(),
        departureTime: z.string().optional(),
        arrivalTime: z.string().optional(),
      }),
    )
    .default([]),
});

type FormValues = z.infer<typeof schema>;

type Props = {
  open: boolean;
  onOpenChange(open: boolean): void;
  travel?: TravelResponse | null;
};

const emptyDefaults: FormValues = {
  title: "",
  description: "",
  startDate: "",
  endDate: "",
  durationDays: undefined,
  price: undefined,
  currency: "EUR",
  status: "DRAFT",
  destinations: [],
  activities: [],
  accommodations: [],
  transportations: [],
};

function toDefaults(travel?: TravelResponse | null): FormValues {
  if (!travel) return emptyDefaults;
  return {
    title: travel.title,
    description: travel.description ?? "",
    startDate: travel.startDate ?? "",
    endDate: travel.endDate ?? "",
    durationDays: travel.durationDays ?? undefined,
    price: travel.price ?? undefined,
    currency: travel.currency,
    status: travel.status,
    destinations: travel.destinations.map((d) => ({
      name: d.name,
      country: d.country ?? "",
      latitude: d.latitude ?? undefined,
      longitude: d.longitude ?? undefined,
      order: d.order,
    })),
    activities: travel.activities.map((a) => ({
      name: a.name,
      description: a.description ?? "",
      category: a.category ?? "",
      durationMinutes: a.durationMinutes ?? undefined,
    })),
    accommodations: travel.accommodations.map((a) => ({
      name: a.name,
      type: a.type,
      address: a.address ?? "",
    })),
    transportations: travel.transportations.map((t) => ({
      type: t.type,
      provider: t.provider ?? "",
      departureLocation: t.departureLocation ?? "",
      arrivalLocation: t.arrivalLocation ?? "",
      departureTime: t.departureTime ?? "",
      arrivalTime: t.arrivalTime ?? "",
    })),
  };
}

export function TravelDialog({ open, onOpenChange, travel }: Props) {
  const editing = Boolean(travel);
  const createMutation = useCreateTravel();
  const updateMutation = useUpdateTravel();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyDefaults,
  });

  const {
    register,
    handleSubmit,
    control,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting },
  } = form;

  useEffect(() => {
    if (open) {
      reset(toDefaults(travel));
    }
  }, [open, travel, reset]);

  const destinations = useFieldArray({ control, name: "destinations" });
  const activities = useFieldArray({ control, name: "activities" });
  const accommodations = useFieldArray({ control, name: "accommodations" });
  const transportations = useFieldArray({ control, name: "transportations" });

  const status = watch("status");

  const onSubmit = handleSubmit(async (values) => {
    const payload = {
      title: values.title,
      description: values.description || undefined,
      startDate: values.startDate || undefined,
      endDate: values.endDate || undefined,
      durationDays: values.durationDays,
      price: values.price,
      currency: values.currency || undefined,
      status: values.status,
      destinations: values.destinations,
      activities: values.activities,
      accommodations: values.accommodations,
      transportations: values.transportations,
    };
    if (editing && travel) {
      await updateMutation.mutateAsync({ id: travel.id, body: payload });
    } else {
      await createMutation.mutateAsync(payload);
    }
    onOpenChange(false);
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-3xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{editing ? "Edit travel" : "New travel"}</DialogTitle>
          <DialogDescription>
            Configure the trip metadata and its destinations, activities,
            accommodations and transportations. Sub-resources are replaced when
            you save.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-6" noValidate>
          <section className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input id="title" {...register("title")} />
              {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <textarea
                id="description"
                rows={3}
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                {...register("description")}
              />
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div className="space-y-2">
                <Label htmlFor="startDate">Start date</Label>
                <Input id="startDate" type="date" {...register("startDate")} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="endDate">End date</Label>
                <Input id="endDate" type="date" {...register("endDate")} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="durationDays">Duration (days)</Label>
                <Input id="durationDays" type="number" min={1} {...register("durationDays")} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="price">Price</Label>
                <Input id="price" type="number" step="0.01" min={0} {...register("price")} />
              </div>
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="currency">Currency (ISO 3)</Label>
                <Input id="currency" maxLength={3} {...register("currency")} />
              </div>
              <div className="space-y-2">
                <Label>Status</Label>
                <Select
                  value={status}
                  onValueChange={(v) => setValue("status", v as TravelStatus, { shouldDirty: true })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TRAVEL_STATUSES.map((s) => (
                      <SelectItem key={s} value={s}>
                        {s}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </section>

          <section className="space-y-3">
            <SectionHeader
              title="Destinations"
              onAdd={() =>
                destinations.append({
                  name: "",
                  country: "",
                  latitude: undefined,
                  longitude: undefined,
                  order: destinations.fields.length + 1,
                })
              }
            />
            {destinations.fields.map((field, index) => (
              <div
                key={field.id}
                className="grid grid-cols-1 gap-2 rounded-md border bg-muted/30 p-3 sm:grid-cols-5"
              >
                <Input placeholder="Name" {...register(`destinations.${index}.name` as const)} />
                <Input placeholder="Country" {...register(`destinations.${index}.country` as const)} />
                <Input
                  placeholder="Latitude"
                  type="number"
                  step="0.0001"
                  {...register(`destinations.${index}.latitude` as const)}
                />
                <Input
                  placeholder="Longitude"
                  type="number"
                  step="0.0001"
                  {...register(`destinations.${index}.longitude` as const)}
                />
                <div className="flex gap-2">
                  <Input
                    placeholder="Order"
                    type="number"
                    min={1}
                    {...register(`destinations.${index}.order` as const)}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => destinations.remove(index)}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </section>

          <section className="space-y-3">
            <SectionHeader
              title="Activities"
              onAdd={() => activities.append({ name: "", description: "", category: "", durationMinutes: undefined })}
            />
            {activities.fields.map((field, index) => (
              <div key={field.id} className="grid grid-cols-1 gap-2 rounded-md border bg-muted/30 p-3 sm:grid-cols-5">
                <Input placeholder="Name" {...register(`activities.${index}.name` as const)} />
                <Input placeholder="Category" {...register(`activities.${index}.category` as const)} />
                <Input
                  placeholder="Duration (min)"
                  type="number"
                  min={0}
                  {...register(`activities.${index}.durationMinutes` as const)}
                />
                <Input
                  placeholder="Description"
                  {...register(`activities.${index}.description` as const)}
                  className="sm:col-span-1"
                />
                <Button type="button" variant="ghost" size="icon" onClick={() => activities.remove(index)}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </section>

          <section className="space-y-3">
            <SectionHeader
              title="Accommodations"
              onAdd={() => accommodations.append({ name: "", type: "HOTEL", address: "" })}
            />
            {accommodations.fields.map((field, index) => (
              <div key={field.id} className="grid grid-cols-1 gap-2 rounded-md border bg-muted/30 p-3 sm:grid-cols-4">
                <Input placeholder="Name" {...register(`accommodations.${index}.name` as const)} />
                <Select
                  value={watch(`accommodations.${index}.type`) as AccommodationType}
                  onValueChange={(v) =>
                    setValue(`accommodations.${index}.type`, v as AccommodationType, { shouldDirty: true })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ACCOMMODATION_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>
                        {t}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Input placeholder="Address" {...register(`accommodations.${index}.address` as const)} />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => accommodations.remove(index)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </section>

          <section className="space-y-3">
            <SectionHeader
              title="Transportations"
              onAdd={() =>
                transportations.append({
                  type: "FLIGHT",
                  provider: "",
                  departureLocation: "",
                  arrivalLocation: "",
                  departureTime: "",
                  arrivalTime: "",
                })
              }
            />
            {transportations.fields.map((field, index) => (
              <div
                key={field.id}
                className="grid grid-cols-1 gap-2 rounded-md border bg-muted/30 p-3 sm:grid-cols-7"
              >
                <Select
                  value={watch(`transportations.${index}.type`) as TransportationType}
                  onValueChange={(v) =>
                    setValue(`transportations.${index}.type`, v as TransportationType, { shouldDirty: true })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TRANSPORTATION_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>
                        {t}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Input placeholder="Provider" {...register(`transportations.${index}.provider` as const)} />
                <Input placeholder="From" {...register(`transportations.${index}.departureLocation` as const)} />
                <Input placeholder="To" {...register(`transportations.${index}.arrivalLocation` as const)} />
                <Input
                  placeholder="Departure"
                  type="datetime-local"
                  {...register(`transportations.${index}.departureTime` as const)}
                />
                <Input
                  placeholder="Arrival"
                  type="datetime-local"
                  {...register(`transportations.${index}.arrivalTime` as const)}
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => transportations.remove(index)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </section>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {editing ? "Save changes" : "Create travel"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function SectionHeader({ title, onAdd }: { title: string; onAdd(): void }) {
  return (
    <div className="flex items-center justify-between">
      <h3 className="text-sm font-semibold">{title}</h3>
      <Button type="button" size="sm" variant="outline" onClick={onAdd}>
        <Plus className="mr-2 h-4 w-4" />
        Add
      </Button>
    </div>
  );
}
