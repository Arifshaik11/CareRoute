import type { ReactNode } from 'react'
import { AlertOctagon, AlertTriangle, ArrowUpCircle, CheckCircle2, Circle, Clock, Send, Siren } from 'lucide-react'
import { Badge, type Tone } from './ui/Badge'
import { PRIORITY_LABEL, PROGRAM_SHORT, REFERRAL_STATUS_LABEL } from '../lib/labels'
import type { Priority, Program, ReferralStatus } from '../types/api'

const priorityTone: Record<Priority, Tone> = {
  CRITICAL: 'critical',
  HIGH: 'high',
  MEDIUM: 'medium',
  LOW: 'low',
}

const priorityIcon: Record<Priority, ReactNode> = {
  CRITICAL: <AlertOctagon className="size-3.5" aria-hidden />,
  HIGH: <AlertTriangle className="size-3.5" aria-hidden />,
  MEDIUM: <ArrowUpCircle className="size-3.5" aria-hidden />,
  LOW: <Circle className="size-3.5" aria-hidden />,
}

/** Urgency is always icon + word, never colour alone. */
export function PriorityBadge({ priority }: { priority: Priority }) {
  return (
    <Badge tone={priorityTone[priority]} icon={priorityIcon[priority]}>
      {PRIORITY_LABEL[priority]}
    </Badge>
  )
}

const statusTone: Record<ReferralStatus, Tone> = {
  REFERRED: 'brand',
  ACKNOWLEDGED: 'accent',
  ARRIVED: 'good',
  CLOSED: 'neutral',
}

const statusIcon: Record<ReferralStatus, ReactNode> = {
  REFERRED: <Send className="size-3.5" aria-hidden />,
  ACKNOWLEDGED: <Clock className="size-3.5" aria-hidden />,
  ARRIVED: <CheckCircle2 className="size-3.5" aria-hidden />,
  CLOSED: <CheckCircle2 className="size-3.5" aria-hidden />,
}

export function ReferralStatusBadge({ status }: { status: ReferralStatus }) {
  return (
    <Badge tone={statusTone[status]} icon={statusIcon[status]}>
      {REFERRAL_STATUS_LABEL[status]}
    </Badge>
  )
}

export function EscalatedBadge() {
  return (
    <Badge tone="critical" icon={<Siren className="size-3.5" aria-hidden />}>
      Escalated
    </Badge>
  )
}

export function OverdueBadge() {
  return (
    <Badge tone="critical" icon={<Clock className="size-3.5" aria-hidden />}>
      Overdue
    </Badge>
  )
}

export function ProgramTag({ program }: { program: Program }) {
  return <Badge tone="neutral">{PROGRAM_SHORT[program]}</Badge>
}
