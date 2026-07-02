import { defineConfig, devices } from "@playwright/test";

// E2E runs against the running docker stack (nginx gateway on 5443, self-signed
// TLS). Override the target with E2E_BASE_URL when pointing at another env.
const baseURL = process.env.E2E_BASE_URL ?? "https://localhost:5443";

export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: process.env.CI ? [["list"], ["html", { open: "never" }]] : "list",
  use: {
    baseURL,
    ignoreHTTPSErrors: true,
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
});
