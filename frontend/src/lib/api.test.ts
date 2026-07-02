import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { apiFetch, getStoredToken, setStoredToken } from "./api";

function mockResponse(body: unknown, init: { status?: number; contentType?: string } = {}) {
  const status = init.status ?? 200;
  const contentType = init.contentType ?? "application/json";
  return {
    status,
    ok: status >= 200 && status < 300,
    statusText: "",
    headers: { get: (h: string) => (h.toLowerCase() === "content-type" ? contentType : null) },
    json: async () => body,
    text: async () => String(body),
  } as unknown as Response;
}

describe("apiFetch", () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    fetchMock.mockReset();
    window.sessionStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("attaches the bearer token and builds the query string", async () => {
    setStoredToken("tok-123");
    fetchMock.mockResolvedValueOnce(mockResponse([{ id: "1" }]));

    await apiFetch("/api/reports", { query: { status: "OPEN", empty: "" } });

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0]!;
    expect(url).toBe("/api/reports?status=OPEN");
    expect((options.headers as Record<string, string>).Authorization).toBe("Bearer tok-123");
  });

  it("serialises the JSON body and sets the content-type on writes", async () => {
    fetchMock.mockResolvedValueOnce(mockResponse({ ok: true }));

    await apiFetch("/api/feedback", { method: "POST", body: { rating: 5 } });

    const [, options] = fetchMock.mock.calls[0]!;
    expect(options.method).toBe("POST");
    expect(options.body).toBe(JSON.stringify({ rating: 5 }));
    expect((options.headers as Record<string, string>)["Content-Type"]).toBe("application/json");
  });

  it("maps a non-ok response to an ApiError carrying status and message", async () => {
    fetchMock.mockResolvedValueOnce(
      mockResponse({ message: "Payment not completed", error: "conflict" }, { status: 409 }),
    );

    await expect(apiFetch("/api/subscriptions/x/confirm", { method: "POST" })).rejects.toMatchObject({
      status: 409,
      message: "Payment not completed",
      code: "conflict",
    });
  });

  it("clears the stored token on a 401", async () => {
    setStoredToken("stale");
    fetchMock.mockResolvedValueOnce(mockResponse({ message: "nope" }, { status: 401 }));

    await expect(apiFetch("/api/auth/me")).rejects.toMatchObject({ status: 401 });
    expect(getStoredToken()).toBeNull();
  });

  it("returns undefined for a 204 No Content", async () => {
    fetchMock.mockResolvedValueOnce(mockResponse(null, { status: 204 }));

    const result = await apiFetch("/api/admin/users/1/payment-methods/2", { method: "DELETE" });
    expect(result).toBeUndefined();
  });
});
