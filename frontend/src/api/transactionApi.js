import client from "./client";

export const getTransactions = (params) => client.get("/transactions", { params });
export const createTransaction = (body) => client.post("/transactions", body);
export const updateTransaction = (id, body) => client.put(`/transactions/${id}`, body);
export const deleteTransaction = (id) => client.delete(`/transactions/${id}`);
