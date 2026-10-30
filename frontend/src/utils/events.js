const REFRESH_NOTIFICATIONS = "spendwise:refresh-notifications";

/** Memberi tahu NotificationBell agar memuat ulang (mis. setelah menyimpan pengeluaran). */
export const requestNotificationRefresh = () => window.dispatchEvent(new Event(REFRESH_NOTIFICATIONS));

export const onNotificationRefresh = (handler) => {
  window.addEventListener(REFRESH_NOTIFICATIONS, handler);
  return () => window.removeEventListener(REFRESH_NOTIFICATIONS, handler);
};
