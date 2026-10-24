import { Navigate, Route, Routes } from 'react-router'
import { RequireAuth } from './auth/RequireAuth'
import { useAuth } from './auth/auth-context'
import { homePathFor } from './auth/home-path'
import { AppShell } from './components/AppShell'
import { FullPageSpinner } from './components/ui/Spinner'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PatientsPage } from './pages/PatientsPage'
import { PatientDetailPage } from './pages/PatientDetailPage'
import { ReferralsPage } from './pages/ReferralsPage'
import { ReferralDetailPage } from './pages/ReferralDetailPage'
import { DueListPage } from './pages/asha/DueListPage'
import { NewPatientPage } from './pages/asha/NewPatientPage'
import { RecordVisitPage } from './pages/asha/RecordVisitPage'
import { DashboardPage } from './pages/supervisor/DashboardPage'
import { RulesPage } from './pages/supervisor/RulesPage'
import { RuleEditorPage } from './pages/supervisor/RuleEditorPage'

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <AppShell />
          </RequireAuth>
        }
      >
        <Route index element={<HomeRedirect />} />

        {/* ASHA */}
        <Route path="due" element={<RequireAuth roles={['ASHA']}><DueListPage /></RequireAuth>} />
        <Route path="patients/new" element={<RequireAuth roles={['ASHA']}><NewPatientPage /></RequireAuth>} />
        <Route path="patients/:id/visit" element={<RequireAuth roles={['ASHA']}><RecordVisitPage /></RequireAuth>} />

        {/* Shared, scoped by the API */}
        <Route path="patients" element={<PatientsPage />} />
        <Route path="patients/:id" element={<PatientDetailPage />} />
        <Route path="referrals" element={<ReferralsPage />} />
        <Route path="referrals/:id" element={<ReferralDetailPage />} />

        {/* Supervisor */}
        <Route path="dashboard" element={<RequireAuth roles={['SUPERVISOR']}><DashboardPage /></RequireAuth>} />
        <Route path="rules" element={<RequireAuth roles={['SUPERVISOR']}><RulesPage /></RequireAuth>} />
        <Route path="rules/new" element={<RequireAuth roles={['SUPERVISOR']}><RuleEditorPage /></RequireAuth>} />
        <Route path="rules/:id" element={<RequireAuth roles={['SUPERVISOR']}><RuleEditorPage /></RequireAuth>} />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

function HomeRedirect() {
  const { user, status } = useAuth()
  if (status === 'loading') return <FullPageSpinner />
  return <Navigate to={user ? homePathFor(user.role) : '/login'} replace />
}
