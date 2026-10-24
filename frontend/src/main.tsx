import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router'
import axios from 'axios'
import { AuthProvider } from './auth/AuthContext'
import { LangProvider } from './contexts/LangContext'
import { ToastProvider } from './contexts/ToastContext'
import { App } from './App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 10_000,
      refetchOnWindowFocus: true,
      // Don't retry client errors (403/404/...), only flaky network/server failures.
      retry: (failureCount, error) => {
        const status = axios.isAxiosError(error) ? error.response?.status : undefined
        return status !== undefined && status < 500 ? false : failureCount < 2
      },
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <LangProvider>
          <ToastProvider>
            <AuthProvider>
              <App />
            </AuthProvider>
          </ToastProvider>
        </LangProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
