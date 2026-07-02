import { expect, test } from "@playwright/test";

// Credentials default to the compose bootstrap admin (see .env.example). The
// admin inherits the traveler (USER) capability through the role hierarchy, so
// it can exercise both the traveler pages and the admin-only moderation page.
const EMAIL = process.env.E2E_ADMIN_EMAIL ?? "admin@travelplan.local";
const PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? "ChangeMe123!";

test.describe("Travel Plan end-to-end", () => {
  test("logs in, browses the V2 pages, searches and logs out", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Email").fill(EMAIL);
    await page.getByLabel("Password").fill(PASSWORD);
    await page.getByRole("button", { name: "Sign in" }).click();

    // Admin lands on the users page after authenticating.
    await expect(page).toHaveURL(/\/users/);

    // My trips — the traveler subscriptions page (empty for a fresh admin).
    await page.getByRole("link", { name: "My trips" }).click();
    await expect(page.getByRole("heading", { name: "My trips" })).toBeVisible();
    await expect(page.getByText(/No subscriptions yet/i)).toBeVisible();

    // Reports — admin-only moderation queue.
    await page.getByRole("link", { name: "Reports" }).click();
    await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();

    // Discover — search goes through the gateway to Elasticsearch.
    await page.getByRole("link", { name: "Discover" }).click();
    await page.getByPlaceholder(/Search by title/i).fill("mountain");
    await page.getByRole("button", { name: "Search" }).click();
    await expect(page.getByRole("heading", { name: /Results for/i })).toBeVisible();

    // Logout returns to the login screen.
    await page.getByRole("button", { name: "Logout" }).click();
    await expect(page).toHaveURL(/\/login/);
  });

  test("the new V2 endpoints answer through the gateway", async ({ request }) => {
    const login = await request.post("/api/auth/login", {
      data: { email: EMAIL, password: PASSWORD },
    });
    expect(login.ok()).toBeTruthy();
    const { accessToken } = await login.json();
    const headers = { Authorization: `Bearer ${accessToken}` };

    const subscriptions = await request.get("/api/subscriptions/me", { headers });
    expect(subscriptions.ok()).toBeTruthy();
    expect(Array.isArray(await subscriptions.json())).toBeTruthy();

    const reports = await request.get("/api/reports", { headers });
    expect(reports.ok()).toBeTruthy();
    expect(Array.isArray(await reports.json())).toBeTruthy();

    const stats = await request.get("/api/stats/me", { headers });
    expect(stats.ok()).toBeTruthy();
  });
});
