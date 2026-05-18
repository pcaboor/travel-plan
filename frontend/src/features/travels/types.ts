export type TravelStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
export type AccommodationType =
  | "HOTEL"
  | "HOSTEL"
  | "APARTMENT"
  | "BNB"
  | "RESORT"
  | "CAMPING"
  | "OTHER";
export type TransportationType = "FLIGHT" | "TRAIN" | "BUS" | "CAR" | "BOAT" | "OTHER";

export type DestinationInput = {
  name: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  order: number;
};

export type ActivityInput = {
  name: string;
  description?: string;
  category?: string;
  durationMinutes?: number;
};

export type AccommodationInput = {
  name: string;
  type: AccommodationType;
  address?: string;
};

export type TransportationInput = {
  type: TransportationType;
  provider?: string;
  departureLocation?: string;
  arrivalLocation?: string;
  departureTime?: string;
  arrivalTime?: string;
};

export type TravelMutationInput = {
  title?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  durationDays?: number;
  price?: number;
  currency?: string;
  status?: TravelStatus;
  destinations?: DestinationInput[];
  activities?: ActivityInput[];
  accommodations?: AccommodationInput[];
  transportations?: TransportationInput[];
};

export type TravelResponse = {
  id: string;
  title: string;
  description: string | null;
  startDate: string | null;
  endDate: string | null;
  durationDays: number | null;
  price: number | null;
  currency: string;
  status: TravelStatus;
  createdAt: string | null;
  updatedAt: string | null;
  destinations: Array<{
    id: string;
    name: string;
    country: string | null;
    latitude: number | null;
    longitude: number | null;
    order: number;
  }>;
  activities: Array<{
    id: string;
    name: string;
    description: string | null;
    category: string | null;
    durationMinutes: number | null;
  }>;
  accommodations: Array<{
    id: string;
    name: string;
    type: AccommodationType;
    address: string | null;
  }>;
  transportations: Array<{
    id: string;
    type: TransportationType;
    provider: string | null;
    departureLocation: string | null;
    arrivalLocation: string | null;
    departureTime: string | null;
    arrivalTime: string | null;
  }>;
};

export type TravelPage = {
  content: TravelResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export const TRAVEL_STATUSES: TravelStatus[] = ["DRAFT", "PUBLISHED", "ARCHIVED"];
export const ACCOMMODATION_TYPES: AccommodationType[] = [
  "HOTEL",
  "HOSTEL",
  "APARTMENT",
  "BNB",
  "RESORT",
  "CAMPING",
  "OTHER",
];
export const TRANSPORTATION_TYPES: TransportationType[] = [
  "FLIGHT",
  "TRAIN",
  "BUS",
  "CAR",
  "BOAT",
  "OTHER",
];
