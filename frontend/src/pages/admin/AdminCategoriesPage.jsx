import { useState } from "react";
import { Plus, Trash2 } from "lucide-react";
import { createDefaultCategory, deleteDefaultCategory } from "../../api/adminApi";
import { getCategories } from "../../api/categoryApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import ConfirmDialog from "../../components/ui/ConfirmDialog";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, FormError, Spinner } from "../../components/ui/Feedback";
import Input from "../../components/ui/Input";
import Select from "../../components/ui/Select";
import { useToast } from "../../components/ui/Toast";
import { TRANSACTION_TYPES } from "../../utils/labels";

export default function AdminCategoriesPage() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(() => getCategories());
  const [form, setForm] = useState({ name: "", type: "EXPENSE" });
  const [formError, setFormError] = useState(null);
  const [toDelete, setToDelete] = useState(null);

  const add = async (e) => {
    e.preventDefault();
    setFormError(null);
    try {
      const res = await createDefaultCategory(form);
      toast.success(res.message);
      setForm({ ...form, name: "" });
      reload();
    } catch (err) {
      setFormError(err);
    }
  };

  if (loading && !data) return <Spinner />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  return (
    <>
      <Card>
        <form onSubmit={add} className="grid gap-3 sm:grid-cols-[1fr_200px_auto] sm:items-end">
          <Input label="Nama kategori default" value={form.name} maxLength={40} required onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Select label="Jenis" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            {TRANSACTION_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </Select>
          <Button type="submit"><Plus size={16} /> Tambah</Button>
        </form>
        <div className="mt-3"><FormError error={formError} /></div>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {TRANSACTION_TYPES.map((t) => {
          const items = data.filter((c) => c.type === t.value);
          return (
            <Card key={t.value}>
              <h2 className="mb-3 text-base font-semibold">{t.label}</h2>
              {items.length === 0 ? (
                <EmptyState title="Belum ada kategori" />
              ) : (
                <ul className="divide-y divide-slate-100">
                  {items.map((c) => (
                    <li key={c.categoryId} className="flex items-center justify-between py-2 text-sm">
                      <span className="text-slate-800">{c.name}</span>
                      <Button variant="ghost" size="sm" aria-label={`Hapus ${c.name}`} onClick={() => setToDelete(c)}>
                        <Trash2 size={14} className="text-red-600" />
                      </Button>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          );
        })}
      </div>

      {toDelete && (
        <ConfirmDialog
          title="Hapus kategori default"
          message={`Hapus kategori "${toDelete.name}"? Kategori yang sudah dipakai tidak dapat dihapus.`}
          onClose={() => setToDelete(null)}
          onConfirm={async () => {
            const res = await deleteDefaultCategory(toDelete.categoryId);
            toast.success(res.message);
            reload();
          }}
        />
      )}
    </>
  );
}
