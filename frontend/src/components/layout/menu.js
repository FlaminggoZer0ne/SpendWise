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
} from "lucide-react";

/** Kode menu dikirim backend lewat `user.menuAccess`; `short` = label ringkas untuk bottom nav ponsel. */
export const MENU = {
  DASHBOARD: { label: "Dashboard", short: "Beranda", icon: LayoutDashboard, path: "/dashboard" },
  TRANSACTIONS: { label: "Transaksi", short: "Transaksi", icon: ArrowLeftRight, path: "/transactions" },
  WALLETS: { label: "Dompet", short: "Dompet", icon: Wallet, path: "/wallets" },
  CATEGORIES: { label: "Kategori", short: "Kategori", icon: Tags, path: "/categories" },
  BUDGETS: { label: "Anggaran", short: "Anggaran", icon: PieChart, path: "/budgets" },
  GOALS: { label: "Tabungan", short: "Tabungan", icon: Target, path: "/goals" },
  REPORTS: { label: "Laporan", short: "Laporan", icon: BarChart3, path: "/reports" },
  USERS: { label: "Kelola Pengguna", short: "Pengguna", icon: Users, path: "/admin/users" },
  DEFAULT_CATEGORIES: { label: "Kategori Default", short: "Kategori", icon: FolderCog, path: "/admin/categories" },
  PROFILE: { label: "Profil", short: "Profil", icon: UserCircle, path: "/profile" },
};

/** Tab utama di bottom nav (maks. 5); menu lain tetap lewat drawer. */
export const BOTTOM_TABS = {
  MEMBER: ["DASHBOARD", "TRANSACTIONS", "WALLETS", "BUDGETS", "REPORTS"],
  ADMIN: ["USERS", "DEFAULT_CATEGORIES", "PROFILE"],
};
