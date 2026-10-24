import { Link } from 'react-router'
import { EmptyState } from '../components/ui/States'

export function NotFoundPage() {
  return (
    <EmptyState title="Page not found">
      <Link to="/" className="font-semibold text-brand hover:underline">
        Go to your home screen
      </Link>
    </EmptyState>
  )
}
