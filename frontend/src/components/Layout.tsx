import { useState, type ReactNode } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import {
  BarChart3,
  Briefcase,
  Compass,
  CreditCard,
  Flag,
  Globe2,
  LayoutDashboard,
  LogOut,
  Luggage,
  Menu,
  Tickets,
  Trophy,
  Users,
  X,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { hasRole, useAuth } from "@/lib/auth";
import { cn } from "@/lib/utils";

type NavItem = { to: string; label: string; icon: typeof Users; roles?: string[] };

const NAV: NavItem[] = [
  { to: "/discover", label: "Discover", icon: Compass },
  { to: "/my-trips", label: "My trips", icon: Luggage },
  { to: "/my-stats", label: "My stats", icon: BarChart3 },
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard, roles: ["MANAGER", "ADMIN"] },
  { to: "/travels", label: "Travels", icon: Globe2, roles: ["MANAGER", "ADMIN"] },
  { to: "/bookings", label: "Bookings", icon: Tickets, roles: ["ADMIN", "MANAGER", "VIEWER"] },
  { to: "/users", label: "Users", icon: Users, roles: ["ADMIN", "MANAGER", "VIEWER"] },
  { to: "/leaderboard", label: "Leaderboard", icon: Trophy, roles: ["ADMIN"] },
  { to: "/reports", label: "Reports", icon: Flag, roles: ["ADMIN"] },
];

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [open, setOpen] = useState(false);

  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 w-64 transform border-r bg-background transition-transform md:static md:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full md:translate-x-0",
        )}
      >
        <div className="flex h-16 items-center justify-between border-b px-6">
          <Link to="/" className="flex items-center gap-2 font-semibold">
            <Briefcase className="h-5 w-5" />
            <span>Travel Plan</span>
          </Link>
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setOpen(false)}
            aria-label="Close menu"
          >
            <X className="h-5 w-5" />
          </Button>
        </div>
        <nav className="flex flex-col gap-1 p-4">
          {NAV.filter((item) => !item.roles || hasRole(user, ...item.roles)).map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
                  )
                }
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </NavLink>
            );
          })}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-4 border-b bg-background/95 px-4 backdrop-blur md:px-6">
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setOpen(true)}
            aria-label="Open menu"
          >
            <Menu className="h-5 w-5" />
          </Button>
          <div className="hidden text-sm text-muted-foreground md:block">
            {pageTitle(location.pathname)}
          </div>
          <div className="ml-auto flex items-center gap-3">
            <div className="hidden text-right text-sm sm:block">
              <div className="font-medium">{user?.email}</div>
              <div className="text-xs text-muted-foreground">
                {user?.roles.join(", ")}
              </div>
            </div>
            <Button variant="outline" size="sm" onClick={logout}>
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </Button>
          </div>
        </header>

        <main className="flex-1 p-4 md:p-8">{children}</main>
      </div>

      {open && (
        <div
          className="fixed inset-0 z-30 bg-black/40 md:hidden"
          onClick={() => setOpen(false)}
        />
      )}
    </div>
  );
}

function pageTitle(pathname: string): string {
  if (pathname.startsWith("/discover")) return "Discover";
  if (pathname.startsWith("/my-trips")) return "My trips";
  if (pathname.startsWith("/my-stats")) return "My stats";
  if (pathname.startsWith("/dashboard")) return "Dashboard";
  if (pathname.startsWith("/leaderboard")) return "Leaderboard";
  if (pathname.startsWith("/reports")) return "Reports";
  if (pathname.startsWith("/users")) return "Users";
  if (pathname.startsWith("/travels")) return "Travels";
  if (pathname.startsWith("/bookings")) return "Bookings";
  if (pathname.startsWith("/payment-methods")) return "Payment methods";
  return "";
}

export { CreditCard };
