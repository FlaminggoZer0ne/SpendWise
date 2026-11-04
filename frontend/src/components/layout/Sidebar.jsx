import { NavLink } from "react-router-dom";
import { X } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { MENU } from "./menu";

export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = (user?.menuAccess ?? []).map((code) => MENU[code]).filter(Boolean);

  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-slate-900/30 backdrop-blur-sm md:hidden" onClick={onClose} aria-hidden="true" />}
      <aside
        className={`glass-strong fixed inset-y-0 left-0 z-40 flex w-60 flex-col rounded-none border-y-0 border-l-0 transition-transform md:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}
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
                `flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-colors ${
                  isActive ? "bg-emerald-50/90 text-emerald-700 ring-1 ring-white/80" : "text-slate-600 hover:bg-white/60"
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
