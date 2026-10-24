import type { ReactNode } from 'react'
import { AlertTriangle, Inbox } from 'lucide-react'
import { errorMessage } from '../../lib/http'
import { Button } from './Button'

export function EmptyState({ title, children, icon }: { title: string; children?: ReactNode; icon?: ReactNode }) {
  return (
    <div className="flex flex-col items-center px-6 py-12 text-center">
      <div className="mb-3 grid size-11 place-items-center rounded-full bg-brand-soft text-brand">
        {icon ?? <Inbox className="size-5" aria-hidden />}
      </div>
      <p className="font-semibold text-ink">{title}</p>
      {children && <div className="mt-1 max-w-sm text-sm text-muted">{children}</div>}
    </div>
  )
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <div className="flex flex-col items-center px-6 py-12 text-center" role="alert">
      <div className="mb-3 grid size-11 place-items-center rounded-full bg-critical-soft text-critical">
        <AlertTriangle className="size-5" aria-hidden />
      </div>
      <p className="font-semibold text-ink">Couldn't load this</p>
      <p className="mt-1 max-w-sm text-sm text-muted">{errorMessage(error)}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" className="mt-4" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}

export function InlineError({ error }: { error: unknown }) {
  if (!error) return null
  return (
    <div className="flex items-start gap-2 rounded-lg bg-critical-soft px-3 py-2 text-sm text-critical" role="alert">
      <AlertTriangle className="mt-0.5 size-4 shrink-0" aria-hidden />
      <span>{errorMessage(error)}</span>
    </div>
  )
}
