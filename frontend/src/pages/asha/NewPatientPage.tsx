import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ShieldCheck } from 'lucide-react'
import { useRegisterPatient } from '../../api/queries'
import { useCurrentUser } from '../../auth/auth-context'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { Field, Input, Select, Textarea } from '../../components/ui/Field'
import { PageHeader } from '../../components/ui/PageHeader'
import { InlineError } from '../../components/ui/States'
import { PROGRAM_LABEL, SEX_LABEL } from '../../lib/labels'
import type { Program, Sex } from '../../types/api'

const PROGRAMS: Program[] = ['MATERNAL', 'TB', 'CHILD_IMMUNIZATION', 'NCD']
const SEXES: Sex[] = ['FEMALE', 'MALE', 'OTHER']

export function NewPatientPage() {
  const user = useCurrentUser()
  const navigate = useNavigate()
  const register = useRegisterPatient()

  const [fullName, setFullName] = useState('')
  const [age, setAge] = useState('')
  const [sex, setSex] = useState<Sex>('FEMALE')
  const [phone, setPhone] = useState('')
  const [program, setProgram] = useState<Program>('MATERNAL')
  const [notes, setNotes] = useState('')
  const [consent, setConsent] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  function validate(): boolean {
    const next: Record<string, string> = {}
    if (!fullName.trim()) next.fullName = 'Enter the patient’s name'
    const ageNum = Number(age)
    if (age === '' || !Number.isInteger(ageNum) || ageNum < 0 || ageNum > 120) next.age = 'Enter an age between 0 and 120'
    if (phone && !/^[6-9]\d{9}$/.test(phone)) next.phone = 'Enter a 10-digit mobile number'
    if (!consent) next.consent = 'Consent is required before registering'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!validate()) return
    register.mutate(
      {
        fullName: fullName.trim(),
        age: Number(age),
        sex,
        phone: phone || undefined,
        program,
        consentGiven: consent,
        notes: notes.trim() || undefined,
      },
      { onSuccess: (p) => navigate(`/patients/${p.id}/visit`, { replace: true }) },
    )
  }

  return (
    <div className="mx-auto max-w-xl">
      <PageHeader title="Register patient" subtitle={`Will be added to ${user.villageName ?? 'your village'}`} />
      <Card className="p-5">
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <Field label="Full name" error={errors.fullName}>
            {(id) => <Input id={id} value={fullName} onChange={(e) => setFullName(e.target.value)} maxLength={120} />}
          </Field>

          <div className="grid grid-cols-2 gap-3">
            <Field label="Age (years)" error={errors.age}>
              {(id) => (
                <Input id={id} inputMode="numeric" value={age} onChange={(e) => setAge(e.target.value.replace(/\D/g, ''))} />
              )}
            </Field>
            <Field label="Sex">
              {(id) => (
                <Select id={id} value={sex} onChange={(e) => setSex(e.target.value as Sex)}>
                  {SEXES.map((s) => (
                    <option key={s} value={s}>
                      {SEX_LABEL[s]}
                    </option>
                  ))}
                </Select>
              )}
            </Field>
          </div>

          <Field label="Programme" hint="Which national programme you follow this patient under.">
            {(id) => (
              <Select id={id} value={program} onChange={(e) => setProgram(e.target.value as Program)}>
                {PROGRAMS.map((p) => (
                  <option key={p} value={p}>
                    {PROGRAM_LABEL[p]}
                  </option>
                ))}
              </Select>
            )}
          </Field>

          <Field label="Mobile number (optional)" error={errors.phone} hint="Used only by you and the PHC to contact the patient.">
            {(id) => (
              <Input
                id={id}
                inputMode="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 10))}
              />
            )}
          </Field>

          <Field label="Notes (optional)">
            {(id) => <Textarea id={id} value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={1000} />}
          </Field>

          <label className="flex items-start gap-3 rounded-lg border border-line bg-canvas p-3">
            <input
              type="checkbox"
              className="mt-1 size-4 accent-[var(--color-brand)]"
              checked={consent}
              onChange={(e) => setConsent(e.target.checked)}
            />
            <span className="text-sm">
              <span className="flex items-center gap-1 font-semibold text-ink">
                <ShieldCheck className="size-4 text-brand" aria-hidden /> Patient consent recorded
              </span>
              <span className="text-muted">
                I explained what is recorded and who can see it (their ASHA and the PHC), and the patient agreed.
              </span>
              {errors.consent && <span className="mt-1 block text-xs font-medium text-critical">{errors.consent}</span>}
            </span>
          </label>

          <InlineError error={register.error} />

          <div className="flex justify-end gap-2 pt-2">
            <Button variant="ghost" onClick={() => navigate(-1)}>
              Cancel
            </Button>
            <Button type="submit" loading={register.isPending}>
              Register & record first visit
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
