// All times are shown in India Standard Time regardless of the viewer's machine.
const TZ = 'Asia/Kolkata'

const dateTime = new Intl.DateTimeFormat('en-IN', {
  timeZone: TZ,
  day: 'numeric',
  month: 'short',
  hour: 'numeric',
  minute: '2-digit',
})

const dateOnly = new Intl.DateTimeFormat('en-IN', {
  timeZone: TZ,
  day: 'numeric',
  month: 'short',
  year: 'numeric',
})

export function formatDateTime(iso?: string): string {
  return iso ? dateTime.format(new Date(iso)) : '—'
}

export function formatDate(iso?: string): string {
  return iso ? dateOnly.format(new Date(iso)) : '—'
}

/** "in 3 h", "2 d ago" — compact relative time for due dates. */
export function formatRelative(iso: string, now: number = Date.now()): string {
  const diffMs = new Date(iso).getTime() - now
  const abs = Math.abs(diffMs)
  const minutes = Math.round(abs / 60_000)
  const hours = Math.round(abs / 3_600_000)
  const days = Math.round(abs / 86_400_000)
  const amount = minutes < 60 ? `${Math.max(minutes, 1)} min` : hours < 48 ? `${hours} h` : `${days} d`
  return diffMs >= 0 ? `in ${amount}` : `${amount} ago`
}

export function isToday(iso: string): boolean {
  const key = (d: Date) => dateOnly.format(d)
  return key(new Date(iso)) === key(new Date())
}

export function isRecent(iso: string, minutes = 15): boolean {
  return Date.now() - new Date(iso).getTime() < minutes * 60_000
}

export function formatPercent(rate?: number | null): string {
  return rate == null ? '—' : `${Math.round(rate * 100)}%`
}
