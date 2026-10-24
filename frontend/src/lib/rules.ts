import type { Meta, RuleCondition } from '../types/api'

/** Renders a condition as plain language, e.g. "Systolic BP (mmHg) ≥ 140 OR Diastolic BP (mmHg) ≥ 90". */
export function describeCondition(condition: RuleCondition, meta?: Meta): string {
  const joiner = condition.match === 'ALL' ? ' AND ' : ' OR '
  return condition.clauses
    .map((c) => {
      const field = meta?.conditionFields.find((f) => f.code === c.field)
      const op = field?.operators.find((o) => o.code === c.op)?.symbol ?? c.op
      let value: string
      if (typeof c.value === 'boolean') value = c.value ? 'yes' : 'no'
      else if (field?.kind === 'SYMPTOM_LIST')
        value = meta?.symptoms.find((s) => s.code === c.value)?.label.toLowerCase() ?? String(c.value)
      else value = String(c.value)
      return `${field?.label ?? c.field} ${op} ${value}`
    })
    .join(joiner)
}

export function formatHours(hours: number): string {
  if (hours % 24 === 0) {
    const days = hours / 24
    return `${days} day${days === 1 ? '' : 's'}`
  }
  return `${hours} hour${hours === 1 ? '' : 's'}`
}
