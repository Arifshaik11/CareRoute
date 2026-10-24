import { useState, useCallback, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router'
import clsx from 'clsx'
import { CheckCircle2, ClipboardList, Route as RouteIcon, Send } from 'lucide-react'
import { useMeta, usePatient, useRecordVisit } from '../../api/queries'
import { ScreeningNotice } from '../../components/ScreeningNotice'
import { PriorityBadge, ProgramTag } from '../../components/StatusBadges'
import { VoiceButton } from '../../components/VoiceButton'
import { Button, ButtonLink } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { Field, Input, Textarea } from '../../components/ui/Field'
import { PageHeader } from '../../components/ui/PageHeader'
import { Spinner } from '../../components/ui/Spinner'
import { ErrorState, InlineError } from '../../components/ui/States'
import { useVoiceInput, type VoiceFields } from '../../hooks/useVoiceInput'
import { formatDateTime } from '../../lib/format'
import { OUTCOME_LABEL } from '../../lib/labels'
import type { PatientDetail, Program, RecordVisitRequest, VisitResult } from '../../types/api'

const SHOWS_BP: Program[] = ['MATERNAL', 'NCD']

export function RecordVisitPage() {
  const id = Number(useParams().id)
  const patient = usePatient(id)
  const [result, setResult] = useState<VisitResult | null>(null)

  if (patient.isLoading) return <Spinner />
  if (patient.error || !patient.data) return <ErrorState error={patient.error} onRetry={() => patient.refetch()} />

  return (
    <div className="mx-auto max-w-2xl">
      {result ? (
        <VisitOutcome patientId={id} result={result} />
      ) : (
        <VisitForm detail={patient.data} onSaved={setResult} />
      )}
    </div>
  )
}

/** Estimate today's pregnancy weeks from the last recorded value, to save the ASHA a calculation. */
function estimatedWeeks(detail: PatientDetail): string {
  const last = detail.visits.find((v) => v.pregnancyWeeks != null)
  if (!last?.pregnancyWeeks) return ''
  const elapsedWeeks = Math.floor((Date.now() - new Date(last.visitedAt).getTime()) / (7 * 86_400_000))
  return String(Math.min(last.pregnancyWeeks + elapsedWeeks, 45))
}

function VisitForm({ detail, onSaved }: { detail: PatientDetail; onSaved: (r: VisitResult) => void }) {
  const { patient } = detail
  const meta = useMeta()
  const record = useRecordVisit(patient.id)

  const [systolic, setSystolic] = useState('')
  const [diastolic, setDiastolic] = useState('')
  const [weight, setWeight] = useState('')
  const [temperature, setTemperature] = useState('')
  const [weeks, setWeeks] = useState(() => estimatedWeeks(detail))
  const [doseTaken, setDoseTaken] = useState<boolean | null>(null)
  const [symptoms, setSymptoms] = useState<string[]>([])
  const [notes, setNotes] = useState('')
  const [formError, setFormError] = useState<string | null>(null)
  const [usedVoice, setUsedVoice] = useState(false)

  const onVoiceResult = useCallback((fields: VoiceFields) => {
    if (fields.systolic != null)       setSystolic(String(fields.systolic))
    if (fields.diastolic != null)      setDiastolic(String(fields.diastolic))
    if (fields.weightKg != null)       setWeight(String(fields.weightKg))
    if (fields.temperatureC != null)   setTemperature(String(fields.temperatureC))
    if (fields.pregnancyWeeks != null) setWeeks(String(fields.pregnancyWeeks))
    if (fields.doseTaken != null)      setDoseTaken(fields.doseTaken)
    if (fields.symptoms?.length)       setSymptoms((s) => [...new Set([...s, ...fields.symptoms!])])
    setUsedVoice(true)
  }, [])

  const voice = useVoiceInput(onVoiceResult)

  const showBp = SHOWS_BP.includes(patient.program)
  const isMaternal = patient.program === 'MATERNAL'
  const isTb = patient.program === 'TB'

  function toggleSymptom(code: string) {
    setSymptoms((s) => (s.includes(code) ? s.filter((c) => c !== code) : [...s, code]))
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    setFormError(null)
    const num = (v: string) => (v.trim() === '' ? undefined : Number(v))
    const body: RecordVisitRequest = {
      systolicBp: num(systolic),
      diastolicBp: num(diastolic),
      weightKg: num(weight),
      temperatureC: num(temperature),
      pregnancyWeeks: isMaternal ? num(weeks) : undefined,
      doseTaken: isTb && doseTaken !== null ? doseTaken : undefined,
      symptoms,
      notes: notes.trim() || undefined,
      source: usedVoice ? 'VOICE' : 'MANUAL',
    }
    if ((body.systolicBp == null) !== (body.diastolicBp == null)) {
      setFormError('Enter both BP values, or leave both empty.')
      return
    }
    if (isTb && doseTaken === null) {
      setFormError('Record whether today’s TB dose was taken.')
      return
    }
    record.mutate(body, { onSuccess: onSaved })
  }

  return (
    <>
      <PageHeader
        title="Record visit"
        subtitle={
          <span className="inline-flex items-center gap-2">
            {patient.fullName} · {patient.age} y <ProgramTag program={patient.program} />
          </span>
        }
      />
      <div className="mb-4">
        <ScreeningNotice />
      </div>

      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        {voice.isSupported && (
          <div className="flex justify-end">
            <VoiceButton voice={voice} />
          </div>
        )}
        <Card>
          <CardHeader title="Observations" subtitle="Leave a field empty if you didn't measure it." />
          <div className="grid gap-4 p-4 sm:grid-cols-2 sm:p-5">
            {showBp && (
              <Field label="Blood pressure (mmHg)" hint="Systolic / diastolic">
                {(id) => (
                  <div className="flex items-center gap-2">
                    <Input id={id} inputMode="numeric" placeholder="120" value={systolic}
                      onChange={(e) => setSystolic(e.target.value.replace(/\D/g, '').slice(0, 3))} aria-label="Systolic" />
                    <span className="text-muted">/</span>
                    <Input inputMode="numeric" placeholder="80" value={diastolic}
                      onChange={(e) => setDiastolic(e.target.value.replace(/\D/g, '').slice(0, 3))} aria-label="Diastolic" />
                  </div>
                )}
              </Field>
            )}
            {isMaternal && (
              <Field label="Pregnancy (weeks)" hint={weeks ? 'Estimated from the last visit — correct if needed' : undefined}>
                {(id) => (
                  <Input id={id} inputMode="numeric" value={weeks}
                    onChange={(e) => setWeeks(e.target.value.replace(/\D/g, '').slice(0, 2))} />
                )}
              </Field>
            )}
            <Field label="Temperature (°C)">
              {(id) => (
                <Input id={id} inputMode="decimal" placeholder="36.8" value={temperature}
                  onChange={(e) => setTemperature(e.target.value.replace(/[^\d.]/g, '').slice(0, 4))} />
              )}
            </Field>
            <Field label="Weight (kg)">
              {(id) => (
                <Input id={id} inputMode="decimal" value={weight}
                  onChange={(e) => setWeight(e.target.value.replace(/[^\d.]/g, '').slice(0, 5))} />
              )}
            </Field>
            {isTb && (
              <fieldset className="sm:col-span-2">
                <legend className="mb-1.5 text-sm font-semibold text-ink-soft">Today’s TB dose taken?</legend>
                <div className="flex gap-2">
                  {[true, false].map((v) => (
                    <button key={String(v)} type="button" onClick={() => setDoseTaken(v)}
                      className={clsx('h-10 flex-1 rounded-lg border text-sm font-semibold',
                        doseTaken === v ? 'border-brand bg-brand text-white' : 'border-line bg-surface text-ink')}
                      aria-pressed={doseTaken === v}>
                      {v ? 'Yes, taken' : 'No, missed'}
                    </button>
                  ))}
                </div>
              </fieldset>
            )}
          </div>
        </Card>

        <Card>
          <CardHeader title="Symptoms reported" subtitle="Tick everything the patient reports today." />
          <div className="grid gap-2 p-4 sm:grid-cols-2 sm:p-5">
            {meta.data?.symptoms.map((s) => {
              const checked = symptoms.includes(s.code)
              return (
                <label key={s.code}
                  className={clsx('flex cursor-pointer items-center gap-3 rounded-lg border px-3 py-2.5 transition-colors',
                    checked ? 'border-brand bg-brand-soft' : 'border-line bg-surface hover:bg-canvas')}>
                  <input type="checkbox" className="size-4 accent-[var(--color-brand)]" checked={checked}
                    onChange={() => toggleSymptom(s.code)} />
                  <span className="leading-tight">
                    <span className="block text-sm font-semibold text-ink">{s.label}</span>
                    <span className="font-telugu block text-xs text-muted">{s.labelTe}</span>
                  </span>
                </label>
              )
            })}
            {meta.isLoading && <Spinner label="Loading symptoms…" />}
          </div>
        </Card>

        <Card className="p-4 sm:p-5">
          <Field label="Notes (optional)">
            {(id) => <Textarea id={id} value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={1000} />}
          </Field>
        </Card>

        {formError && (
          <p className="rounded-lg bg-critical-soft px-3 py-2 text-sm font-medium text-critical" role="alert">
            {formError}
          </p>
        )}
        <InlineError error={record.error} />

        <div className="flex justify-end gap-2">
          <ButtonLink to={`/patients/${patient.id}`} variant="ghost">Cancel</ButtonLink>
          <Button type="submit" size="lg" loading={record.isPending}>
            Save visit
          </Button>
        </div>
      </form>
    </>
  )
}

/** What the rules decided — shown immediately, with the reason for every flag. */
function VisitOutcome({ patientId, result }: { patientId: number; result: VisitResult }) {
  const navigate = useNavigate()
  const referral = result.outcomes.find((o) => o.result === 'REFERRAL_CREATED')

  return (
    <>
      <PageHeader title="Visit saved" subtitle={formatDateTime(result.visit.visitedAt)} />

      <Card className={clsx('mb-4 p-5', referral ? 'border-critical/40 bg-critical-soft/40' : 'border-good/30 bg-good-soft/50')}>
        <div className="flex items-start gap-3">
          {referral ? (
            <Send className="mt-0.5 size-6 shrink-0 text-critical" aria-hidden />
          ) : (
            <CheckCircle2 className="mt-0.5 size-6 shrink-0 text-good" aria-hidden />
          )}
          <div>
            <p className="text-lg font-bold text-ink">
              {referral ? 'Referral sent to the PHC' : result.outcomes.length ? 'Next visit scheduled' : 'No flags raised'}
            </p>
            <p className="mt-1 text-sm text-ink-soft">
              {referral
                ? `The PHC can see this referral now. If the patient hasn't arrived by ${formatDateTime(referral.dueAt)}, it will come back to your due list.`
                : result.outcomes.length
                  ? 'The follow-up below has been added to your due list.'
                  : 'All observations were within the configured thresholds.'}
              {result.completedFollowUps > 0 &&
                ` This visit also completed ${result.completedFollowUps} pending task${result.completedFollowUps > 1 ? 's' : ''}.`}
            </p>
          </div>
        </div>
      </Card>

      {result.outcomes.length > 0 && (
        <Card className="mb-4">
          <CardHeader title="Why" subtitle="Each rule that fired, in plain language" />
          <ul className="divide-y divide-line">
            {result.outcomes.map((o) => (
              <li key={`${o.ruleCode}-${o.result}`} className="p-4 sm:px-5">
                <div className="flex flex-wrap items-center gap-2">
                  <PriorityBadge priority={o.priority} />
                  <span className="text-sm font-bold text-ink">{o.ruleName}</span>
                </div>
                <p className="mt-2 text-sm text-ink-soft">{o.explanation}</p>
                <p className="mt-2 flex items-center gap-1.5 text-xs font-semibold text-brand">
                  {o.action === 'REFERRAL' ? <RouteIcon className="size-3.5" aria-hidden /> : <ClipboardList className="size-3.5" aria-hidden />}
                  {OUTCOME_LABEL[o.result]}
                  {o.dueAt && o.result !== 'COVERED_BY_REFERRAL' && ` · due ${formatDateTime(o.dueAt)}`}
                </p>
              </li>
            ))}
          </ul>
        </Card>
      )}

      <ScreeningNotice compact />

      <div className="mt-5 flex flex-wrap justify-end gap-2">
        <Button variant="secondary" onClick={() => navigate(`/patients/${patientId}`)}>
          View patient
        </Button>
        <Button onClick={() => navigate('/due')}>Back to due list</Button>
      </div>
    </>
  )
}
