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

  // BP: "BP 140 over 90", "blood pressure 140 90", "BP 140/90"
  const bpMatch = lower.match(/(?:bp|blood pressure|బిపి)[^\d]*(\d{2,3})\s*(?:over|\/|[\s,]+)(\d{2,3})/)
  if (bpMatch) {
    result.systolic = parseInt(bpMatch[1], 10)
    result.diastolic = parseInt(bpMatch[2], 10)
  }

  // Temperature: "temperature 37.2", "fever 38", "temp 37"
  const tempMatch = lower.match(/(?:temperature|temp|fever|జ్వరం)[^\d]*(\d{2}(?:\.\d)?)/)
  if (tempMatch) result.temperatureC = parseFloat(tempMatch[1])

  // Weight: "weight 52", "weight 52.5 kg"
  const wMatch = lower.match(/(?:weight|బరువు)[^\d]*(\d{2,3}(?:\.\d)?)/)
  if (wMatch) result.weightKg = parseFloat(wMatch[1])

  // Pregnancy weeks: "weeks 32", "32 weeks pregnant", "pregnancy 32"
  const wkMatch = lower.match(/(?:weeks?|pregnant|pregnancy|గర్భం)[^\d]*(\d{1,2})|(\d{1,2})\s*weeks?/)
  if (wkMatch) result.pregnancyWeeks = parseInt(wkMatch[1] ?? wkMatch[2], 10)

  // Dose: "dose taken", "took dose", "missed dose", "no dose"
  if (/dose taken|took.{0,10}dose|మందు తీసుకున్నారు/.test(lower)) result.doseTaken = true
  if (/missed dose|no dose|dose missed|మందు తీసుకోలేదు/.test(lower)) result.doseTaken = false

  // Symptoms (keyword matching)
  const symptomMap: Record<string, string> = {
    'headache': 'SEVERE_HEADACHE',
    'head ache': 'SEVERE_HEADACHE',
    'తలనొప్పి': 'SEVERE_HEADACHE',
    'blurred vision': 'BLURRED_VISION',
    'మసక': 'BLURRED_VISION',
    'bleeding': 'BLEEDING',
    'రక్తస్రావం': 'BLEEDING',
    'swelling': 'SWELLING',
    'వాపు': 'SWELLING',
    'fetal movement': 'REDUCED_FETAL_MOVEMENT',
    'fever': 'FEVER',
    'జ్వరం': 'FEVER',
    'cough': 'COUGH_2_WEEKS',
    'దగ్గు': 'COUGH_2_WEEKS',
    'hemoptysis': 'HEMOPTYSIS',
    'blood cough': 'HEMOPTYSIS',
    'breathless': 'BREATHLESSNESS',
    'ఆయాసం': 'BREATHLESSNESS',
    'weight loss': 'WEIGHT_LOSS',
    'diarrhoea': 'DIARRHOEA',
    'diarrhea': 'DIARRHOEA',
    'విరేచనాలు': 'DIARRHOEA',
    'rash': 'RASH',
    'దద్దుర్లు': 'RASH',
  }
  const found: string[] = []
  for (const [keyword, code] of Object.entries(symptomMap)) {
    if (lower.includes(keyword) && !found.includes(code)) found.push(code)
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
    rec.lang = 'te-IN'
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
