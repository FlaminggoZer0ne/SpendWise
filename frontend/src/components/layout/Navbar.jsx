import { useLocation, useNavigate } from "react-router-dom";
import { LogOut, Menu } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import NotificationBell from "../NotificationBell";

const TITLES = {
  "/dashboard": "Dashboard",
  "/transactions": "Transaksi",
  "/wallets": "Dompet",
  "/categories": "Kategori",
  "/budgets": "Anggaran",
  "/goals": "Target Tabungan",
  "/reports": "Laporan",
  "/profile": "Profil",
  "/admin/users": "Kelola Pengguna",
  "/admin/categories": "Kategori Default",
};

export default function Navbar({ onMenu }) {
  const { user, logout } = useAuth();
  const { pathname } = useLocation();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 md:px-8">
      <div className="flex items-center gap-3">
        <button type="button" onClick={onMenu} aria-label="Buka menu" className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 md:hidden">
          <Menu size={20} />
        </button>
        <h1 className="text-lg font-semibold">{TITLES[pathname] ?? "SpendWise"}</h1>
      </div>
      <div className="flex items-center gap-2">
        {user?.role === "MEMBER" && <NotificationBell />}
        <span className="hidden text-sm text-slate-600 sm:inline">{user?.fullName}</span>
        <button
          type="button"
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
        >
          <LogOut size={16} />
          <span className="hidden sm:inline">Keluar</span>
        </button>
      </div>
    </header>
  );
}
