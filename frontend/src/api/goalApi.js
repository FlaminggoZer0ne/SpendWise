import client from "./client";

export const getGoals = () => client.get("/goals");
export const createGoal = (body) => client.post("/goals", body);
export const contribute = (id, body) => client.post(`/goals/${id}/contribute`, body);
export const cancelGoal = (id) => client.post(`/goals/${id}/cancel`);
