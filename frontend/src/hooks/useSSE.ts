import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useToast } from '../contexts/ToastContext'
import { tokenStore } from '../lib/http'

export function useEscalationAlerts() {
  const toast = useToast()
  const qc = useQueryClient()

  useEffect(() => {
    const token = getToken()
    if (!token) return

    const es = new EventSource(`/api/events?token=${encodeURIComponent(token)}`)

    es.addEventListener('escalation', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data)
        toast.push(
          'Referral escalated',
          `${data.patientName} · ${data.villageName}`,
          'critical',
        )
        qc.invalidateQueries({ queryKey: ['dashboard'] })
        qc.invalidateQueries({ queryKey: ['follow-ups', 'due'] })
        qc.invalidateQueries({ queryKey: ['referrals'] })
      } catch {
        // malformed event — ignore
      }
    })

    es.onerror = () => {
      // EventSource auto-reconnects; silence the error
    }

    return () => es.close()
  }, [toast, qc])
}
