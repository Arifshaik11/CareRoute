import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import type { Role } from '../types/api'
import { useAuth } from './auth-context'
import { FullPageSpinner } from '../components/ui/Spinner'
import { homePathFor } from './home-path'

interface Props {
  children: ReactNode
  roles?: Role[]
}

/** Gate for signed-in routes; optionally restricted to certain roles. */
export function RequireAuth({ children, roles }: Props) {
  const { user, status } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <FullPageSpinner />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (roles && !roles.includes(user.role)) return <Navigate to={homePathFor(user.role)} replace />
  return <>{children}</>
}
