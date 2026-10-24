import type { ReactNode } from 'react'
import clsx from 'clsx'

export type Tone = 'critical' | 'high' | 'medium' | 'low' | 'good' | 'brand' | 'neutral' | 'accent'

const tones: Record<Tone, string> = {
  critical: 'bg-critical-soft text-critical',
  high: 'bg-high-soft text-high',
  medium: 'bg-medium-soft text-medium',
  low: 'bg-low-soft text-low',
  good: 'bg-good-soft text-good',
  brand: 'bg-brand-soft text-brand',
  accent: 'bg-accent-soft text-ink',
  neutral: 'bg-ink/5 text-ink-soft',
}

export function Badge({ tone = 'neutral', icon, children }: { tone?: Tone; icon?: ReactNode; children: ReactNode }) {
  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2 py-0.5 text-xs font-semibold',
        tones[tone],
      )}
    >
      {icon}
      {children}
    </span>
  )
}
