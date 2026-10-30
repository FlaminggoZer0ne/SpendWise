import { Navigate, Route, Routes } from "react-router-dom";
import { homePath, useAuth } from "./context/AuthContext";
import AppLayout from "./components/layout/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import { Spinner } from "./components/ui/Feedback";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import DashboardPage from "./pages/dashboard/DashboardPage";
import TransactionPage from "./pages/transactions/TransactionPage";
import WalletPage from "./pages/wallets/WalletPage";
import CategoryPage from "./pages/categories/CategoryPage";
import BudgetPage from "./pages/budgets/BudgetPage";
import SavingGoalPage from "./pages/goals/SavingGoalPage";
import ReportPage from "./pages/reports/ReportPage";
import ProfilePage from "./pages/profile/ProfilePage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import AdminCategoriesPage from "./pages/admin/AdminCategoriesPage";
import NotFoundPage from "./pages/NotFoundPage";

function Home() {
  const { user, loading } = useAuth();
  if (loading) return <Spinner />;
  return <Navigate to={user ? homePath(user) : "/login"} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<Home />} />

      <Route element={<ProtectedRoute role="MEMBER" />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/transactions" element={<TransactionPage />} />
          <Route path="/wallets" element={<WalletPage />} />
          <Route path="/categories" element={<CategoryPage />} />
          <Route path="/budgets" element={<BudgetPage />} />
          <Route path="/goals" element={<SavingGoalPage />} />
          <Route path="/reports" element={<ReportPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute role="ADMIN" />}>
        <Route element={<AppLayout />}>
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/categories" element={<AdminCategoriesPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
