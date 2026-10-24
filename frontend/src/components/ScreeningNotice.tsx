import { ShieldCheck } from 'lucide-react'

/** Shown wherever observations are entered or displayed: CareRoute coordinates care, it does not diagnose. */
export function ScreeningNotice({ compact = false }: { compact?: boolean }) {
  return (
    <div className="flex items-start gap-2 rounded-lg border border-brand/15 bg-brand-soft/60 px-3 py-2 text-xs text-ink-soft">
      <ShieldCheck className="mt-0.5 size-4 shrink-0 text-brand" aria-hidden />
      <p>
        <span className="font-semibold text-brand">Screening information, not a diagnosis.</span>
        {!compact && ' Flags come from rules set by the medical officer. Clinical decisions are made by qualified staff at the facility.'}
      </p>
    </div>
  )
}
