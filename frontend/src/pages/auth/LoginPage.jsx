import { useState } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { homePath, useAuth } from "../../context/AuthContext";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import { FormError } from "../../components/ui/Feedback";

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to={homePath(user)} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const u = await login(form.username, form.password);
      navigate(homePath(u), { replace: true });
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="text-center">
          <img src="/logo.png" alt="" className="mx-auto h-12 w-12 object-contain" />
          <h1 className="mt-2 text-xl font-bold">Masuk ke SpendWise</h1>
        </div>
        {params.get("expired") && !error && (
          <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">Sesi berakhir, silakan login kembali.</p>
        )}
        <FormError error={error} />
        <Input label="Username" value={form.username} autoComplete="username" required
          onChange={(e) => setForm({ ...form, username: e.target.value })} />
        <Input label="Password" type="password" value={form.password} autoComplete="current-password" required
          onChange={(e) => setForm({ ...form, password: e.target.value })} />
        <Button type="submit" className="w-full" disabled={busy}>{busy ? "Memproses..." : "Masuk"}</Button>
        <p className="text-center text-sm text-slate-500">
          Belum punya akun? <Link to="/register" className="font-medium text-emerald-600 hover:underline">Daftar</Link>
        </p>
      </form>
    </div>
  );
}
