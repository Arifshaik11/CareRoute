import { Link } from 'react-router'
import { CheckCheck, ClipboardCheck, Info, Siren, Stethoscope, UserPlus } from 'lucide-react'
import { useCompleteFollowUp, useDueList } from '../../api/queries'
import { useCurrentUser } from '../../auth/auth-context'
import { OverdueBadge, PriorityBadge, ProgramTag } from '../../components/StatusBadges'
import { Badge } from '../../components/ui/Badge'
import { Button, ButtonLink } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { PageHeader } from '../../components/ui/PageHeader'
import { Spinner } from '../../components/ui/Spinner'
import { EmptyState, ErrorState, InlineError } from '../../components/ui/States'
import { formatDateTime, formatRelative, isRecent, isToday } from '../../lib/format'
import type { FollowUp } from '../../types/api'

/** The ASHA's single prioritised list — replaces the stack of paper registers. */
export function DueListPage() {
  const user = useCurrentUser()
  const { data, isLoading, error, refetch } = useDueList()

  const overdue = data?.filter((f) => f.overdue) ?? []
  const today = data?.filter((f) => !f.overdue && isToday(f.dueAt)) ?? []
  const upcoming = data?.filter((f) => !f.overdue && !isToday(f.dueAt)) ?? []

  return (
    <>
      <PageHeader
        title={`Namaskaram, ${user.fullName.split(' ')[0]}`}
        subtitle={`${user.villageName ?? ''} · Your visits, most urgent first`}
        actions={
          <ButtonLink to="/patients/new" variant="secondary">
            <UserPlus className="size-4" aria-hidden /> Register patient
          </ButtonLink>
        }
      />

      {isLoading && <Spinner />}
      {error && <ErrorState error={error} onRetry={() => refetch()} />}

      {data && (
        <>
          <div className="mb-6 grid grid-cols-3 gap-3">
            <Counter label="Overdue" value={overdue.length} tone="critical" />
            <Counter label="Due today" value={today.length} tone="brand" />
            <Counter label="Upcoming" value={upcoming.length} tone="muted" />
          </div>

          {data.length === 0 ? (
            <Card>
              <EmptyState title="All caught up" icon={<CheckCheck className="size-5" aria-hidden />}>
                No pending visits. New tasks appear here automatically when a visit or referral needs follow-up.
              </EmptyState>
            </Card>
          ) : (
            <div className="space-y-8">
              <Section title="Overdue" items={overdue} />
              <Section title="Due today" items={today} />
              <Section title="Upcoming" items={upcoming} />
            </div>
          )}
        </>
      )}
    </>
  )
}

function Counter({ label, value, tone }: { label: string; value: number; tone: 'critical' | 'brand' | 'muted' }) {
  const color = tone === 'critical' ? 'text-critical' : tone === 'brand' ? 'text-brand' : 'text-ink-soft'
  return (
    <Card className="px-4 py-3">
      <p className={`tabular text-2xl font-extrabold ${color}`}>{value}</p>
      <p className="text-xs font-semibold text-muted">{label}</p>
    </Card>
  )
}

function Section({ title, items }: { title: string; items: FollowUp[] }) {
  if (items.length === 0) return null
  return (
    <section>
      <h2 className="mb-3 text-xs font-bold uppercase tracking-wider text-muted">
        {title} · {items.length}
      </h2>
      <div className="space-y-3">
        {items.map((f) => (
          <DueCard key={f.id} item={f} />
        ))}
      </div>
    </section>
  )
}

function DueCard({ item }: { item: FollowUp }) {
  const complete = useCompleteFollowUp()
  const fromEscalation = item.referralId != null

  return (
    <Card
      className={
        item.priority === 'CRITICAL' ? 'border-l-4 border-l-critical' : item.priority === 'HIGH' ? 'border-l-4 border-l-high' : ''
      }
    >
      <div className="p-4">
        <div className="flex flex-wrap items-center gap-2">
          <PriorityBadge priority={item.priority} />
          {item.overdue && <OverdueBadge />}
          {fromEscalation && (
            <Badge tone="critical" icon={<Siren className="size-3.5" aria-hidden />}>
              Missed referral
            </Badge>
          )}
          {isRecent(item.createdAt) && <Badge tone="accent">New</Badge>}
          <span className="ml-auto text-xs font-semibold text-muted" title={formatDateTime(item.dueAt)}>
            Due {formatRelative(item.dueAt)}
          </span>
        </div>

        <Link to={`/patients/${item.patientId}`} className="mt-3 block">
          <p className="text-base font-bold text-ink hover:text-brand">
            {item.patientName}
            <span className="ml-2 text-sm font-medium text-muted">{item.patientAge} y</span>
          </p>
        </Link>
        <div className="mt-1 flex items-center gap-2">
          <ProgramTag program={item.program} />
          <span className="text-xs text-muted">{item.villageName}</span>
        </div>

        {/* The "why": every task explains itself. */}
        <div className="mt-3 flex gap-2 rounded-lg bg-canvas px-3 py-2">
          <Info className="mt-0.5 size-4 shrink-0 text-brand" aria-hidden />
          <div className="text-sm text-ink-soft">
            <p>{item.reason}</p>
            {item.ruleCode && <p className="mt-1 text-xs text-muted">Rule: {item.ruleName ?? item.ruleCode}</p>}
          </div>
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          <ButtonLink to={`/patients/${item.patientId}/visit`} size="sm">
            <Stethoscope className="size-4" aria-hidden /> Record visit
          </ButtonLink>
          <Button
            size="sm"
            variant="secondary"
            loading={complete.isPending}
            onClick={() => complete.mutate({ id: item.id, note: 'Marked done from due list' })}
          >
            <ClipboardCheck className="size-4" aria-hidden /> Mark done
          </Button>
        </div>
        {complete.error && (
          <div className="mt-3">
            <InlineError error={complete.error} />
          </div>
        )}
      </div>
    </Card>
  )
}
