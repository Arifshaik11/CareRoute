import { Link } from 'react-router'
import clsx from 'clsx'
import { ClipboardList, Pencil, Plus, Route as RouteIcon } from 'lucide-react'
import { useMeta, useRules, useToggleRule } from '../../api/queries'
import { PriorityBadge } from '../../components/StatusBadges'
import { Badge } from '../../components/ui/Badge'
import { ButtonLink } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { PageHeader } from '../../components/ui/PageHeader'
import { Spinner } from '../../components/ui/Spinner'
import { ErrorState, InlineError } from '../../components/ui/States'
import { PROGRAM_LABEL } from '../../lib/labels'
import { describeCondition, formatHours } from '../../lib/rules'
import type { Rule } from '../../types/api'

export function RulesPage() {
  const { data, isLoading, error, refetch } = useRules()
  const meta = useMeta()
  const toggle = useToggleRule()

  return (
    <>
      <PageHeader
        title="Follow-up & referral rules"
        subtitle="Transparent, configurable rules. Every flag in CareRoute traces back to one of these."
        actions={
          <ButtonLink to="/rules/new">
            <Plus className="size-4" aria-hidden /> New rule
          </ButtonLink>
        }
      />
      <InlineError error={toggle.error} />
      {isLoading && <Spinner />}
      {error && <ErrorState error={error} onRetry={() => refetch()} />}
      <div className="mt-2 space-y-3">
        {data?.map((rule) => (
          <RuleCard
            key={rule.id}
            rule={rule}
            condition={describeCondition(rule.condition, meta.data)}
            onToggle={() => toggle.mutate({ id: rule.id, active: !rule.active })}
            toggling={toggle.isPending && toggle.variables?.id === rule.id}
          />
        ))}
      </div>
    </>
  )
}

function RuleCard({ rule, condition, onToggle, toggling }: { rule: Rule; condition: string; onToggle: () => void; toggling: boolean }) {
  return (
    <Card className={clsx('p-4 sm:p-5', !rule.active && 'opacity-60')}>
      <div className="flex flex-wrap items-start gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="font-bold text-ink">{rule.name}</h2>
            <code className="rounded bg-canvas px-1.5 py-0.5 text-xs text-muted">{rule.code}</code>
          </div>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <PriorityBadge priority={rule.priority} />
            <Badge tone="brand" icon={rule.action === 'REFERRAL' ? <RouteIcon className="size-3.5" /> : <ClipboardList className="size-3.5" />}>
              {rule.action === 'REFERRAL' ? 'Refer' : 'Follow up'} within {formatHours(rule.dueWithinHours)}
            </Badge>
            <Badge>{rule.program ? PROGRAM_LABEL[rule.program] : 'All programmes'}</Badge>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <label className="flex cursor-pointer items-center gap-2 text-sm font-semibold text-ink-soft">
            <input type="checkbox" className="peer sr-only" checked={rule.active} onChange={onToggle} disabled={toggling} />
            <span
              className={clsx(
                'relative h-6 w-10 rounded-full transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-brand',
                rule.active ? 'bg-brand' : 'bg-line',
              )}
              aria-hidden
            >
              <span className={clsx('absolute top-1 size-4 rounded-full bg-white transition-all', rule.active ? 'left-5' : 'left-1')} />
            </span>
            {rule.active ? 'Active' : 'Off'}
          </label>
          <Link to={`/rules/${rule.id}`} className="grid size-9 place-items-center rounded-lg text-ink-soft hover:bg-ink/5" aria-label={`Edit ${rule.name}`}>
            <Pencil className="size-4" aria-hidden />
          </Link>
        </div>
      </div>
      <p className="mt-3 rounded-lg bg-canvas px-3 py-2 text-sm">
        <span className="font-semibold text-ink">If </span>
        <span className="text-ink-soft">{condition}</span>
      </p>
      <p className="mt-2 text-sm text-muted">“{rule.explanation}”</p>
    </Card>
  )
}
