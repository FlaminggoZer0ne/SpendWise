import { useState } from "react";
import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

export default function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <div className="min-h-screen bg-slate-50">
      <Sidebar open={menuOpen} onClose={() => setMenuOpen(false)} />
      <div className="md:pl-60">
        <Navbar onMenu={() => setMenuOpen(true)} />
        <main className="mx-auto max-w-6xl space-y-6 p-4 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
