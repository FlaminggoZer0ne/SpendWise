import { useState } from "react";
import { Plus, Trash2 } from "lucide-react";
import { createCategory, deleteCategory, getCategories } from "../../api/categoryApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, FormError, Spinner } from "../../components/ui/Feedback";
import Input from "../../components/ui/Input";
import { useToast } from "../../components/ui/Toast";
import { TRANSACTION_TYPES } from "../../utils/labels";

export default function CategoryPage() {
  const toast = useToast();
  const [tab, setTab] = useState("EXPENSE");
  const [name, setName] = useState("");
  const [formError, setFormError] = useState(null);
  const [toDelete, setToDelete] = useState(null);
  const { data, loading, error, reload } = useFetch(() => getCategories(tab), [tab]);

  const add = async (e) => {
    e.preventDefault();
    setFormError(null);
    try {
      const res = await createCategory({ name, type: tab });
      toast.success(res.message);
      setName("");
      reload();
    } catch (err) {
      setFormError(err);
    }
  };

  return (
    <>
      <div className="flex gap-2" role="tablist">
        {TRANSACTION_TYPES.map((t) => (
          <button
            key={t.value}
            role="tab"
            aria-selected={tab === t.value}
            onClick={() => { setTab(t.value); setFormError(null); }}
            className={`rounded-lg px-4 py-2 text-sm font-medium ${tab === t.value ? "bg-emerald-600 text-white" : "bg-white text-slate-600 hover:bg-slate-100"}`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <Card>
        <form onSubmit={add} className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex-1">
            <Input label="Kategori baru" value={name} maxLength={40} required placeholder="Contoh: Ngopi" onChange={(e) => setName(e.target.value)} />
          </div>
          <Button type="submit"><Plus size={16} /> Tambah</Button>
        </form>
        <div className="mt-3"><FormError error={formError} /></div>
      </Card>

      {loading && !data ? <Spinner /> : error ? <ErrorState error={error} onRetry={reload} /> : data.length === 0 ? (
        <EmptyState title="Belum ada kategori" />
      ) : (
        <Card>
          <ul className="divide-y divide-slate-100">
            {data.map((c) => (
              <li key={c.categoryId} className="flex items-center justify-between py-2.5 text-sm">
                <span className="flex items-center gap-2 text-slate-800">
                  {c.name}
                  {c.isDefault && <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500">Default</span>}
                </span>
                {!c.isDefault && (
                  <Button variant="ghost" size="sm" aria-label={`Hapus ${c.name}`} onClick={() => setToDelete(c)}>
                    <Trash2 size={14} className="text-red-600" />
                  </Button>
                )}
              </li>
            ))}
          </ul>
        </Card>
      )}

      {toDelete && (
        <ConfirmDialog
          title="Hapus kategori"
          message={`Hapus kategori "${toDelete.name}"? Kategori yang sudah dipakai tidak dapat dihapus.`}
          onClose={() => setToDelete(null)}
          onConfirm={async () => {
            const res = await deleteCategory(toDelete.categoryId);
            toast.success(res.message);
            reload();
          }}
        />
      )}
    </>
  );
}
