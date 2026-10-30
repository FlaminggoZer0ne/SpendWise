import client from "./client";

export const getCategories = (type) => client.get("/categories", { params: type ? { type } : {} });
export const createCategory = (body) => client.post("/categories", body);
export const deleteCategory = (id) => client.delete(`/categories/${id}`);
