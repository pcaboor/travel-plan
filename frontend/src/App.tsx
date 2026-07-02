import { Navigate, Route, Routes } from "react-router-dom";

import { Layout } from "@/components/Layout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { hasRole, useAuth } from "@/lib/auth";
import { BookingsPage } from "@/pages/BookingsPage";
import { DiscoverPage } from "@/pages/DiscoverPage";
import { LeaderboardPage } from "@/pages/LeaderboardPage";
import { LoginPage } from "@/pages/LoginPage";
import { ManagerDashboardPage } from "@/pages/ManagerDashboardPage";
import { MyStatsPage } from "@/pages/MyStatsPage";
import { MyTripsPage } from "@/pages/MyTripsPage";
import { PaymentMethodsPage } from "@/pages/PaymentMethodsPage";
import { ReportsPage } from "@/pages/ReportsPage";
import { TravelsPage } from "@/pages/TravelsPage";
import { UsersPage } from "@/pages/UsersPage";

function HomeRedirect() {
  const { user } = useAuth();
  if (hasRole(user, "ADMIN")) return <Navigate to="/users" replace />;
  if (hasRole(user, "MANAGER")) return <Navigate to="/dashboard" replace />;
  return <Navigate to="/discover" replace />;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="*"
        element={
          <ProtectedRoute>
            <Layout>
              <Routes>
                <Route path="/" element={<HomeRedirect />} />
                <Route path="/discover" element={<DiscoverPage />} />
                <Route path="/my-trips" element={<MyTripsPage />} />
                <Route path="/my-stats" element={<MyStatsPage />} />
                <Route path="/dashboard" element={<ManagerDashboardPage />} />
                <Route path="/leaderboard" element={<LeaderboardPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/users" element={<UsersPage />} />
                <Route
                  path="/users/:userId/payment-methods"
                  element={<PaymentMethodsPage />}
                />
                <Route path="/travels/*" element={<TravelsPage />} />
                <Route path="/bookings/*" element={<BookingsPage />} />
                <Route path="*" element={<HomeRedirect />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
