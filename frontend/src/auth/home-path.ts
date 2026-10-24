import type { Role } from '../types/api'

export function homePathFor(role: Role): string {
  switch (role) {
    case 'ASHA':
      return '/due'
    case 'PHC_STAFF':
      return '/referrals'
    case 'SUPERVISOR':
      return '/dashboard'
  }
}
