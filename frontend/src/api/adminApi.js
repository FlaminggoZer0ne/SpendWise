import client from "./client";

export const getUsers = () => client.get("/admin/users");
export const setUserActive = (id, active) => client.patch(`/admin/users/${id}/active`, { active });
export const createDefaultCategory = (body) => client.post("/admin/categories", body);
export const deleteDefaultCategory = (id) => client.delete(`/admin/categories/${id}`);
