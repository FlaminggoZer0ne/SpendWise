import { NavLink } from "react-router-dom";
import {
  ArrowLeftRight,
  BarChart3,
  FolderCog,
  LayoutDashboard,
  PieChart,
  Tags,
  Target,
  UserCircle,
  Users,
  Wallet,
  X,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";

const MENU = {
  DASHBOARD: { label: "Dashboard", icon: LayoutDashboard, path: "/dashboard" },
  TRANSACTIONS: { label: "Transaksi", icon: ArrowLeftRight, path: "/transactions" },
  WALLETS: { label: "Dompet", icon: Wallet, path: "/wallets" },
  CATEGORIES: { label: "Kategori", icon: Tags, path: "/categories" },
  BUDGETS: { label: "Anggaran", icon: PieChart, path: "/budgets" },
  GOALS: { label: "Tabungan", icon: Target, path: "/goals" },
  REPORTS: { label: "Laporan", icon: BarChart3, path: "/reports" },
  USERS: { label: "Kelola Pengguna", icon: Users, path: "/admin/users" },
  DEFAULT_CATEGORIES: { label: "Kategori Default", icon: FolderCog, path: "/admin/categories" },
  PROFILE: { label: "Profil", icon: UserCircle, path: "/profile" },
};

export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = (user?.menuAccess ?? []).map((code) => MENU[code]).filter(Boolean);

  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-slate-900/40 md:hidden" onClick={onClose} aria-hidden="true" />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-60 flex-col border-r border-slate-200 bg-white transition-transform md:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}
      >
        <div className="flex h-16 items-center justify-between px-5">
          <div className="flex items-center gap-2">
            <img src="/logo.png" alt="" className="h-8 w-8 rounded-lg object-contain" />
            <span className="text-lg font-bold text-slate-900">SpendWise</span>
          </div>
          <button type="button" onClick={onClose} aria-label="Tutup menu" className="rounded p-1 text-slate-400 md:hidden">
            <X size={18} />
          </button>
        </div>
        <nav className="flex-1 space-y-1 px-3 py-2">
          {items.map(({ label, icon: Icon, path }) => (
            <NavLink
              key={path}
              to={path}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium ${
                  isActive ? "bg-emerald-50 text-emerald-700" : "text-slate-600 hover:bg-slate-100"
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
