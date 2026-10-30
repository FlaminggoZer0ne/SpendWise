import { ChevronLeft, ChevronRight } from "lucide-react";
import Button from "./Button";

export default function Pagination({ page, totalPages, totalItems, onChange }) {
  if (!totalItems) return null;
  return (
    <div className="flex items-center justify-between px-1 py-3 text-sm text-slate-500">
      <span>
        Halaman {page} dari {Math.max(totalPages, 1)} · {totalItems} data
      </span>
      <div className="flex gap-2">
        <Button variant="secondary" size="sm" disabled={page <= 1} onClick={() => onChange(page - 1)} aria-label="Halaman sebelumnya">
          <ChevronLeft size={16} />
        </Button>
        <Button variant="secondary" size="sm" disabled={page >= totalPages} onClick={() => onChange(page + 1)} aria-label="Halaman berikutnya">
          <ChevronRight size={16} />
        </Button>
      </div>
    </div>
  );
}
