import client from "./client";

export const getNotifications = () => client.get("/notifications");
export const markAllRead = () => client.post("/notifications/read-all");
