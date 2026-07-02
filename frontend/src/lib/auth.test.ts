import { describe, expect, it } from "vitest";

import { hasRole, type AuthUser } from "./auth";

const user = (roles: string[]): AuthUser => ({
  id: "u1",
  email: "u@example.com",
  firstName: null,
  lastName: null,
  roles,
});

describe("hasRole", () => {
  it("returns false for an anonymous (null) user", () => {
    expect(hasRole(null, "ADMIN")).toBe(false);
  });

  it("matches when the user holds one of the requested roles", () => {
    expect(hasRole(user(["ADMIN"]), "ADMIN")).toBe(true);
    expect(hasRole(user(["MANAGER"]), "MANAGER", "ADMIN")).toBe(true);
  });

  it("returns false when none of the requested roles are held", () => {
    expect(hasRole(user(["USER"]), "ADMIN")).toBe(false);
    expect(hasRole(user(["USER"]), "MANAGER", "ADMIN")).toBe(false);
  });
});
