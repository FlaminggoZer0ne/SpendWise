import axios from "axios";

const client = axios.create({ baseURL: "/api", withCredentials: true });

client.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status;
    const isAuthCall = err.config?.url?.startsWith("/auth/login") || err.config?.url?.startsWith("/auth/me");
    if (status === 401 && !isAuthCall && !window.location.pathname.startsWith("/login")) {
      window.location.href = "/login?expired=1";
    }
    return Promise.reject(err.response?.data ?? { success: false, message: "Tidak dapat terhubung ke server" });
  }
);

export default client;
