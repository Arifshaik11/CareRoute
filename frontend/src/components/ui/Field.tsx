import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import clsx from 'clsx'

const control =
  'w-full rounded-lg border border-line bg-surface px-3 text-sm text-ink placeholder:text-muted/70 ' +
  'focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 disabled:bg-canvas'

interface FieldProps {
  label: ReactNode
  hint?: ReactNode
  error?: string
  children: (id: string) => ReactNode
}

/** Label + control + hint/error, wired together with a generated id for accessibility. */
export function Field({ label, hint, error, children }: FieldProps) {
  const id = useId()
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-semibold text-ink-soft">
        {label}
      </label>
      {children(id)}
      {error ? (
        <p className="text-xs font-medium text-critical">{error}</p>
      ) : (
        hint && <p className="text-xs text-muted">{hint}</p>
      )}
    </div>
  )
}

export function Input({ className, ...rest }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={clsx(control, 'h-10', className)} {...rest} />
}

export function Select({ className, ...rest }: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select className={clsx(control, 'h-10 pr-8', className)} {...rest} />
}

export function Textarea({ className, ...rest }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={clsx(control, 'min-h-20 py-2', className)} {...rest} />
}
