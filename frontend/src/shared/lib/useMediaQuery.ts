import { useEffect, useState } from 'react'

/**
 * Diz se uma media query do CSS vale agora (ex.: `(min-width: 768px)`) e acompanha quando ela muda. Onde o
 * navegador não tem `matchMedia` (os testes em jsdom), devolve `fallback`.
 */
export function useMediaQuery(query: string, fallback = true): boolean {
  const [matches, setMatches] = useState(() =>
    typeof window.matchMedia === 'function' ? window.matchMedia(query).matches : fallback,
  )

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return
    const list = window.matchMedia(query)
    const onChange = () => setMatches(list.matches)
    onChange()
    list.addEventListener('change', onChange)
    return () => list.removeEventListener('change', onChange)
  }, [query])

  return matches
}
