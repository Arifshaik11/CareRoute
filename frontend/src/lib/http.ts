import axios, { AxiosError } from 'axios'
import type { ApiProblem } from '../types/api'

const TOKEN_KEY = 'careroute.token'

export const tokenStore = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set(token: string) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch {
      /* storage unavailable (private mode): session lasts until reload */
    }
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      /* ignore */
    }
  },
}

/** Shared API client. In dev, Vite proxies /api to the Spring Boot server. */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15_000,
})

http.interceptors.request.use((config) => {
  const token = tokenStore.get()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

type UnauthorizedListener = () => void
let onUnauthorized: UnauthorizedListener | null = null

/** The auth provider registers here so an expired token logs the user out everywhere. */
export function setUnauthorizedListener(listener: UnauthorizedListener | null) {
  onUnauthorized = listener
}

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const isLogin = error.config?.url?.includes('/auth/login')
    if (error.response?.status === 401 && !isLogin) {
      tokenStore.clear()
      onUnauthorized?.()
    }
    return Promise.reject(error)
  },
)

/** Turns any API failure into one human-readable sentence. */
export function errorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Cannot reach the server. Check that the backend is running.'
    }
    const problem = error.response.data as ApiProblem | undefined
    if (problem?.errors) {
      const [field, message] = Object.entries(problem.errors)[0] ?? []
      if (field) return `${humanize(field)}: ${message}`
    }
    return problem?.detail ?? fallback
  }
  return fallback
}

function humanize(field: string): string {
  return field.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase())
}
