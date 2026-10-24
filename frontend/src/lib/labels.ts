import type { OutcomeResult, Priority, Program, ReferralStatus, Role, Sex } from '../types/api'

export const PROGRAM_LABEL: Record<Program, string> = {
  MATERNAL: 'Maternal care',
  TB: 'TB treatment',
  CHILD_IMMUNIZATION: 'Child health',
  NCD: 'NCD (BP / diabetes)',
}

export const PROGRAM_SHORT: Record<Program, string> = {
  MATERNAL: 'Maternal',
  TB: 'TB',
  CHILD_IMMUNIZATION: 'Child',
  NCD: 'NCD',
}

export const ROLE_LABEL: Record<Role, string> = {
  ASHA: 'ASHA worker',
  PHC_STAFF: 'PHC staff',
  SUPERVISOR: 'Block supervisor',
}

export const SEX_LABEL: Record<Sex, string> = {
  FEMALE: 'Female',
  MALE: 'Male',
  OTHER: 'Other',
}

export const PRIORITY_LABEL: Record<Priority, string> = {
  CRITICAL: 'Critical',
  HIGH: 'High',
  MEDIUM: 'Medium',
  LOW: 'Low',
}

export const REFERRAL_STATUS_LABEL: Record<ReferralStatus, string> = {
  REFERRED: 'Referred',
  ACKNOWLEDGED: 'Acknowledged',
  ARRIVED: 'Arrived',
  CLOSED: 'Closed',
}

export const OUTCOME_LABEL: Record<OutcomeResult, string> = {
  FOLLOW_UP_CREATED: 'Follow-up added to due list',
  REFERRAL_CREATED: 'Referral sent to PHC',
  COVERED_BY_REFERRAL: 'Included in the referral above',
  ALREADY_REFERRED: 'Patient already has an open referral',
}
