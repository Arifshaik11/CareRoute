import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { X, Bell } from 'lucide-react'

interface Toast {
  id: number
  title: string
  body: string
  tone: 'info' | 'critical'
}

interface ToastCtx {
  push: (title: string, body: string, tone?: Toast['tone']) => void
}

const ToastContext = createContext<ToastCtx>({ push: () => {} })

let nextId = 1

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const push = useCallback((title: string, body: string, tone: Toast['tone'] = 'info') => {
    const id = nextId++
    setToasts((t) => [...t, { id, title, body, tone }])
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 6000)
  }, [])

  function dismiss(id: number) {
    setToasts((t) => t.filter((x) => x.id !== id))
  }

  return (
    <ToastContext.Provider value={{ push }}>
      {children}
      <div className="fixed bottom-24 right-4 z-50 flex flex-col gap-2 sm:bottom-6" aria-live="polite">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`flex w-72 items-start gap-3 rounded-xl border p-3 shadow-lg backdrop-blur transition-all ${
              toast.tone === 'critical'
                ? 'border-critical/30 bg-critical-soft text-critical'
                : 'border-brand/30 bg-brand-soft text-brand'
            }`}
          >
            <Bell className="mt-0.5 size-4 shrink-0" aria-hidden />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-bold">{toast.title}</p>
              <p className="mt-0.5 text-xs opacity-80">{toast.body}</p>
            </div>
            <button
              type="button"
              onClick={() => dismiss(toast.id)}
              className="shrink-0 opacity-60 hover:opacity-100"
              aria-label="Dismiss"
            >
              <X className="size-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  return useContext(ToastContext)
}
