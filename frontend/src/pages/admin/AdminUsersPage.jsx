import { useState } from "react";
import { Search } from "lucide-react";
import { getUsers, setUserActive } from "../../api/adminApi";
import useFetch from "../../hooks/useFetch";
import { useAuth } from "../../context/AuthContext";
import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import { ErrorState, Spinner } from "../../components/ui/Feedback";
import { Table, Td, Th } from "../../components/ui/Table";
import { useToast } from "../../components/ui/Toast";
import { FIELD } from "../../components/ui/Input";
import { formatTanggal } from "../../utils/format";

export default function AdminUsersPage() {
  const { user: me } = useAuth();
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(getUsers);
  const [q, setQ] = useState("");

  const toggle = async (u) => {
    try {
      const res = await setUserActive(u.userId, !u.active);
      toast.success(res.message);
      reload();
    } catch (err) {
      toast.error(err.message);
    }
  };

  if (loading && !data) return <Spinner />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const keyword = q.trim().toLowerCase();
  const rows = data.filter((u) => !keyword || u.username.toLowerCase().includes(keyword) || u.fullName.toLowerCase().includes(keyword));

  return (
    <>
      <div className="relative max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input className={`${FIELD} border-slate-300 pl-9`} placeholder="Cari username atau nama" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>
      {rows.length === 0 ? (
        <EmptyState title="Tidak ada pengguna" description="Tidak ada pengguna yang cocok dengan pencarian." />
      ) : (
        <Table>
          <thead>
            <tr><Th>Username</Th><Th>Nama</Th><Th>Email</Th><Th>Role</Th><Th>Bergabung</Th><Th>Status</Th><Th right>Aksi</Th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((u) => (
              <tr key={u.userId}>
                <Td className="font-medium text-slate-900">{u.username}</Td>
                <Td>{u.fullName}</Td>
                <Td>{u.email}</Td>
                <Td>{u.role}</Td>
                <Td>{formatTanggal(u.createdAt?.slice(0, 10))}</Td>
                <Td>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${u.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
                    {u.active ? "Aktif" : "Nonaktif"}
                  </span>
                </Td>
                <Td right>
                  <Button size="sm" variant={u.active ? "secondary" : "primary"} disabled={u.userId === me.userId} onClick={() => toggle(u)}>
                    {u.active ? "Nonaktifkan" : "Aktifkan"}
                  </Button>
                </Td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </>
  );
}
