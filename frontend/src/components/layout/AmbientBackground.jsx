/** Blob warna yang diburamkan di belakang halaman, supaya efek kaca punya sesuatu untuk diburamkan. */
export default function AmbientBackground() {
  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 z-0 overflow-hidden">
      <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-emerald-400/30 blur-3xl" />
      <div className="absolute -left-28 top-1/3 h-72 w-72 rounded-full bg-blue-500/20 blur-3xl" />
      <div className="absolute -bottom-24 right-0 h-72 w-72 rounded-full bg-violet-500/20 blur-3xl" />
    </div>
  );
}
