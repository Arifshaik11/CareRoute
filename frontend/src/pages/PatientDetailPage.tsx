import type { ReactNode } from 'react'
import { useParams } from 'react-router'
import { Eye, Phone, ShieldCheck, Stethoscope } from 'lucide-react'
import { useAccessLog, useMeta, usePatient } from '../api/queries'
import { useCurrentUser } from '../auth/auth-context'
import { ReferralTimeline } from '../components/ReferralTimeline'
import { ScreeningNotice } from '../components/ScreeningNotice'
import { EscalatedBadge, OverdueBadge, PriorityBadge, ProgramTag, ReferralStatusBadge } from '../components/StatusBadges'
import { ButtonLink } from '../components/ui/Button'
import { Card, CardHeader } from '../components/ui/Card'
import { PageHeader } from '../components/ui/PageHeader'
import { Spinner } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { formatDate, formatDateTime, formatRelative } from '../lib/format'
import { PROGRAM_LABEL, SEX_LABEL } from '../lib/labels'
import type { Visit } from '../types/api'

export function PatientDetailPage() {
  const id = Number(useParams().id)
  const user = useCurrentUser()
  const { data, isLoading, error, refetch } = usePatient(id)
  const accessLog = useAccessLog(id, user.role === 'SUPERVISOR')

  if (isLoading) return <Spinner />
  if (error || !data) return <ErrorState error={error} onRetry={() => refetch()} />

  const { patient } = data
  const pending = data.followUps.filter((f) => f.status === 'PENDING')

  return (
    <>
      <PageHeader
        title={patient.fullName}
        subtitle={
          <span className="inline-flex flex-wrap items-center gap-2">
            {patient.age} y · {SEX_LABEL[patient.sex]} · {patient.villageName} · ASHA {patient.ashaName}
            <ProgramTag program={patient.program} />
          </span>
        }
        actions={
          user.role === 'ASHA' && (
            <ButtonLink to={`/patients/${patient.id}/visit`}>
              <Stethoscope className="size-4" aria-hidden /> Record visit
            </ButtonLink>
          )
        }
      />

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <CardHeader title="Open tasks" subtitle="Pending follow-ups for this patient" />
            {pending.length === 0 ? (
              <p className="px-5 py-4 text-sm text-muted">No pending follow-ups.</p>
            ) : (
              <ul className="divide-y divide-line">
                {pending.map((f) => (
                  <li key={f.id} className="px-4 py-3 sm:px-5">
                    <div className="flex flex-wrap items-center gap-2">
                      <PriorityBadge priority={f.priority} />
                      {f.overdue && <OverdueBadge />}
                      <span className="ml-auto text-xs font-semibold text-muted">Due {formatRelative(f.dueAt)}</span>
                    </div>
                    <p className="mt-2 text-sm text-ink-soft">{f.reason}</p>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card>
            <CardHeader title="Referrals" subtitle="Every referral and what happened to it" />
            {data.referrals.length === 0 ? (
              <p className="px-5 py-4 text-sm text-muted">No referrals.</p>
            ) : (
              <ul className="divide-y divide-line">
                {data.referrals.map((r) => (
                  <li key={r.id} className="px-4 py-4 sm:px-5">
                    <div className="mb-3 flex flex-wrap items-center gap-2">
                      <ReferralStatusBadge status={r.status} />
                      <PriorityBadge priority={r.priority} />
                      {r.escalatedAt && r.status !== 'ARRIVED' && r.status !== 'CLOSED' && <EscalatedBadge />}
                      <span className="ml-auto text-xs text-muted">to {r.facilityName}</span>
                    </div>
                    {r.events && <ReferralTimeline events={r.events} />}
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card>
            <CardHeader title="Visit history" subtitle={`${data.visits.length} visit${data.visits.length === 1 ? '' : 's'}`} />
            {data.visits.length === 0 ? (
              <EmptyState title="No visits yet" />
            ) : (
              <ul className="divide-y divide-line">
                {data.visits.map((v) => (
                  <VisitRow key={v.id} visit={v} />
                ))}
              </ul>
            )}
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="p-4 sm:p-5">
            <h2 className="text-sm font-bold">Details</h2>
            <dl className="mt-3 space-y-2 text-sm">
              <Row label="Programme" value={PROGRAM_LABEL[patient.program]} />
              <Row
                label="Phone"
                value={
                  patient.phone ? (
                    <span className="inline-flex items-center gap-1">
                      <Phone className="size-3.5" aria-hidden /> {patient.phone}
                    </span>
                  ) : (
                    '—'
                  )
                }
              />
              <Row label="Registered" value={formatDate(patient.registeredAt)} />
              {data.notes && <Row label="Notes" value={data.notes} />}
            </dl>
            <div className="mt-4 flex items-start gap-2 rounded-lg bg-good-soft px-3 py-2 text-xs text-good">
              <ShieldCheck className="mt-0.5 size-4 shrink-0" aria-hidden />
              <span>Consent recorded {formatDate(data.consentAt)}</span>
            </div>
          </Card>

          <ScreeningNotice />

          {user.role === 'SUPERVISOR' && (
            <Card>
              <CardHeader title="Access log" subtitle="Who viewed or changed this record" />
              {accessLog.isLoading && <Spinner />}
              {accessLog.data && (
                <ul className="max-h-80 divide-y divide-line overflow-y-auto">
                  {accessLog.data.map((e, i) => (
                    <li key={i} className="flex gap-2 px-4 py-2.5 text-xs">
                      <Eye className="mt-0.5 size-3.5 shrink-0 text-muted" aria-hidden />
                      <div>
                        <p className="font-semibold text-ink">
                          {e.action.replace(/_/g, ' ').toLowerCase()} · {e.actorName}
                        </p>
                        <p className="text-muted">{formatDateTime(e.at)}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          )}
        </div>
      </div>
    </>
  )
}

function Row({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="flex justify-between gap-3">
      <dt className="text-muted">{label}</dt>
      <dd className="text-right font-medium text-ink">{value}</dd>
    </div>
  )
}

function VisitRow({ visit }: { visit: Visit }) {
  const meta = useMeta()
  const symptomLabel = (code: string) => meta.data?.symptoms.find((s) => s.code === code)?.label ?? code
  const readings = [
    visit.systolicBp != null && `BP ${visit.systolicBp}/${visit.diastolicBp}`,
    visit.pregnancyWeeks != null && `${visit.pregnancyWeeks} wk`,
    visit.temperatureC != null && `${visit.temperatureC} °C`,
    visit.weightKg != null && `${visit.weightKg} kg`,
    visit.doseTaken != null && (visit.doseTaken ? 'Dose taken' : 'Dose missed'),
  ].filter(Boolean)

  return (
    <li className="px-4 py-3 sm:px-5">
      <div className="flex justify-between gap-3">
        <p className="tabular text-sm font-semibold text-ink">{readings.join(' · ') || 'No measurements'}</p>
        <p className="shrink-0 text-xs text-muted">{formatDateTime(visit.visitedAt)}</p>
      </div>
      {visit.symptoms.length > 0 && (
        <p className="mt-1 text-xs text-ink-soft">Reported: {visit.symptoms.map(symptomLabel).join(', ')}</p>
      )}
      {visit.notes && <p className="mt-1 text-xs text-muted">{visit.notes}</p>}
    </li>
  )
}
