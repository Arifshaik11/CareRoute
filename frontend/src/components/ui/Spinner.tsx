import { Loader2 } from 'lucide-react'

export function Spinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-muted" role="status">
      <Loader2 className="size-5 animate-spin" aria-hidden />
      {label}
    </div>
  )
}

export function FullPageSpinner() {
  return (
    <div className="grid min-h-dvh place-items-center">
      <Spinner />
    </div>
  )
}
