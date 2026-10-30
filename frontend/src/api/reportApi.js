import axios from "axios";
import client from "./client";

export const getDashboard = () => client.get("/reports/dashboard");
export const getCategoryReport = (params) => client.get("/reports/category", { params });
export const getPeriodReport = (params) => client.get("/reports/period", { params });

/** Unduh CSV sesuai filter aktif; nama file diambil dari header Content-Disposition. */
export async function downloadReportCsv(params) {
  const res = await axios.get("/api/reports/export", { params, responseType: "blob", withCredentials: true });
  const name = res.headers["content-disposition"]?.match(/filename="?([^";]+)"?/)?.[1] ?? "laporan.csv";
  const url = URL.createObjectURL(res.data);
  Object.assign(document.createElement("a"), { href: url, download: name }).click();
  URL.revokeObjectURL(url);
}
