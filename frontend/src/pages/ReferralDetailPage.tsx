import { useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router'
import { CheckCircle2, ClipboardCheck, Hand, Info } from 'lucide-react'
import { useReferral, useReferralAction, type ReferralAction } from '../api/queries'
import { useCurrentUser } from '../auth/auth-context'
import { ReferralTimeline } from '../components/ReferralTimeline'
import { ScreeningNotice } from '../components/ScreeningNotice'
import { EscalatedBadge, OverdueBadge, PriorityBadge, ProgramTag, ReferralStatusBadge } from '../components/StatusBadges'
import { Button } from '../components/ui/Button'
import { Card, CardHeader } from '../components/ui/Card'
import { Field, Textarea } from '../components/ui/Field'
import { PageHeader } from '../components/ui/PageHeader'
import { Spinner } from '../components/ui/Spinner'
import { ErrorState, InlineError } from '../components/ui/States'
import { formatDateTime, formatRelative } from '../lib/format'
import type { Referral } from '../types/api'

export function ReferralDetailPage() {
  const id = Number(useParams().id)
  const user = useCurrentUser()
  const { data, isLoading, error, refetch } = useReferral(id)

  if (isLoading) return <Spinner />
  if (error || !data) return <ErrorState error={error} onRetry={() => refetch()} />

  const open = data.status === 'REFERRED' || data.status === 'ACKNOWLEDGED'

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader
        title={`Referral #${data.id}`}
        subtitle={
          <span className="inline-flex flex-wrap items-center gap-2">
            <ReferralStatusBadge status={data.status} />
            <PriorityBadge priority={data.priority} />
            {data.escalatedAt && open && <EscalatedBadge />}
            {data.overdue && !data.escalatedAt && <OverdueBadge />}
          </span>
        }
      />

      <div className="grid gap-4 md:grid-cols-5">
        <div className="space-y-4 md:col-span-3">
          <Card className="p-4 sm:p-5">
            <Link to={`/patients/${data.patientId}`} className="text-lg font-bold text-ink hover:text-brand">
              {data.patientName} <span className="text-sm font-medium text-muted">{data.patientAge} y</span>
            </Link>
            <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-muted">
              <ProgramTag program={data.program} /> {data.villageName} · ASHA {data.ashaName}
            </p>
            <div className="mt-4 flex gap-2 rounded-lg bg-canvas px-3 py-2.5">
              <Info className="mt-0.5 size-4 shrink-0 text-brand" aria-hidden />
              <div className="text-sm text-ink-soft">
                <p>{data.reason}</p>
                {data.ruleCode && <p className="mt-1 text-xs text-muted">Rule: {data.ruleCode}</p>}
              </div>
            </div>
            <dl className="mt-4 grid grid-cols-2 gap-3 text-sm">
              <div>
                <dt className="text-xs text-muted">Referred</dt>
                <dd className="font-semibold">{formatDateTime(data.createdAt)}</dd>
              </div>
              <div>
                <dt className="text-xs text-muted">Expected by</dt>
                <dd className={data.overdue ? 'font-semibold text-critical' : 'font-semibold'}>
                  {formatDateTime(data.dueAt)}
                  {open && <span className="block text-xs font-medium">({formatRelative(data.dueAt)})</span>}
                </dd>
              </div>
            </dl>
          </Card>

          {user.role === 'PHC_STAFF' && <FacilityActions referral={data} />}
          {data.outcomeNote && (
            <Card className="p-4 sm:p-5">
              <p className="text-xs font-bold uppercase tracking-wider text-muted">Outcome</p>
              <p className="mt-1 text-sm text-ink">{data.outcomeNote}</p>
            </Card>
          )}
          <ScreeningNotice />
        </div>

        <Card className="md:col-span-2">
          <CardHeader title="Timeline" subtitle={`To ${data.facilityName}`} />
          <div className="p-5">
            <ReferralTimeline events={data.events ?? []} />
          </div>
        </Card>
      </div>
    </div>
  )
}

/** The PHC side of the loop: acknowledge, confirm arrival, record outcome. */
function FacilityActions({ referral }: { referral: Referral }) {
  const action = useReferralAction()
  const [note, setNote] = useState('')

  const next: { action: ReferralAction; label: string; icon: ReactNode; needsNote: boolean } | null =
    referral.status === 'REFERRED'
      ? { action: 'acknowledge', label: 'Acknowledge — expecting patient', icon: <Hand className="size-4" aria-hidden />, needsNote: false }
      : referral.status === 'ACKNOWLEDGED'
        ? { action: 'arrive', label: 'Patient has arrived', icon: <CheckCircle2 className="size-4" aria-hidden />, needsNote: false }
        : referral.status === 'ARRIVED'
          ? { action: 'close', label: 'Close with outcome', icon: <ClipboardCheck className="size-4" aria-hidden />, needsNote: true }
          : null

  if (!next) return null

  function run(a: ReferralAction) {
    action.mutate({ id: referral.id, action: a, note: note.trim() || undefined }, { onSuccess: () => setNote('') })
  }

  return (
    <Card>
      <CardHeader title="Update referral" subtitle="Your updates close the loop for the ASHA and supervisor." />
      <div className="space-y-3 p-4 sm:p-5">
        <Field label={next.needsNote ? 'Outcome note (required)' : 'Note (optional)'}>
          {(id) => (
            <Textarea
              id={id}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              maxLength={500}
              placeholder={next.needsNote ? 'e.g. Reviewed by MO, BP monitoring advised' : ''}
            />
          )}
        </Field>
        <InlineError error={action.error} />
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => run(next.action)} loading={action.isPending} disabled={next.needsNote && !note.trim()}>
            {next.icon} {next.label}
          </Button>
          {referral.status === 'REFERRED' && (
            <Button variant="secondary" onClick={() => run('arrive')} disabled={action.isPending}>
              Patient already here
            </Button>
          )}
        </div>
      </div>
    </Card>
  )
}
