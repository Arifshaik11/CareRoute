import clsx from 'clsx'
import { formatDateTime } from '../lib/format'
import type { ReferralEvent, ReferralEventType } from '../types/api'

const label: Record<ReferralEventType, string> = {
  REFERRED: 'Referred',
  ACKNOWLEDGED: 'Acknowledged by facility',
  ARRIVED: 'Patient arrived',
  CLOSED: 'Closed',
  ESCALATED: 'Escalated — patient did not arrive in time',
}

const dot: Record<ReferralEventType, string> = {
  REFERRED: 'bg-brand',
  ACKNOWLEDGED: 'bg-accent',
  ARRIVED: 'bg-good',
  CLOSED: 'bg-muted',
  ESCALATED: 'bg-critical',
}

/** The closed loop, step by step: who did what, when. */
export function ReferralTimeline({ events }: { events: ReferralEvent[] }) {
  return (
    <ol className="relative space-y-4 border-l-2 border-line pl-5">
      {events.map((e, i) => (
        <li key={`${e.type}-${i}`} className="relative">
          <span
            className={clsx('absolute -left-[27px] top-1 size-3 rounded-full ring-4 ring-surface', dot[e.type])}
            aria-hidden
          />
          <p className={clsx('text-sm font-semibold', e.type === 'ESCALATED' ? 'text-critical' : 'text-ink')}>
            {label[e.type]}
          </p>
          <p className="text-xs text-muted">
            {formatDateTime(e.occurredAt)} · {e.actorName}
          </p>
          {e.note && <p className="mt-1 text-sm text-ink-soft">{e.note}</p>}
        </li>
      ))}
    </ol>
  )
}
