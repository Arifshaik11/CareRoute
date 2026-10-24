import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router'
import { ChevronRight, Search, UserPlus } from 'lucide-react'
import { usePatients } from '../api/queries'
import { useCurrentUser } from '../auth/auth-context'
import { ProgramTag } from '../components/StatusBadges'
import { ButtonLink } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input, Select } from '../components/ui/Field'
import { PageHeader } from '../components/ui/PageHeader'
import { Spinner } from '../components/ui/Spinner'
import { EmptyState, ErrorState } from '../components/ui/States'
import { useLang } from '../contexts/LangContext'
import { T } from '../lib/i18n'
import { PROGRAM_LABEL, SEX_LABEL } from '../lib/labels'
import type { Program } from '../types/api'

const PROGRAMS: Program[] = ['MATERNAL', 'TB', 'CHILD_IMMUNIZATION', 'NCD']

export function PatientsPage() {
  const user = useCurrentUser()
  const [q, setQ] = useState('')
  const [program, setProgram] = useState<Program | ''>('')
  const deferredQ = useDeferredValue(q)
  const { data, isLoading, error, refetch } = usePatients(program || undefined, deferredQ.trim())
  const { t } = useLang()

  const scope =
    user.role === 'ASHA'
      ? t(T.patientsSubtitleAsha.en, T.patientsSubtitleAsha.te)
      : user.role === 'PHC_STAFF'
        ? t(T.patientsSubtitlePhc.en, T.patientsSubtitlePhc.te)
        : t(T.patientsSubtitleSuper.en, T.patientsSubtitleSuper.te)

  return (
    <>
      <PageHeader
        title={t(T.patientsTitle.en, T.patientsTitle.te)}
        subtitle={scope}
        actions={
          user.role === 'ASHA' && (
            <ButtonLink to="/patients/new">
              <UserPlus className="size-4" aria-hidden /> {t(T.registerPatient.en, T.registerPatient.te)}
            </ButtonLink>
          )
        }
      />

      <div className="mb-4 flex flex-col gap-2 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted" aria-hidden />
          <Input
            className="pl-9"
            placeholder={t(T.searchByName.en, T.searchByName.te)}
            value={q}
            onChange={(e) => setQ(e.target.value)}
            aria-label={t(T.searchByName.en, T.searchByName.te)}
          />
        </div>
        <Select
          className="sm:w-56"
          value={program}
          onChange={(e) => setProgram(e.target.value as Program | '')}
          aria-label={t(T.programme.en, T.programme.te)}
        >
          <option value="">{t(T.allProgrammes.en, T.allProgrammes.te)}</option>
          {PROGRAMS.map((p) => (
            <option key={p} value={p}>
              {PROGRAM_LABEL[p]}
            </option>
          ))}
        </Select>
      </div>

      <Card>
        {isLoading && <Spinner />}
        {error && <ErrorState error={error} onRetry={() => refetch()} />}
        {data && data.length === 0 && (
          <EmptyState title={t(T.noPatients.en, T.noPatients.te)}>
            {t(T.noPatientsHint.en, T.noPatientsHint.te)}
          </EmptyState>
        )}
        {data && data.length > 0 && (
          <ul className="divide-y divide-line">
            {data.map((p) => (
              <li key={p.id}>
                <Link to={`/patients/${p.id}`} className="flex items-center gap-3 px-4 py-3 hover:bg-canvas sm:px-5">
                  <div className="grid size-9 shrink-0 place-items-center rounded-full bg-brand-soft text-sm font-bold text-brand">
                    {p.fullName.charAt(0)}
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-semibold text-ink">{p.fullName}</p>
                    <p className="text-xs text-muted">
                      {p.age} y · {SEX_LABEL[p.sex]} · {p.villageName}
                      {user.role !== 'ASHA' && ` · ASHA ${p.ashaName}`}
                    </p>
                  </div>
                  <ProgramTag program={p.program} />
                  <ChevronRight className="size-4 text-muted" aria-hidden />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>
      {data && <p className="mt-2 text-xs text-muted">{data.length} patient{data.length === 1 ? '' : 's'}</p>}
    </>
  )
}
