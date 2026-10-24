import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/auth-context'
import { homePathFor } from '../auth/home-path'
import { Logo } from '../components/AppShell'
import { Button } from '../components/ui/Button'
import { Field, Input } from '../components/ui/Field'
import { InlineError } from '../components/ui/States'
import { ROLE_LABEL } from '../lib/labels'
import type { Role } from '../types/api'

const DEMO_PASSWORD = 'demo@123'

const DEMO_ACCOUNTS: { username: string; name: string; role: Role; scope: string }[] = [
  { username: 'asha.sunitha', name: 'Sunitha K', role: 'ASHA', scope: 'Ramapuram village' },
  { username: 'phc.kiran', name: 'Dr. Kiran V', role: 'PHC_STAFF', scope: 'Ramapuram PHC' },
  { username: 'supervisor.anil', name: 'Anil P', role: 'SUPERVISOR', scope: 'Ramapuram Block' },
]

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to={homePathFor(user.role)} replace />

  async function signIn(u: string, p: string) {
    setError(null)
    setSubmitting(true)
    try {
      const signedIn = await login(u, p)
      navigate(homePathFor(signedIn.role), { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    void signIn(username.trim(), password)
  }

  return (
    <div className="grid min-h-dvh lg:grid-cols-[1.1fr_1fr]">
      <section className="relative hidden overflow-hidden bg-brand p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="flex items-center gap-2">
          <span className="rounded-lg bg-white/10 px-2 py-1 text-xs font-bold uppercase tracking-widest">CareRoute</span>
        </div>
        <div className="max-w-md">
          <p className="text-sm font-semibold uppercase tracking-widest text-accent">Community health follow-up</p>
          <h1 className="mt-4 text-4xl font-extrabold leading-tight">
            No patient should fall through the cracks between the village and the clinic.
          </h1>
          <p className="mt-5 text-base text-white/75">
            One prioritised due list for ASHA workers. Closed-loop referrals that escalate when a patient
            doesn't arrive. Transparent rules the medical officer controls.
          </p>
        </div>
        <p className="text-xs text-white/60">
          Prototype with synthetic data only. Screening information, not a diagnosis.
        </p>
        <svg className="pointer-events-none absolute -right-24 -bottom-24 size-[420px] opacity-15" viewBox="0 0 200 200" aria-hidden>
          <path d="M20 160 C 60 40, 140 140, 180 30" fill="none" stroke="white" strokeWidth="10" strokeLinecap="round" />
          <circle cx="20" cy="160" r="14" fill="white" />
          <circle cx="180" cy="30" r="14" fill="white" />
        </svg>
      </section>

      <section className="flex items-center justify-center px-5 py-10">
        <div className="w-full max-w-sm">
          <div className="mb-8">
            <Logo />
            <h2 className="mt-6 text-2xl font-extrabold tracking-tight">Sign in</h2>
            <p className="mt-1 text-sm text-muted">Use your CareRoute account, or pick a demo role below.</p>
          </div>

          <form onSubmit={onSubmit} className="space-y-4" noValidate>
            <Field label="Username">
              {(id) => (
                <Input
                  id={id}
                  autoComplete="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              )}
            </Field>
            <Field label="Password">
              {(id) => (
                <Input
                  id={id}
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              )}
            </Field>
            <InlineError error={error} />
            <Button type="submit" size="lg" className="w-full" loading={submitting} disabled={!username || !password}>
              Sign in
            </Button>
          </form>

          <div className="mt-8">
            <p className="mb-2 text-xs font-bold uppercase tracking-wider text-muted">Demo accounts</p>
            <div className="space-y-2">
              {DEMO_ACCOUNTS.map((a) => (
                <button
                  key={a.username}
                  type="button"
                  disabled={submitting}
                  onClick={() => void signIn(a.username, DEMO_PASSWORD)}
                  className="flex w-full items-center justify-between rounded-lg border border-line bg-surface px-3 py-2.5 text-left transition-colors hover:border-brand/40 hover:bg-brand-soft/40 disabled:opacity-60"
                >
                  <span>
                    <span className="block text-sm font-semibold">{a.name}</span>
                    <span className="block text-xs text-muted">{a.scope}</span>
                  </span>
                  <span className="text-xs font-semibold text-brand">{ROLE_LABEL[a.role]}</span>
                </button>
              ))}
            </div>
            <p className="mt-2 text-xs text-muted">
              Password for all demo accounts: <code className="font-semibold text-ink">{DEMO_PASSWORD}</code>
            </p>
          </div>
        </div>
      </section>
    </div>
  )
}
