import { createContext, useContext, useState, type ReactNode } from 'react'

type Lang = 'en' | 'te'

interface LangCtx {
  lang: Lang
  toggle: () => void
  t: (en: string, te: string) => string
}

const LangContext = createContext<LangCtx>({
  lang: 'en',
  toggle: () => {},
  t: (en) => en,
})

export function LangProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Lang>(() => {
    try { return (localStorage.getItem('cr-lang') as Lang) ?? 'en' } catch { return 'en' }
  })

  function toggle() {
    const next: Lang = lang === 'en' ? 'te' : 'en'
    setLang(next)
    try { localStorage.setItem('cr-lang', next) } catch {}
  }

  function t(en: string, te: string) {
    return lang === 'te' ? te : en
  }

  return <LangContext.Provider value={{ lang, toggle, t }}>{children}</LangContext.Provider>
}

export function useLang() {
  return useContext(LangContext)
}
