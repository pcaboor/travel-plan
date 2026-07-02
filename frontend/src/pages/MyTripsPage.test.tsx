import { describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";

import type { SubscriptionResponse } from "@/features/subscriptions/types";
import { renderWithClient } from "@/test/utils";

const state = vi.hoisted(() => ({
  subs: { data: [] as SubscriptionResponse[], isLoading: false, error: null as unknown },
}));

vi.mock("@/features/subscriptions/api", () => ({
  useMySubscriptions: () => state.subs,
  useUnsubscribe: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useConfirmSubscription: () => ({ mutateAsync: vi.fn(), isPending: false }),
}));

import { MyTripsPage } from "./MyTripsPage";

describe("MyTripsPage", () => {
  it("shows the empty state when there are no subscriptions", () => {
    state.subs = { data: [], isLoading: false, error: null };
    renderWithClient(<MyTripsPage />);

    expect(screen.getByRole("heading", { name: "My trips" })).toBeInTheDocument();
    expect(screen.getByText(/No subscriptions yet/i)).toBeInTheDocument();
  });

  it("renders a confirmed subscription with Feedback and Report actions but no Confirm", () => {
    state.subs = {
      data: [
        {
          bookingId: "b1",
          travelRefId: "1234567890ab",
          status: "CONFIRMED",
          amount: 200,
          currency: "EUR",
          travelStartDate: "2026-09-01",
        },
      ],
      isLoading: false,
      error: null,
    };
    renderWithClient(<MyTripsPage />);

    expect(screen.getByText("CONFIRMED")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Feedback/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Report/i })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Confirm/i })).not.toBeInTheDocument();
  });

  it("offers Confirm and Unsubscribe for a pending subscription", () => {
    state.subs = {
      data: [
        {
          bookingId: "b2",
          travelRefId: "abcdef012345",
          status: "PENDING",
          amount: 50,
          currency: "EUR",
          travelStartDate: null,
        },
      ],
      isLoading: false,
      error: null,
    };
    renderWithClient(<MyTripsPage />);

    expect(screen.getByRole("button", { name: /Confirm/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Unsubscribe/i })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Feedback/i })).not.toBeInTheDocument();
  });
});
