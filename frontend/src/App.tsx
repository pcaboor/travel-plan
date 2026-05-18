import { Navigate, Route, Routes } from "react-router-dom";

import { Layout } from "@/components/Layout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { LoginPage } from "@/pages/LoginPage";
import { UsersPage } from "@/pages/UsersPage";
import { PaymentMethodsPage } from "@/pages/PaymentMethodsPage";
import { TravelsPage } from "@/pages/TravelsPage";
import { BookingsPage } from "@/pages/BookingsPage";

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
                <Route path="/" element={<Navigate to="/users" replace />} />
                <Route path="/users" element={<UsersPage />} />
                <Route
                  path="/users/:userId/payment-methods"
                  element={<PaymentMethodsPage />}
                />
                <Route path="/travels/*" element={<TravelsPage />} />
                <Route path="/bookings/*" element={<BookingsPage />} />
                <Route path="*" element={<Navigate to="/users" replace />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
