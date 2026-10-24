import { useCallback, useRef, useState } from 'react'

export interface VoiceFields {
  systolic?: number
  diastolic?: number
  weightKg?: number
  temperatureC?: number
  pregnancyWeeks?: number
  doseTaken?: boolean
  symptoms?: string[]
  rawTranscript?: string
}

type ParsedResult = VoiceFields & { rawTranscript: string }

/** Parse a spoken sentence (English or Telugu-influenced) into visit field values. */
function parseTranscript(text: string): ParsedResult {
  const lower = text.toLowerCase()
  const result: ParsedResult = { rawTranscript: text }

  // ── Blood Pressure ────────────────────────────────────────────────────────
  // English:  "BP 140 over 90", "blood pressure 140/90", "BP 140 90"
  // Telugu:   "బిపి", "బ్లడ్ ప్రెజర్", "బ్లడ్ ప్రెషర్"
  const bpMatch = lower.match(
    /(?:bp|blood pressure|బిపి|బ్లడ్\s*(?:ప్రెజర్|ప్రెషర్|pressure))[^\d]*(\d{2,3})\s*(?:over|\/|[\s,]+)(\d{2,3})/
  )
  if (bpMatch) {
    result.systolic = parseInt(bpMatch[1], 10)
    result.diastolic = parseInt(bpMatch[2], 10)
  }

  // ── Temperature ───────────────────────────────────────────────────────────
  // English:   "temperature 37.2", "temp 38", "fever 38"
  // Telugu:    "టెంపరేచర్", "టెంప్", "జ్వరం"
  // Accepts decimals: "36.8", "36 point 8", "36 పాయింట్ 8"
  const tempMatch = lower.match(
    /(?:temperature|temp(?:erature)?|fever|జ్వరం|టెంపరేచర్|టెంప్)[^\d]*(\d{2}(?:[.\s](?:point|పాయింట్)\s*)?\d?)/
  )
  if (tempMatch) {
    // Normalise "36 8" or "36 point 8" → "36.8"
    const raw = tempMatch[1].replace(/\s*(?:point|పాయింట్)\s*/g, '.').replace(/\s+/, '.')
    result.temperatureC = parseFloat(raw)
  }

  // ── Weight ────────────────────────────────────────────────────────────────
  // "weight 52", "weight 52.5 kg", "బరువు 52", "వెయిట్ 52"
  const wMatch = lower.match(/(?:weight|బరువు|వెయిట్)[^\d]*(\d{2,3}(?:\.\d)?)/)
  if (wMatch) result.weightKg = parseFloat(wMatch[1])

  // ── Pregnancy Weeks ───────────────────────────────────────────────────────
  // "32 weeks", "weeks 32", "pregnancy 32", "గర్భం 32", "32 వారాలు"
  const wkMatch = lower.match(
    /(?:weeks?|pregnant|pregnancy|గర్భం|వారాలు|వారం)[^\d]*(\d{1,2})|(\d{1,2})\s*(?:weeks?|వారాలు)/
  )
  if (wkMatch) result.pregnancyWeeks = parseInt(wkMatch[1] ?? wkMatch[2], 10)

  // ── TB Dose ───────────────────────────────────────────────────────────────
  // Taken: "dose taken", "took dose", "మందు తీసుకున్నారు", "మందు తీసుకున్నా"
  // Missed: "missed dose", "no dose", "మందు తీసుకోలేదు", "మందు వేయలేదు"
  if (/dose taken|took.{0,10}dose|yes.{0,5}dose|మందు తీసుకున్న/.test(lower)) result.doseTaken = true
  if (/missed dose|no dose|dose missed|not taken|మందు తీసుకోలేదు|మందు వేయలేదు/.test(lower)) result.doseTaken = false

  // ── Symptoms ──────────────────────────────────────────────────────────────
  // Each entry: [keyword-regex, symptom-code]
  const symptomPatterns: [RegExp, string][] = [
    [/headache|head ache|తలనొప్పి|హెడేక్/, 'SEVERE_HEADACHE'],
    [/blurred.{0,5}vision|vision.{0,5}blurr|మసక|బ్లర్/, 'BLURRED_VISION'],
    [/bleeding|రక్తస్రావం|బ్లీడింగ్/, 'BLEEDING'],
    [/swelling|వాపు|సూజన్/, 'SWELLING'],
    [/fetal.{0,10}movement|reduced.{0,10}movement|పిండం కదలడం/, 'REDUCED_FETAL_MOVEMENT'],
    [/\bfever\b|జ్వరం|ఫీవర్/, 'FEVER'],
    [/\bcough\b|దగ్గు|కఫ్/, 'COUGH_2_WEEKS'],
    [/hemoptysis|blood.{0,5}cough|రక్తం దగ్గు/, 'HEMOPTYSIS'],
    [/breathless|shortness.{0,10}breath|ఆయాసం|శ్వాస/, 'BREATHLESSNESS'],
    [/weight.{0,5}loss|బరువు తగ్గడం/, 'WEIGHT_LOSS'],
    [/diarrhoe?a|విరేచనాలు|డయేరియా/, 'DIARRHOEA'],
    [/\brush\b|దద్దుర్లు|రాష్/, 'RASH'],
  ]
  const found: string[] = []
  for (const [pattern, code] of symptomPatterns) {
    if (pattern.test(lower) && !found.includes(code)) found.push(code)
  }
  if (found.length > 0) result.symptoms = found

  return result
}

// Extend window for SpeechRecognition
declare global {
  interface Window {
    SpeechRecognition?: new () => SpeechRecognition
    webkitSpeechRecognition?: new () => SpeechRecognition
  }
  interface SpeechRecognition extends EventTarget {
    lang: string
    interimResults: boolean
    continuous: boolean
    onresult: ((e: SpeechRecognitionEvent) => void) | null
    onend: (() => void) | null
    onerror: ((e: Event) => void) | null
    start(): void
    stop(): void
  }
  interface SpeechRecognitionEvent extends Event {
    results: SpeechRecognitionResultList
  }
  interface SpeechRecognitionResultList {
    readonly length: number
    item(index: number): SpeechRecognitionResult
    [index: number]: SpeechRecognitionResult
  }
  interface SpeechRecognitionResult {
    readonly isFinal: boolean
    readonly length: number
    item(index: number): SpeechRecognitionAlternative
    [index: number]: SpeechRecognitionAlternative
  }
  interface SpeechRecognitionAlternative {
    readonly transcript: string
    readonly confidence: number
  }
}

export interface UseVoiceInputReturn {
  isListening: boolean
  isSupported: boolean
  transcript: string
  start: () => void
  stop: () => void
}

export function useVoiceInput(onResult: (fields: VoiceFields) => void): UseVoiceInputReturn {
  const [isListening, setIsListening] = useState(false)
  const [transcript, setTranscript] = useState('')
  const recRef = useRef<SpeechRecognition | null>(null)

  const SR = typeof window !== 'undefined'
    ? (window.SpeechRecognition ?? window.webkitSpeechRecognition)
    : undefined
  const isSupported = !!SR

  const start = useCallback(() => {
    if (!SR) return
    const rec = new SR()
    // en-IN gives digit output and handles Telugu-accented English well.
    // The parser also handles Telugu keywords for bilingual speakers.
    rec.lang = 'en-IN'
    rec.interimResults = false
    rec.continuous = false

    rec.onresult = (e) => {
      const text = Array.from({ length: e.results.length }, (_, i) => e.results[i][0].transcript).join(' ')
      setTranscript(text)
      onResult(parseTranscript(text))
    }
    rec.onend = () => setIsListening(false)
    rec.onerror = () => setIsListening(false)

    rec.start()
    recRef.current = rec
    setIsListening(true)
    setTranscript('')
  }, [SR, onResult])

  const stop = useCallback(() => {
    recRef.current?.stop()
    setIsListening(false)
  }, [])

  return { isListening, isSupported, transcript, start, stop }
}
