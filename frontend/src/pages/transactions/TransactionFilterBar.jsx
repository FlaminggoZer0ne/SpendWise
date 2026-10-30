import Card from "../../components/ui/Card";
import Input from "../../components/ui/Input";
import Select from "../../components/ui/Select";
import { TRANSACTION_TYPES } from "../../utils/labels";

export default function TransactionFilterBar({ filter, onChange, categories, wallets }) {
  const set = (k) => (e) => onChange({ ...filter, [k]: e.target.value, page: 1 });
  return (
    <Card className="grid gap-3 sm:grid-cols-2 lg:grid-cols-6">
      <Input label="Dari" type="date" value={filter.from} onChange={set("from")} />
      <Input label="Sampai" type="date" value={filter.to} onChange={set("to")} />
      <Select label="Jenis" value={filter.type} onChange={set("type")}>
        <option value="">Semua</option>
        {TRANSACTION_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
      </Select>
      <Select label="Kategori" value={filter.categoryId} onChange={set("categoryId")}>
        <option value="">Semua</option>
        {categories.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
      </Select>
      <Select label="Dompet" value={filter.walletId} onChange={set("walletId")}>
        <option value="">Semua</option>
        {wallets.map((w) => <option key={w.walletId} value={w.walletId}>{w.name}</option>)}
      </Select>
      <Input label="Cari catatan" value={filter.q} placeholder="Kata kunci" onChange={set("q")} />
    </Card>
  );
}
