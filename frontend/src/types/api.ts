// Types mirroring the backend DTOs (com.kvanttik.careroute.*). Keep in sync with the API.

export type Role = 'ASHA' | 'PHC_STAFF' | 'SUPERVISOR'
export type Program = 'MATERNAL' | 'TB' | 'CHILD_IMMUNIZATION' | 'NCD'
export type Sex = 'FEMALE' | 'MALE' | 'OTHER'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type FollowUpStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED'
export type ReferralStatus = 'REFERRED' | 'ACKNOWLEDGED' | 'ARRIVED' | 'CLOSED'
export type ReferralEventType = ReferralStatus | 'ESCALATED'
export type RuleAction = 'FOLLOW_UP' | 'REFERRAL'
export type MatchMode = 'ALL' | 'ANY'
export type VisitSource = 'MANUAL' | 'VOICE'
export type OutcomeResult =
  | 'FOLLOW_UP_CREATED'
  | 'REFERRAL_CREATED'
  | 'COVERED_BY_REFERRAL'
  | 'ALREADY_REFERRED'

export interface User {
  id: number
  username: string
  fullName: string
  role: Role
  villageId?: number
  villageName?: string
  facilityId?: number
  facilityName?: string
}

export interface LoginResponse {
  token: string
  expiresAt: string
  user: User
}

export interface PatientSummary {
  id: number
  fullName: string
  age: number
  sex: Sex
  program: Program
  villageName: string
  ashaName: string
  phone?: string
  registeredAt: string
}

export interface Visit {
  id: number
  visitedAt: string
  recordedBy: string
  systolicBp?: number
  diastolicBp?: number
  weightKg?: number
  temperatureC?: number
  pregnancyWeeks?: number
  doseTaken?: boolean
  symptoms: string[]
  notes?: string
  source: VisitSource
}

export interface FollowUp {
  id: number
  patientId: number
  patientName: string
  patientAge: number
  program: Program
  villageName: string
  reason: string
  priority: Priority
  dueAt: string
  status: FollowUpStatus
  overdue: boolean
  completedAt?: string
  completionNote?: string
  ruleCode?: string
  ruleName?: string
  referralId?: number
  createdAt: string
}

export interface ReferralEvent {
  type: ReferralEventType
  actorName: string
  note?: string
  occurredAt: string
}

export interface Referral {
  id: number
  patientId: number
  patientName: string
  patientAge: number
  program: Program
  villageName: string
  ashaName: string
  facilityName: string
  reason: string
  priority: Priority
  status: ReferralStatus
  createdAt: string
  dueAt: string
  overdue: boolean
  escalatedAt?: string
  acknowledgedAt?: string
  arrivedAt?: string
  closedAt?: string
  outcomeNote?: string
  ruleCode?: string
  events?: ReferralEvent[]
}

export interface PatientDetail {
  patient: PatientSummary
  consentGiven: boolean
  consentAt?: string
  notes?: string
  visits: Visit[]
  followUps: FollowUp[]
  referrals: Referral[]
}

export interface RuleOutcome {
  ruleCode: string
  ruleName: string
  action: RuleAction
  priority: Priority
  explanation: string
  result: OutcomeResult
  dueAt?: string
  followUpId?: number
  referralId?: number
}

export interface VisitResult {
  visit: Visit
  outcomes: RuleOutcome[]
  completedFollowUps: number
}

export interface CreatePatientRequest {
  fullName: string
  age: number
  sex: Sex
  phone?: string
  program: Program
  consentGiven: boolean
  notes?: string
}

export interface RecordVisitRequest {
  systolicBp?: number
  diastolicBp?: number
  weightKg?: number
  temperatureC?: number
  pregnancyWeeks?: number
  doseTaken?: boolean
  symptoms: string[]
  notes?: string
  source: VisitSource
}

export interface ConditionClause {
  field: string
  op: string
  value: number | boolean | string
}

export interface RuleCondition {
  match: MatchMode
  clauses: ConditionClause[]
}

export interface Rule {
  id: number
  code: string
  name: string
  program?: Program
  condition: RuleCondition
  action: RuleAction
  priority: Priority
  dueWithinHours: number
  explanation: string
  active: boolean
  updatedAt: string
}

export type RuleRequest = Omit<Rule, 'id' | 'updatedAt'>

export interface SymptomMeta {
  code: string
  label: string
  labelTe: string
}

export interface FieldMeta {
  code: string
  label: string
  kind: 'NUMBER' | 'BOOLEAN' | 'SYMPTOM_LIST'
  operators: { code: string; symbol: string }[]
}

export interface Meta {
  programs: Program[]
  priorities: Priority[]
  symptoms: SymptomMeta[]
  conditionFields: FieldMeta[]
}

export interface Dashboard {
  generatedAt: string
  kpis: {
    patients: number
    visitsLast7Days: number
    pendingFollowUps: number
    overdueFollowUps: number
    openReferrals: number
    escalatedReferrals: number
    referralCompletionRate?: number
  }
  funnel: {
    referred: number
    acknowledged: number
    arrived: number
    closed: number
    lostOrOverdue: number
  }
  villages: {
    villageId: number
    villageName: string
    ashaName: string
    patients: number
    pendingFollowUps: number
    overdueFollowUps: number
    openReferrals: number
    escalatedReferrals: number
    referralCompletionRate?: number
  }[]
  patientsByProgram: Record<Program, number>
  activeEscalations: Referral[]
}

export interface AuditEntry {
  action: string
  actorName: string
  actorRole?: Role
  details?: string
  at: string
}

export interface FastForwardResult {
  hours: number
  referralsShifted: number
  followUpsShifted: number
  escalated: number
}

/** RFC 7807 problem body returned by the API on errors. */
export interface ApiProblem {
  title?: string
  detail?: string
  status?: number
  errors?: Record<string, string>
}
