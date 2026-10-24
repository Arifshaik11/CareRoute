import clsx from 'clsx'
import { Mic, Loader2 } from 'lucide-react'
import type { UseVoiceInputReturn } from '../hooks/useVoiceInput'

interface Props {
  voice: UseVoiceInputReturn
  className?: string
}

export function VoiceButton({ voice, className }: Props) {
  if (!voice.isSupported) return null

  return (
    <div className={clsx('flex flex-col items-end gap-2', className)}>
      <button
        type="button"
        onClick={voice.isListening ? voice.stop : voice.start}
        className={clsx(
          'flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-semibold transition-all',
          voice.isListening
            ? 'animate-pulse bg-critical text-white shadow-lg shadow-critical/30'
            : 'bg-brand text-white hover:bg-brand-strong',
        )}
        aria-label={voice.isListening ? 'Stop voice input' : 'Start voice input (Telugu / English)'}
      >
        {voice.isListening ? (
          <>
            <Loader2 className="size-4 animate-spin" aria-hidden />
            <span>Listening…</span>
          </>
        ) : (
          <>
            <Mic className="size-4" aria-hidden />
            <span>Voice entry</span>
          </>
        )}
      </button>
      {voice.transcript && (
        <p className="max-w-xs rounded-lg bg-canvas px-3 py-1.5 text-xs text-ink-soft" aria-live="polite">
          <span className="font-semibold text-brand">Heard:</span> {voice.transcript}
        </p>
      )}
      {!voice.isListening && !voice.transcript && (
        <p className="text-xs text-muted">Speak in Telugu or English</p>
      )}
    </div>
  )
}
