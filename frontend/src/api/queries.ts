import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { http } from '../lib/http'
import type {
  AuditEntry,
  CreatePatientRequest,
  Dashboard,
  FastForwardResult,
  FollowUp,
  Meta,
  PatientDetail,
  PatientSummary,
  Program,
  RecordVisitRequest,
  Referral,
  ReferralStatus,
  Rule,
  RuleRequest,
  VisitResult,
} from '../types/api'

/** Central query keys, so invalidation after a mutation is never guesswork. */
export const keys = {
  meta: ['meta'] as const,
  dueList: ['follow-ups', 'due'] as const,
  patients: (program?: Program, q?: string) => ['patients', { program, q }] as const,
  patient: (id: number) => ['patient', id] as const,
  accessLog: (id: number) => ['patient', id, 'access-log'] as const,
  referrals: (status?: ReferralStatus, escalated?: boolean) => ['referrals', { status, escalated }] as const,
  referral: (id: number) => ['referral', id] as const,
  rules: ['rules'] as const,
  dashboard: ['dashboard'] as const,
}

// ---------------------------------------------------------------- reference data

export function useMeta() {
  return useQuery({
    queryKey: keys.meta,
    queryFn: async () => (await http.get<Meta>('/meta')).data,
    staleTime: Infinity,
  })
}

// ---------------------------------------------------------------- ASHA due list

export function useDueList() {
  return useQuery({
    queryKey: keys.dueList,
    queryFn: async () => (await http.get<FollowUp[]>('/follow-ups/due')).data,
    refetchInterval: 30_000,
  })
}

export function useCompleteFollowUp() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, note }: { id: number; note?: string }) =>
      (await http.post<FollowUp>(`/follow-ups/${id}/complete`, { note })).data,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: keys.dueList })
      qc.invalidateQueries({ queryKey: keys.patient(data.patientId) })
    },
  })
}

// ---------------------------------------------------------------- patients

export function usePatients(program?: Program, q?: string) {
  return useQuery({
    queryKey: keys.patients(program, q),
    queryFn: async () =>
      (await http.get<PatientSummary[]>('/patients', { params: { program, q: q || undefined } })).data,
  })
}

export function usePatient(id: number) {
  return useQuery({
    queryKey: keys.patient(id),
    queryFn: async () => (await http.get<PatientDetail>(`/patients/${id}`)).data,
    enabled: Number.isFinite(id),
  })
}

export function useAccessLog(id: number, enabled: boolean) {
  return useQuery({
    queryKey: keys.accessLog(id),
    queryFn: async () => (await http.get<{ entries: AuditEntry[] }>(`/patients/${id}/access-log`)).data.entries,
    enabled,
  })
}

export function useRegisterPatient() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (body: CreatePatientRequest) => (await http.post<PatientSummary>('/patients', body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['patients'] }),
  })
}

export function useRecordVisit(patientId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (body: RecordVisitRequest) =>
      (await http.post<VisitResult>(`/patients/${patientId}/visits`, body)).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: keys.patient(patientId) })
      qc.invalidateQueries({ queryKey: keys.dueList })
      qc.invalidateQueries({ queryKey: ['referrals'] })
    },
  })
}

// ---------------------------------------------------------------- referrals

export function useReferrals(status?: ReferralStatus, escalated?: boolean) {
  return useQuery({
    queryKey: keys.referrals(status, escalated),
    queryFn: async () =>
      (await http.get<Referral[]>('/referrals', { params: { status, escalated: escalated || undefined } })).data,
    refetchInterval: 30_000,
  })
}

export function useReferral(id: number) {
  return useQuery({
    queryKey: keys.referral(id),
    queryFn: async () => (await http.get<Referral>(`/referrals/${id}`)).data,
    enabled: Number.isFinite(id),
  })
}

export type ReferralAction = 'acknowledge' | 'arrive' | 'close'

export function useReferralAction() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, action, note }: { id: number; action: ReferralAction; note?: string }) =>
      (await http.post<Referral>(`/referrals/${id}/${action}`, { note })).data,
    onSuccess: (data) => {
      qc.setQueryData(keys.referral(data.id), data)
      qc.invalidateQueries({ queryKey: ['referrals'] })
      qc.invalidateQueries({ queryKey: keys.patient(data.patientId) })
    },
  })
}

// ---------------------------------------------------------------- rules

export function useRules() {
  return useQuery({
    queryKey: keys.rules,
    queryFn: async () => (await http.get<Rule[]>('/rules')).data,
  })
}

export function useSaveRule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, body }: { id?: number; body: RuleRequest }) =>
      id ? (await http.put<Rule>(`/rules/${id}`, body)).data : (await http.post<Rule>('/rules', body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: keys.rules }),
  })
}

export function useToggleRule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
      (await http.patch<Rule>(`/rules/${id}/active`, { active })).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: keys.rules }),
  })
}

// ---------------------------------------------------------------- supervisor

export function useDashboard() {
  return useQuery({
    queryKey: keys.dashboard,
    queryFn: async () => (await http.get<Dashboard>('/dashboard')).data,
    refetchInterval: 30_000,
  })
}

export function useFastForward() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (hours: number) =>
      (await http.post<FastForwardResult>('/demo/fast-forward', { hours })).data,
    onSuccess: () => qc.invalidateQueries(),
  })
}

export function useResetDemo() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      await http.post('/demo/reset')
    },
    onSuccess: () => qc.invalidateQueries(),
  })
}
