import { forwardRef, type ButtonHTMLAttributes } from 'react'
import { Link, type LinkProps } from 'react-router'
import clsx from 'clsx'
import { Loader2 } from 'lucide-react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
type Size = 'sm' | 'md' | 'lg'

const variants: Record<Variant, string> = {
  primary: 'bg-brand text-white hover:bg-brand-strong disabled:bg-brand/50',
  secondary: 'bg-surface text-ink border border-line hover:border-brand/40 hover:bg-brand-soft/40',
  ghost: 'text-ink-soft hover:bg-ink/5',
  danger: 'bg-critical text-white hover:bg-critical/90',
}

const sizes: Record<Size, string> = {
  sm: 'h-8 px-3 text-sm gap-1.5',
  md: 'h-10 px-4 text-sm gap-2',
  lg: 'h-12 px-5 text-base gap-2',
}

function buttonClasses(variant: Variant, size: Size, className?: string) {
  return clsx(
    'inline-flex items-center justify-center rounded-lg font-semibold transition-colors',
    'disabled:cursor-not-allowed disabled:opacity-70',
    variants[variant],
    sizes[size],
    className,
  )
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  loading?: boolean
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'primary', size = 'md', loading = false, className, children, disabled, type = 'button', ...rest },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      className={buttonClasses(variant, size, className)}
      {...rest}
    >
      {loading && <Loader2 className="size-4 animate-spin" aria-hidden />}
      {children}
    </button>
  )
})

interface ButtonLinkProps extends LinkProps {
  variant?: Variant
  size?: Size
}

/** A router link that looks like a button (never nest a <button> inside an <a>). */
export function ButtonLink({ variant = 'primary', size = 'md', className, ...rest }: ButtonLinkProps) {
  return <Link className={buttonClasses(variant, size, typeof className === 'string' ? className : undefined)} {...rest} />
}
