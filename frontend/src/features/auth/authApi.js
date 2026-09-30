import { api } from "@/lib/api"

export const login = (credentials) =>
  api.post("/auth/login", credentials).then((response) => response.data)

export const fetchMe = () => api.get("/auth/me").then((response) => response.data)
