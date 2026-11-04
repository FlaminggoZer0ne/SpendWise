import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { BOTTOM_TABS, MENU } from "./menu";

/** Pil kaca melayang di bawah layar (hanya ponsel); tab aktif disorot hijau lembut. */
export default function BottomNav() {
  const { user } = useAuth();
  const allowed = new Set(user?.menuAccess ?? []);
  const tabs = (BOTTOM_TABS[user?.role] ?? []).filter((code) => allowed.has(code)).map((code) => MENU[code]);
  if (tabs.length === 0) return null;

  return (
    <nav
      aria-label="Menu utama"
      className="glass-strong fixed inset-x-3 bottom-3 z-30 flex items-stretch justify-between gap-1 rounded-full p-2 pb-[max(0.5rem,env(safe-area-inset-bottom))] md:hidden"
    >
      {tabs.map(({ short, icon: Icon, path }) => (
        <NavLink
          key={path}
          to={path}
          className={({ isActive }) =>
            `flex min-w-0 flex-1 flex-col items-center gap-0.5 rounded-3xl px-1 py-1.5 text-[11px] font-medium transition-colors ${
              isActive
                ? "bg-emerald-50/95 text-emerald-700 ring-1 ring-white/80"
                : "text-slate-500 hover:text-slate-700"
            }`
          }
        >
          <Icon size={20} />
          <span className="truncate">{short}</span>
        </NavLink>
      ))}
    </nav>
  );
}
