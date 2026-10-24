import { useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router'
import { Plus, Trash2 } from 'lucide-react'
import { useMeta, useRules, useSaveRule } from '../../api/queries'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { Field, Input, Select, Textarea } from '../../components/ui/Field'
import { PageHeader } from '../../components/ui/PageHeader'
import { Spinner } from '../../components/ui/Spinner'
import { ErrorState, InlineError } from '../../components/ui/States'
import { PRIORITY_LABEL, PROGRAM_LABEL } from '../../lib/labels'
import { describeCondition } from '../../lib/rules'
import type { ConditionClause, FieldMeta, Meta, Priority, Program, Rule, RuleAction, RuleRequest } from '../../types/api'

const EMPTY: RuleRequest = {
  code: '',
  name: '',
  program: undefined,
  condition: { match: 'ANY', clauses: [{ field: 'SYSTOLIC_BP', op: 'GTE', value: 140 }] },
  action: 'REFERRAL',
  priority: 'HIGH',
  dueWithinHours: 48,
  explanation: '',
  active: true,
}

export function RuleEditorPage() {
  const params = useParams()
  const id = params.id ? Number(params.id) : undefined
  const rules = useRules()
  const meta = useMeta()

  if (rules.isLoading || meta.isLoading) return <Spinner />
  if (rules.error || meta.error || !meta.data) return <ErrorState error={rules.error ?? meta.error} />

  const existing = id ? rules.data?.find((r) => r.id === id) : undefined
  if (id && !existing) return <ErrorState error={new Error('Rule not found')} />

  // key resets the form state when switching between rules
  return <RuleForm key={id ?? 'new'} id={id} initial={existing ? toRequest(existing) : EMPTY} meta={meta.data} />
}

function toRequest(r: Rule): RuleRequest {
  return {
    code: r.code,
    name: r.name,
    program: r.program,
    condition: r.condition,
    action: r.action,
    priority: r.priority,
    dueWithinHours: r.dueWithinHours,
    explanation: r.explanation,
    active: r.active,
  }
}

function defaultValue(field: FieldMeta | undefined, meta: Meta): ConditionClause['value'] {
  if (field?.kind === 'BOOLEAN') return false
  if (field?.kind === 'SYMPTOM_LIST') return meta.symptoms[0]?.code ?? ''
  return 0
}

function RuleForm({ id, initial, meta }: { id?: number; initial: RuleRequest; meta: Meta }) {
  const navigate = useNavigate()
  const save = useSaveRule()
  const [rule, setRule] = useState<RuleRequest>(initial)

  const set = <K extends keyof RuleRequest>(key: K, value: RuleRequest[K]) => setRule((r) => ({ ...r, [key]: value }))

  function setClause(index: number, patch: Partial<ConditionClause>) {
    setRule((r) => ({
      ...r,
      condition: { ...r.condition, clauses: r.condition.clauses.map((c, i) => (i === index ? { ...c, ...patch } : c)) },
    }))
  }

  function changeField(index: number, fieldCode: string) {
    const field = meta.conditionFields.find((f) => f.code === fieldCode)
    setClause(index, { field: fieldCode, op: field?.operators[0]?.code ?? 'EQ', value: defaultValue(field, meta) })
  }

  function addClause() {
    setRule((r) => ({
      ...r,
      condition: { ...r.condition, clauses: [...r.condition.clauses, { field: 'TEMPERATURE_C', op: 'GTE', value: 38 }] },
    }))
  }

  function removeClause(index: number) {
    setRule((r) => ({ ...r, condition: { ...r.condition, clauses: r.condition.clauses.filter((_, i) => i !== index) } }))
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    // Numeric values are edited as text (so "38." can be typed) and converted on save.
    const clauses = rule.condition.clauses.map((c) => {
      const kind = meta.conditionFields.find((f) => f.code === c.field)?.kind
      return kind === 'NUMBER' ? { ...c, value: Number(c.value) } : c
    })
    save.mutate(
      { id, body: { ...rule, code: rule.code.trim().toUpperCase(), condition: { ...rule.condition, clauses } } },
      { onSuccess: () => navigate('/rules') },
    )
  }

  const placeholders = meta.conditionFields.map((f) => `{${f.code}}`).join(', ')

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title={id ? 'Edit rule' : 'New rule'} subtitle="Changes apply to every visit recorded from now on." />
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <Card className="grid gap-4 p-4 sm:grid-cols-2 sm:p-5">
          <Field label="Name">
            {(fid) => <Input id={fid} value={rule.name} onChange={(e) => set('name', e.target.value)} maxLength={160} />}
          </Field>
          <Field label="Code" hint="Upper-case, e.g. MAT_HIGH_BP">
            {(fid) => (
              <Input
                id={fid}
                value={rule.code}
                onChange={(e) => set('code', e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, ''))}
                maxLength={60}
              />
            )}
          </Field>
          <Field label="Applies to">
            {(fid) => (
              <Select
                id={fid}
                value={rule.program ?? ''}
                onChange={(e) => set('program', (e.target.value || undefined) as Program | undefined)}
              >
                <option value="">All programmes</option>
                {meta.programs.map((p) => (
                  <option key={p} value={p}>
                    {PROGRAM_LABEL[p]}
                  </option>
                ))}
              </Select>
            )}
          </Field>
          <Field label="Priority">
            {(fid) => (
              <Select id={fid} value={rule.priority} onChange={(e) => set('priority', e.target.value as Priority)}>
                {meta.priorities.map((p) => (
                  <option key={p} value={p}>
                    {PRIORITY_LABEL[p]}
                  </option>
                ))}
              </Select>
            )}
          </Field>
        </Card>

        <Card>
          <CardHeader
            title="Condition"
            subtitle={describeCondition(rule.condition, meta)}
            action={
              <Select
                className="w-auto"
                value={rule.condition.match}
                onChange={(e) => set('condition', { ...rule.condition, match: e.target.value as 'ALL' | 'ANY' })}
                aria-label="Match mode"
              >
                <option value="ANY">Any clause</option>
                <option value="ALL">All clauses</option>
              </Select>
            }
          />
          <div className="space-y-3 p-4 sm:p-5">
            {rule.condition.clauses.map((clause, i) => {
              const field = meta.conditionFields.find((f) => f.code === clause.field)
              return (
                <div key={i} className="grid grid-cols-[1fr_auto] gap-2 sm:grid-cols-[2fr_1fr_1.5fr_auto]">
                  <Select value={clause.field} onChange={(e) => changeField(i, e.target.value)} aria-label="Field" className="col-span-2 sm:col-span-1">
                    {meta.conditionFields.map((f) => (
                      <option key={f.code} value={f.code}>
                        {f.label}
                      </option>
                    ))}
                  </Select>
                  <Select value={clause.op} onChange={(e) => setClause(i, { op: e.target.value })} aria-label="Operator">
                    {field?.operators.map((o) => (
                      <option key={o.code} value={o.code}>
                        {o.symbol}
                      </option>
                    ))}
                  </Select>
                  {field?.kind === 'BOOLEAN' ? (
                    <Select value={String(clause.value)} onChange={(e) => setClause(i, { value: e.target.value === 'true' })} aria-label="Value">
                      <option value="true">Yes</option>
                      <option value="false">No</option>
                    </Select>
                  ) : field?.kind === 'SYMPTOM_LIST' ? (
                    <Select value={String(clause.value)} onChange={(e) => setClause(i, { value: e.target.value })} aria-label="Value">
                      {meta.symptoms.map((s) => (
                        <option key={s.code} value={s.code}>
                          {s.label}
                        </option>
                      ))}
                    </Select>
                  ) : (
                    <Input
                      inputMode="decimal"
                      value={String(clause.value)}
                      onChange={(e) => setClause(i, { value: e.target.value.replace(/[^\d.]/g, '') })}
                      aria-label="Value"
                    />
                  )}
                  <Button
                    variant="ghost"
                    size="md"
                    onClick={() => removeClause(i)}
                    disabled={rule.condition.clauses.length === 1}
                    aria-label="Remove clause"
                  >
                    <Trash2 className="size-4" aria-hidden />
                  </Button>
                </div>
              )
            })}
            <Button variant="secondary" size="sm" onClick={addClause} disabled={rule.condition.clauses.length >= 10}>
              <Plus className="size-4" aria-hidden /> Add clause
            </Button>
          </div>
        </Card>

        <Card className="grid gap-4 p-4 sm:grid-cols-2 sm:p-5">
          <Field label="Then">
            {(fid) => (
              <Select id={fid} value={rule.action} onChange={(e) => set('action', e.target.value as RuleAction)}>
                <option value="REFERRAL">Refer to the PHC</option>
                <option value="FOLLOW_UP">Add a follow-up visit for the ASHA</option>
              </Select>
            )}
          </Field>
          <Field label="Within (hours)" hint="Deadline; open referrals past it escalate">
            {(fid) => (
              <Input
                id={fid}
                inputMode="numeric"
                value={String(rule.dueWithinHours)}
                onChange={(e) => set('dueWithinHours', Number(e.target.value.replace(/\D/g, '')) || 0)}
              />
            )}
          </Field>
          <div className="sm:col-span-2">
            <Field label="Explanation shown to staff" hint={`Use placeholders to insert observed values: ${placeholders}`}>
              {(fid) => (
                <Textarea id={fid} value={rule.explanation} onChange={(e) => set('explanation', e.target.value)} maxLength={500} />
              )}
            </Field>
          </div>
          <label className="flex items-center gap-2 text-sm font-semibold text-ink-soft">
            <input type="checkbox" className="size-4 accent-[var(--color-brand)]" checked={rule.active} onChange={(e) => set('active', e.target.checked)} />
            Active
          </label>
        </Card>

        <InlineError error={save.error} />
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={() => navigate('/rules')}>
            Cancel
          </Button>
          <Button type="submit" loading={save.isPending}>
            Save rule
          </Button>
        </div>
      </form>
    </div>
  )
}
