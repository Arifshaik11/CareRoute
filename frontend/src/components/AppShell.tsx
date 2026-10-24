import { NavLink, Outlet } from 'react-router'
import clsx from 'clsx'
import { ClipboardList, Languages, LayoutDashboard, LogOut, Route, SlidersHorizontal, Users } from 'lucide-react'
import type { ReactNode } from 'react'
import { useCurrentUser, useAuth } from '../auth/auth-context'
import { useLang } from '../contexts/LangContext'
import { useEscalationAlerts } from '../hooks/useSSE'
import { T } from '../lib/i18n'
import { ROLE_LABEL } from '../lib/labels'
import type { Role } from '../types/api'

interface NavItem {
  to: string
  labelEn: string
  labelTe: string
  icon: ReactNode
}

const NAV: Record<Role, NavItem[]> = {
  ASHA: [
    { to: '/due', labelEn: T.dueList.en, labelTe: T.dueList.te, icon: <ClipboardList className="size-5" aria-hidden /> },
    { to: '/patients', labelEn: T.patients.en, labelTe: T.patients.te, icon: <Users className="size-5" aria-hidden /> },
    { to: '/referrals', labelEn: T.referrals.en, labelTe: T.referrals.te, icon: <Route className="size-5" aria-hidden /> },
  ],
  PHC_STAFF: [
    { to: '/referrals', labelEn: T.referrals.en, labelTe: T.referrals.te, icon: <Route className="size-5" aria-hidden /> },
    { to: '/patients', labelEn: T.patients.en, labelTe: T.patients.te, icon: <Users className="size-5" aria-hidden /> },
  ],
  SUPERVISOR: [
    { to: '/dashboard', labelEn: T.dashboard.en, labelTe: T.dashboard.te, icon: <LayoutDashboard className="size-5" aria-hidden /> },
    { to: '/referrals', labelEn: T.referrals.en, labelTe: T.referrals.te, icon: <Route className="size-5" aria-hidden /> },
    { to: '/patients', labelEn: T.patients.en, labelTe: T.patients.te, icon: <Users className="size-5" aria-hidden /> },
    { to: '/rules', labelEn: T.rules.en, labelTe: T.rules.te, icon: <SlidersHorizontal className="size-5" aria-hidden /> },
  ],
}

export function AppShell() {
  const user = useCurrentUser()
  const { logout } = useAuth()
  const { lang, toggle: toggleLang, t } = useLang()
  const items = NAV[user.role]
  const scope = user.villageName ?? user.facilityName ?? 'Ramapuram Block'

  useEscalationAlerts()

  return (
    <div className="min-h-dvh pb-20 sm:pb-0">
      <header className="sticky top-0 z-20 border-b border-line bg-surface/95 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center gap-4 px-4">
          <Logo />
          <nav className="hidden items-center gap-1 sm:flex" aria-label="Main">
            {items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  clsx(
                    'rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors',
                    isActive ? 'bg-brand-soft text-brand' : 'text-ink-soft hover:bg-ink/5',
                  )
                }
              >
                {t(item.labelEn, item.labelTe)}
              </NavLink>
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-3">
            <div className="hidden text-right leading-tight md:block">
              <p className="text-sm font-semibold text-ink">{user.fullName}</p>
              <p className="text-xs text-muted">
                {ROLE_LABEL[user.role]} · {scope}
              </p>
            </div>
            <button
              type="button"
              onClick={toggleLang}
              className="grid size-9 place-items-center rounded-lg text-ink-soft hover:bg-ink/5"
              aria-label={t(T.switchToTelugu.en, T.switchToTelugu.te)}
              title={lang === 'en' ? 'తెలుగు' : 'English'}
            >
              <Languages className="size-5" aria-hidden />
            </button>
            <button
              type="button"
              onClick={logout}
              className="grid size-9 place-items-center rounded-lg text-ink-soft hover:bg-ink/5"
              aria-label={t(T.signOut.en, T.signOut.te)}
              title={t(T.signOut.en, T.signOut.te)}
            >
              <LogOut className="size-5" aria-hidden />
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-5 sm:py-8">
        <Outlet />
      </main>

      {/* Bottom tab bar on phones: the ASHA's primary device. */}
      <nav
        className="fixed inset-x-0 bottom-0 z-20 grid border-t border-line bg-surface sm:hidden"
        style={{ gridTemplateColumns: `repeat(${items.length}, minmax(0, 1fr))` }}
        aria-label="Main"
      >
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              clsx(
                'flex flex-col items-center gap-0.5 py-2 text-[11px] font-semibold',
                isActive ? 'text-brand' : 'text-muted',
              )
            }
          >
            {item.icon}
            {t(item.labelEn, item.labelTe)}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}

export function Logo() {
  return (
    <div className="flex items-center gap-2">
      <svg viewBox="0 0 32 32" className="size-7" aria-hidden>
        <rect width="32" height="32" rx="8" fill="var(--color-brand)" />
        <path d="M9 21c3-8 11-8 14-10" fill="none" stroke="#f4efe6" strokeWidth="3" strokeLinecap="round" />
        <circle cx="9" cy="21" r="3" fill="var(--color-accent)" />
        <circle cx="23" cy="11" r="3" fill="#f4efe6" />
      </svg>
      <span className="text-base font-extrabold tracking-tight text-ink">CareRoute</span>
    </div>
  )
}
