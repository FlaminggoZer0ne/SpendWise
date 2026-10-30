import client from "./client";

export const getWallets = () => client.get("/wallets");
export const createWallet = (body) => client.post("/wallets", body);
export const updateWallet = (id, body) => client.put(`/wallets/${id}`, body);
export const deleteWallet = (id) => client.delete(`/wallets/${id}`);
export const transfer = (body) => client.post("/wallets/transfer", body);
