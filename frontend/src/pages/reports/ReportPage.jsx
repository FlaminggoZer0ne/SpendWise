import { useState } from "react";
import { Download } from "lucide-react";
import { downloadReportCsv, getCategoryReport, getPeriodReport } from "../../api/reportApi";
import useFetch from "../../hooks/useFetch";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import Input from "../../components/ui/Input";
import Select from "../../components/ui/Select";
import { useToast } from "../../components/ui/Toast";
import { akhirBulan, awalBulan, currentMonth } from "../../utils/format";
import { PERIOD_TYPES } from "../../utils/labels";
import CategoryReportView from "./CategoryReportView";
import PeriodReportView from "./PeriodReportView";

const TABS = [
  { key: "category", label: "Per Kategori" },
  { key: "period", label: "Per Periode" },
];

export default function ReportPage() {
  const toast = useToast();
  const [tab, setTab] = useState("category");
  const [range, setRange] = useState({ from: awalBulan(currentMonth()), to: akhirBulan(currentMonth()) });
  const [periodType, setPeriodType] = useState("MONTHLY");
  const [downloading, setDownloading] = useState(false);

  const params = tab === "period" ? { ...range, type: periodType } : range;
  const { data, loading, error, reload } = useFetch(
    () => (tab === "period" ? getPeriodReport(params) : getCategoryReport(params)),
    [tab, range.from, range.to, periodType]
  );

  // Saat pindah tab, data tab sebelumnya (bentuk berbeda) masih ada sampai respons baru tiba.
  const isStale = Boolean(data) && ("periodType" in data) !== (tab === "period");

  const download = async () => {
    setDownloading(true);
    try {
      await downloadReportCsv(tab === "period" ? { kind: "period", ...range, periodType } : { kind: "category", ...range });
    } catch {
      toast.error("Gagal mengunduh laporan");
    } finally {
      setDownloading(false);
    }
  };

  return (
    <>
      <div className="flex gap-2" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.key}
            role="tab"
            aria-selected={tab === t.key}
            onClick={() => setTab(t.key)}
            className={`rounded-lg px-4 py-2 text-sm font-medium ${tab === t.key ? "btn-glow bg-gradient-to-br from-emerald-500 to-emerald-600 text-white" : "glass-field text-slate-600 hover:bg-white/90"}`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <Card className="flex flex-wrap items-end gap-3">
        <Input label="Dari" type="date" value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
        <Input label="Sampai" type="date" value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
        {tab === "period" && (
          <Select label="Periode" value={periodType} onChange={(e) => setPeriodType(e.target.value)}>
            {PERIOD_TYPES.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
          </Select>
        )}
        <Button variant="secondary" onClick={download} disabled={downloading}>
          <Download size={16} /> {downloading ? "Mengunduh..." : "Unduh CSV"}
        </Button>
      </Card>

      {(loading && !data) || isStale ? <Spinner /> : error ? <ErrorState error={error} onRetry={reload} /> : tab === "period" ? (
        <PeriodReportView report={data} />
      ) : (
        <CategoryReportView report={data} />
      )}
    </>
  );
}
