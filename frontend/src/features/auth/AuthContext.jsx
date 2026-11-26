import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react"
import { useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import { setUnauthorizedHandler } from "@/lib/api"
import { session } from "@/lib/session"
import { fetchMe, login } from "./authApi"

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [user, setUser] = useState(null)
  const [status, setStatus] = useState(() =>
    session.getToken() ? "loading" : "anonymous"
  )

  const signOut = useCallback(() => {
    session.clear()
    setUser(null)
    setStatus("anonymous")
    queryClient.clear()
  }, [queryClient])

  useEffect(() => {
    if (status !== "loading") return
    let cancelled = false

    fetchMe()
      .then((me) => {
        if (cancelled) return
        setUser(me)
        setStatus("authenticated")
      })
      .catch((error) => {
        if (cancelled) return
        if ([401, 403].includes(error.response?.status)) session.clear()
        setStatus("anonymous")
      })

    return () => {
      cancelled = true
    }
  }, [status])

  useEffect(() => {
    setUnauthorizedHandler(() => {
      if (session.getToken()) {
        toast.error("Tu sesión expiró. Inicia sesión nuevamente.")
      }
      signOut()
    })
    return () => setUnauthorizedHandler(() => {})
  }, [signOut])

  const signIn = useCallback(async (credentials) => {
    const data = await login(credentials)
    session.setToken(data.accessToken)
    setUser(data.user)
    setStatus("authenticated")
    return data.user
  }, [])

  const value = useMemo(() => {
    const hasPermission = (code) => user?.permisos?.includes(code) ?? false
    return {
      user,
      status,
      signIn,
      signOut,
      hasPermission,
      hasAnyPermission: (codes) => codes.some(hasPermission),
    }
  }, [user, status, signIn, signOut])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth debe usarse dentro de <AuthProvider>")
  }
  return context
}
