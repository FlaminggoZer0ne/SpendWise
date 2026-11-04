import { Inbox } from "lucide-react";

export default function EmptyState({ title, description, action, icon: Icon = Inbox }) {
  return (
    <div className="flex flex-col items-center rounded-2xl border border-dashed border-slate-300/80 bg-white/50 px-6 py-12 text-center backdrop-blur-md">
      <span className="rounded-full bg-slate-100 p-3 text-slate-400">
        <Icon size={24} />
      </span>
      <h3 className="mt-3 text-sm font-semibold">{title}</h3>
      {description && <p className="mt-1 max-w-sm text-sm text-slate-500">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
