import { Navigate, Outlet } from "react-router-dom";
import { homePath, useAuth } from "../context/AuthContext";
import { Spinner } from "./ui/Feedback";

/** `role` opsional: tanpa role, cukup login. Role tidak cocok → beranda role pengguna. */
export default function ProtectedRoute({ role }) {
  const { user, loading } = useAuth();
  if (loading) return <Spinner />;
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to={homePath(user)} replace />;
  return <Outlet />;
}
