import { useAuth } from "@/features/auth/AuthContext"

export function useActor() {
  const { user } = useAuth()
  return `${user.nombres} ${user.apellidos}`
}
