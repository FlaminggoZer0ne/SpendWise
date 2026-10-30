import { useCallback, useEffect, useRef, useState } from "react";
import { Bell } from "lucide-react";
import { getNotifications, markAllRead } from "../api/notificationApi";
import { onNotificationRefresh } from "../utils/events";
import { formatWaktu } from "../utils/format";

const POLL_MS = 60_000;
const LEVEL_STYLE = { WARNING: "border-amber-400 bg-amber-50", DANGER: "border-red-500 bg-red-50" };

export default function NotificationBell() {
  const [state, setState] = useState({ unreadCount: 0, items: [] });
  const [open, setOpen] = useState(false);
  const box = useRef(null);

  const load = useCallback(() => {
    getNotifications()
      .then((res) => setState(res.data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    load();
    const timer = setInterval(load, POLL_MS);
    const off = onNotificationRefresh(load);
    return () => {
      clearInterval(timer);
      off();
    };
  }, [load]);

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => box.current && !box.current.contains(e.target) && setOpen(false);
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  const readAll = async () => {
    await markAllRead();
    load();
  };

  return (
    <div className="relative" ref={box}>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label={`Notifikasi, ${state.unreadCount} belum dibaca`}
        className="relative rounded-lg p-2 text-slate-500 hover:bg-slate-100"
      >
        <Bell size={20} />
        {state.unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-semibold text-white">
            {state.unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-40 mt-2 w-80 max-w-[90vw] rounded-xl border border-slate-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
            <span className="text-sm font-semibold text-slate-900">Notifikasi</span>
            <button
              type="button"
              onClick={readAll}
              disabled={state.unreadCount === 0}
              className="text-xs font-medium text-emerald-600 hover:underline disabled:text-slate-400 disabled:no-underline"
            >
              Tandai semua dibaca
            </button>
          </div>
          <ul className="max-h-80 divide-y divide-slate-100 overflow-y-auto">
            {state.items.length === 0 && <li className="px-4 py-6 text-center text-sm text-slate-400">Belum ada notifikasi</li>}
            {state.items.map((n) => (
              <li key={n.notificationId} className={`border-l-4 px-4 py-3 ${LEVEL_STYLE[n.level]} ${n.isRead ? "opacity-60" : ""}`}>
                <p className="text-sm text-slate-800">{n.message}</p>
                <p className="mt-1 text-xs text-slate-400">{formatWaktu(n.createdAt)}</p>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
