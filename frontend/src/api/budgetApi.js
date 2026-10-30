import client from "./client";

export const getBudgets = (month) => client.get("/budgets", { params: { month } });
export const createBudget = (body) => client.post("/budgets", body);
export const updateBudget = (id, body) => client.put(`/budgets/${id}`, body);
export const deleteBudget = (id) => client.delete(`/budgets/${id}`);
