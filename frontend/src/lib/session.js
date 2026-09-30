const TOKEN_KEY = "ecoandina.token"

export const session = {
  getToken() {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  setToken(token) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch {
      // Almacenamiento no disponible (modo privado): la sesión dura solo la pestaña.
    }
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      // Nada que limpiar.
    }
  },
}
