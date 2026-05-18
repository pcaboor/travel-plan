export type UserStatus = "ACTIVE" | "SUSPENDED" | "DELETED";

export type UserResponse = {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  status: UserStatus;
  roles: string[];
  createdAt: string;
  updatedAt: string;
};

export type UserPage = {
  content: UserResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type UserCreateInput = {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  status?: UserStatus;
  roles?: string[];
};

export type UserUpdateInput = {
  firstName?: string;
  lastName?: string;
  status?: UserStatus;
};

export const ALL_ROLES = ["ADMIN", "MANAGER", "VIEWER", "USER"] as const;
export const STATUSES: UserStatus[] = ["ACTIVE", "SUSPENDED", "DELETED"];
