import { describe, expect, it, vi } from "vitest";
import { screen } from "@testing-library/react";

import type { ReportResponse } from "@/features/reports/types";
import { renderWithClient } from "@/test/utils";

const state = vi.hoisted(() => ({
  reports: { data: [] as ReportResponse[], isLoading: false, error: null as unknown },
}));

vi.mock("@/features/reports/api", () => ({
  useReports: () => state.reports,
  useUpdateReportStatus: () => ({ mutateAsync: vi.fn(), isPending: false }),
}));

import { ReportsPage } from "./ReportsPage";

describe("ReportsPage", () => {
  it("shows the empty state for the current filter", () => {
    state.reports = { data: [], isLoading: false, error: null };
    renderWithClient(<ReportsPage />);

    expect(screen.getByRole("heading", { name: "Reports" })).toBeInTheDocument();
    expect(screen.getByText(/No reports for this filter/i)).toBeInTheDocument();
  });

  it("renders a filed report row with its status and a Review action", () => {
    state.reports = {
      data: [
        {
          id: "r1",
          reporterUserId: "u1",
          targetType: "TRAVEL",
          targetId: "1234567890ab",
          reason: "Misleading description",
          status: "OPEN",
          createdAt: "2026-06-01T10:00:00Z",
        },
      ],
      isLoading: false,
      error: null,
    };
    renderWithClient(<ReportsPage />);

    expect(screen.getByText("TRAVEL")).toBeInTheDocument();
    expect(screen.getByText("Misleading description")).toBeInTheDocument();
    expect(screen.getByText("OPEN")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Review/i })).toBeInTheDocument();
  });
});
