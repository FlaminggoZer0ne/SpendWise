import client from "./client";

export const register = (body) => client.post("/auth/register", body);
export const login = (body) => client.post("/auth/login", body);
export const logout = () => client.post("/auth/logout");
export const me = () => client.get("/auth/me");
export const updateProfile = (body) => client.put("/auth/profile", body);
export const changePassword = (body) => client.put("/auth/password", body);
