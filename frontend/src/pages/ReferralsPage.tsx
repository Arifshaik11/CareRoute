import { useState } from 'react'
import { Link } from 'react-router'
import clsx from 'clsx'
import { ChevronRight } from 'lucide-react'
import { useReferrals } from '../api/queries'
import { useCurrentUser } from '../auth/auth-context'
import { EscalatedBadge, OverdueBadge, PriorityBadge, ProgramTag, ReferralStatusBadge } from '../components/StatusBadges'
import { Card } from '../components/ui/Card'
import { PageHeader } from '../components/ui/PageHeader'
import { Spinner } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { formatDateTime, formatRelative } from '../lib/format'
import type { Referral } from '../types/api'

type Tab = 'open' | 'escalated' | 'arrived' | 'closed' | 'all'

const TABS: { id: Tab; label: string; test: (r: Referral) => boolean }[] = [
  { id: 'open', label: 'Awaiting arrival', test: (r) => r.status === 'REFERRED' || r.status === 'ACKNOWLEDGED' },
  { id: 'escalated', label: 'Escalated', test: (r) => !!r.escalatedAt && (r.status === 'REFERRED' || r.status === 'ACKNOWLEDGED') },
  { id: 'arrived', label: 'Arrived', test: (r) => r.status === 'ARRIVED' },
  { id: 'closed', label: 'Closed', test: (r) => r.status === 'CLOSED' },
  { id: 'all', label: 'All', test: () => true },
]

const PRIORITY_RANK = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 } as const

export function ReferralsPage() {
  const user = useCurrentUser()
  const [tab, setTab] = useState<Tab>('open')
  const { data, isLoading, error, refetch } = useReferrals()

  const active = TABS.find((t) => t.id === tab)!
  const rows = (data ?? [])
    .filter(active.test)
    .sort((a, b) => PRIORITY_RANK[a.priority] - PRIORITY_RANK[b.priority] || a.dueAt.localeCompare(b.dueAt))

  const subtitle =
    user.role === 'PHC_STAFF'
      ? `Incoming referrals to ${user.facilityName ?? 'your facility'}`
      : user.role === 'ASHA'
        ? 'Referrals for the patients you follow'
        : 'All referrals in the block'

  return (
    <>
      <PageHeader title="Referrals" subtitle={subtitle} />

      <div className="mb-4 flex gap-1 overflow-x-auto rounded-xl border border-line bg-surface p-1" role="tablist">
        {TABS.map((t) => {
          const count = data?.filter(t.test).length
          return (
            <button
              key={t.id}
              type="button"
              role="tab"
              aria-selected={tab === t.id}
              onClick={() => setTab(t.id)}
              className={clsx(
                'flex shrink-0 items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-semibold',
                tab === t.id ? 'bg-brand text-white' : 'text-ink-soft hover:bg-ink/5',
              )}
            >
              {t.label}
              {count != null && (
                <span className={clsx('tabular rounded-full px-1.5 text-xs', tab === t.id ? 'bg-white/20' : 'bg-ink/5')}>
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </div>

      <Card>
        {isLoading && <Spinner />}
        {error && <ErrorState error={error} onRetry={() => refetch()} />}
        {data && rows.length === 0 && <EmptyState title="Nothing here">No referrals in this view.</EmptyState>}
        {rows.length > 0 && (
          <ul className="divide-y divide-line">
            {rows.map((r) => (
              <li key={r.id}>
                <Link to={`/referrals/${r.id}`} className="flex gap-3 px-4 py-3.5 hover:bg-canvas sm:px-5">
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <PriorityBadge priority={r.priority} />
                      <ReferralStatusBadge status={r.status} />
                      {r.escalatedAt && (r.status === 'REFERRED' || r.status === 'ACKNOWLEDGED') && <EscalatedBadge />}
                      {r.overdue && !r.escalatedAt && <OverdueBadge />}
                    </div>
                    <p className="mt-2 font-semibold text-ink">
                      {r.patientName} <span className="text-sm font-medium text-muted">{r.patientAge} y</span>
                    </p>
                    <p className="mt-0.5 flex flex-wrap items-center gap-x-2 text-xs text-muted">
                      <ProgramTag program={r.program} /> {r.villageName} · ASHA {r.ashaName} · referred{' '}
                      {formatDateTime(r.createdAt)}
                    </p>
                    <p className="mt-2 line-clamp-2 text-sm text-ink-soft">{r.reason}</p>
                  </div>
                  <div className="flex shrink-0 flex-col items-end justify-between">
                    {(r.status === 'REFERRED' || r.status === 'ACKNOWLEDGED') && (
                      <span className={clsx('text-xs font-semibold', r.overdue ? 'text-critical' : 'text-muted')}>
                        Due{' '}
                        {formatRelative(r.dueAt)}
                      </span>
                    )}
                    <ChevronRight className="size-4 text-muted" aria-hidden />
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </>
  )
}
