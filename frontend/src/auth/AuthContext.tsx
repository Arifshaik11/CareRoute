import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { http, setUnauthorizedListener, tokenStore } from '../lib/http'
import type { LoginResponse, User } from '../types/api'
import { AuthContext, type AuthState } from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [user, setUser] = useState<User | null>(null)
  const [status, setStatus] = useState<AuthState['status']>(() => (tokenStore.get() ? 'loading' : 'anonymous'))

  const logout = useCallback(() => {
    tokenStore.clear()
    setUser(null)
    setStatus('anonymous')
    queryClient.clear()
  }, [queryClient])

  // Restore the session from a stored token on first load.
  useEffect(() => {
    if (!tokenStore.get()) return
    let cancelled = false
    http
      .get<User>('/auth/me')
      .then((res) => {
        if (cancelled) return
        setUser(res.data)
        setStatus('authenticated')
      })
      .catch(() => {
        if (!cancelled) logout()
      })
    return () => {
      cancelled = true
    }
  }, [logout])

  useEffect(() => {
    setUnauthorizedListener(logout)
    return () => setUnauthorizedListener(null)
  }, [logout])

  const login = useCallback(
    async (username: string, password: string) => {
      const { data } = await http.post<LoginResponse>('/auth/login', { username, password })
      queryClient.clear()
      tokenStore.set(data.token)
      setUser(data.user)
      setStatus('authenticated')
      return data.user
    },
    [queryClient],
  )

  const value = useMemo<AuthState>(() => ({ user, status, login, logout }), [user, status, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
