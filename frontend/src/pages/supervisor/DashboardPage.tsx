import { useState } from 'react'
import { Link } from 'react-router'
import clsx from 'clsx'
import { AlertOctagon, FastForward, RotateCcw, Siren } from 'lucide-react'
import { useDashboard, useFastForward, useResetDemo } from '../../api/queries'
import { PriorityBadge } from '../../components/StatusBadges'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { PageHeader } from '../../components/ui/PageHeader'
import { Spinner } from '../../components/ui/Spinner'
import { ErrorState, InlineError } from '../../components/ui/States'
import { formatDateTime, formatPercent } from '../../lib/format'
import { PROGRAM_LABEL } from '../../lib/labels'
import type { Dashboard, Program } from '../../types/api'

export function DashboardPage() {
  const { data, isLoading, error, refetch } = useDashboard()

  return (
    <>
      <PageHeader
        title="Block overview"
        subtitle={data ? `Ramapuram Block · updated ${formatDateTime(data.generatedAt)}` : 'Ramapuram Block'}
      />
      {isLoading && <Spinner />}
      {error && <ErrorState error={error} onRetry={() => refetch()} />}
      {data && <DashboardBody data={data} />}
    </>
  )
}

function DashboardBody({ data }: { data: Dashboard }) {
  const { kpis } = data
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <StatTile label="Referral completion" value={formatPercent(kpis.referralCompletionRate)} hint="Patients who reached the facility" />
        <StatTile label="Escalated referrals" value={kpis.escalatedReferrals} hint="Overdue, patient not arrived" alert={kpis.escalatedReferrals > 0} />
        <StatTile label="Overdue follow-ups" value={kpis.overdueFollowUps} hint={`of ${kpis.pendingFollowUps} pending`} alert={kpis.overdueFollowUps > 0} />
        <StatTile label="Visits, last 7 days" value={kpis.visitsLast7Days} hint={`${kpis.patients} patients followed`} />
      </div>

      <div className="grid gap-4 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <CardHeader title="Referral loop" subtitle="How far each referral got — the gap is where patients are lost" />
          <Funnel funnel={data.funnel} />
        </Card>
        <Card className="lg:col-span-2">
          <CardHeader title="Patients by programme" />
          <ProgramBars counts={data.patientsByProgram} />
        </Card>
      </div>

      <Card>
        <CardHeader title="By village" subtitle="Where to send support first" />
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-sm">
            <thead>
              <tr className="border-b border-line text-left text-xs uppercase tracking-wider text-muted">
                <th className="px-5 py-2.5 font-semibold">Village</th>
                <th className="px-3 py-2.5 font-semibold">ASHA</th>
                <th className="px-3 py-2.5 text-right font-semibold">Patients</th>
                <th className="px-3 py-2.5 text-right font-semibold">Overdue tasks</th>
                <th className="px-3 py-2.5 text-right font-semibold">Open referrals</th>
                <th className="px-3 py-2.5 text-right font-semibold">Escalated</th>
                <th className="px-5 py-2.5 text-right font-semibold">Completion</th>
              </tr>
            </thead>
            <tbody className="tabular divide-y divide-line">
              {data.villages.map((v) => (
                <tr key={v.villageId}>
                  <td className="px-5 py-3 font-semibold">{v.villageName}</td>
                  <td className="px-3 py-3 text-ink-soft">{v.ashaName}</td>
                  <td className="px-3 py-3 text-right">{v.patients}</td>
                  <td className={clsx('px-3 py-3 text-right', v.overdueFollowUps > 0 && 'font-bold text-critical')}>
                    {v.overdueFollowUps} <span className="font-normal text-muted">/ {v.pendingFollowUps}</span>
                  </td>
                  <td className="px-3 py-3 text-right">{v.openReferrals}</td>
                  <td className={clsx('px-3 py-3 text-right', v.escalatedReferrals > 0 && 'font-bold text-critical')}>
                    {v.escalatedReferrals}
                  </td>
                  <td className="px-5 py-3 text-right font-semibold">{formatPercent(v.referralCompletionRate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      <div className="grid gap-4 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <CardHeader
            title="Active escalations"
            subtitle="Referred, overdue, and the patient hasn't arrived — sent back to the ASHA"
            action={<Link to="/referrals" className="text-xs font-semibold text-brand hover:underline">All referrals</Link>}
          />
          {data.activeEscalations.length === 0 ? (
            <p className="px-5 py-4 text-sm text-muted">No active escalations.</p>
          ) : (
            <ul className="divide-y divide-line">
              {data.activeEscalations.map((r) => (
                <li key={r.id}>
                  <Link to={`/referrals/${r.id}`} className="flex items-start gap-3 px-4 py-3 hover:bg-canvas sm:px-5">
                    <Siren className="mt-0.5 size-4 shrink-0 text-critical" aria-hidden />
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold text-ink">
                        {r.patientName} · <span className="font-medium text-muted">{r.villageName}</span>
                      </p>
                      <p className="line-clamp-1 text-xs text-ink-soft">{r.reason}</p>
                    </div>
                    <PriorityBadge priority={r.priority} />
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
        <DemoControls />
      </div>
    </div>
  )
}

function StatTile({ label, value, hint, alert = false }: { label: string; value: number | string; hint: string; alert?: boolean }) {
  return (
    <Card className="p-4">
      <p className="text-xs font-semibold text-muted">{label}</p>
      <p className={clsx('tabular mt-1 flex items-center gap-1.5 text-3xl font-extrabold', alert ? 'text-critical' : 'text-ink')}>
        {alert && <AlertOctagon className="size-5" aria-label="Needs attention" />}
        {value}
      </p>
      <p className="mt-0.5 text-xs text-muted">{hint}</p>
    </Card>
  )
}

/** Single-series funnel: one hue, direct labels, the drop-off called out in words. */
function Funnel({ funnel }: { funnel: Dashboard['funnel'] }) {
  const stages = [
    { label: 'Referred', value: funnel.referred },
    { label: 'Acknowledged by PHC', value: funnel.acknowledged },
    { label: 'Patient arrived', value: funnel.arrived },
    { label: 'Closed with outcome', value: funnel.closed },
  ]
  const max = Math.max(funnel.referred, 1)

  return (
    <div className="space-y-3 p-4 sm:p-5">
      {stages.map((s) => {
        const pct = s.value / max
        return (
          <div key={s.label} className="group" title={`${s.label}: ${s.value} (${Math.round(pct * 100)}% of referred)`}>
            <div className="mb-1 flex justify-between text-xs">
              <span className="font-semibold text-ink-soft">{s.label}</span>
              <span className="tabular text-ink">
                <span className="font-bold">{s.value}</span> <span className="text-muted">· {Math.round(pct * 100)}%</span>
              </span>
            </div>
            <div className="h-3 rounded-full bg-canvas">
              <div
                className="h-3 rounded-full bg-brand transition-[width] duration-500 group-hover:bg-brand-strong"
                style={{ width: `${Math.max(pct * 100, s.value > 0 ? 2 : 0)}%` }}
              />
            </div>
          </div>
        )
      })}
      <div className="mt-2 flex items-start gap-2 rounded-lg bg-critical-soft px-3 py-2 text-sm text-critical">
        <Siren className="mt-0.5 size-4 shrink-0" aria-hidden />
        <span>
          <span className="font-bold">{funnel.lostOrOverdue}</span> referral{funnel.lostOrOverdue === 1 ? ' is' : 's are'} past
          due with the patient not yet at the facility.
        </span>
      </div>
    </div>
  )
}

function ProgramBars({ counts }: { counts: Record<Program, number> }) {
  const entries = (Object.entries(counts) as [Program, number][]).sort((a, b) => b[1] - a[1])
  const max = Math.max(...entries.map(([, n]) => n), 1)
  return (
    <ul className="space-y-3 p-4 sm:p-5">
      {entries.map(([program, n]) => (
        <li key={program} title={`${PROGRAM_LABEL[program]}: ${n}`}>
          <div className="mb-1 flex justify-between text-xs">
            <span className="font-semibold text-ink-soft">{PROGRAM_LABEL[program]}</span>
            <span className="tabular font-bold">{n}</span>
          </div>
          <div className="h-2.5 rounded-full bg-canvas">
            <div className="h-2.5 rounded-full bg-brand/70" style={{ width: `${(n / max) * 100}%` }} />
          </div>
        </li>
      ))}
    </ul>
  )
}

/** Demo-only: simulate time passing so the escalation can be shown live. */
function DemoControls() {
  const fastForward = useFastForward()
  const reset = useResetDemo()
  const [confirmReset, setConfirmReset] = useState(false)

  return (
    <Card className="border-dashed lg:col-span-2">
      <CardHeader title="Demo controls" subtitle="Synthetic data only" />
      <div className="space-y-3 p-4 sm:p-5">
        <p className="text-sm text-ink-soft">
          Move every open deadline 48 hours closer, as if two days had passed, then run escalation.
        </p>
        <Button onClick={() => fastForward.mutate(48)} loading={fastForward.isPending} className="w-full">
          <FastForward className="size-4" aria-hidden /> Fast-forward 48 hours
        </Button>
        {fastForward.data && (
          <p className="text-xs font-semibold text-good" role="status">
            Done — {fastForward.data.escalated} referral{fastForward.data.escalated === 1 ? '' : 's'} escalated.
          </p>
        )}
        <InlineError error={fastForward.error} />

        <div className="border-t border-line pt-3">
          {confirmReset ? (
            <div className="flex gap-2">
              <Button
                variant="danger"
                size="sm"
                loading={reset.isPending}
                onClick={() => reset.mutate(undefined, { onSettled: () => setConfirmReset(false) })}
              >
                Yes, reset all patient data
              </Button>
              <Button variant="ghost" size="sm" onClick={() => setConfirmReset(false)}>
                Cancel
              </Button>
            </div>
          ) : (
            <Button variant="ghost" size="sm" onClick={() => setConfirmReset(true)}>
              <RotateCcw className="size-4" aria-hidden /> Reset synthetic data
            </Button>
          )}
          <InlineError error={reset.error} />
        </div>
      </div>
    </Card>
  )
}
