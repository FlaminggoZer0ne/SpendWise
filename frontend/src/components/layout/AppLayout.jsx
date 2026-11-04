import { useState } from "react";
import { Outlet } from "react-router-dom";
import AmbientBackground from "./AmbientBackground";
import BottomNav from "./BottomNav";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

export default function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <div className="relative min-h-screen overflow-x-clip">
      <AmbientBackground />
      <Sidebar open={menuOpen} onClose={() => setMenuOpen(false)} />
      <div className="relative z-10 min-w-0 md:pl-60">
        <Navbar onMenu={() => setMenuOpen(true)} />
        {/* pb-28: ruang agar konten terakhir tidak tertutup bottom nav melayang di ponsel.
            min-w-0 pada main/grid: mencegah grafik/tabel melebarkan halaman (scroll horizontal). */}
        <main className="mx-auto min-w-0 max-w-6xl space-y-4 p-3 pb-28 sm:space-y-6 sm:p-4 sm:pb-28 md:p-8 md:pb-8">
          <Outlet />
        </main>
      </div>
      <BottomNav />
    </div>
  );
}
