import { useEffect, useRef, useState, type CSSProperties } from 'react'
import { googleLoginUrl } from '@/shared/session'
import styles from './LoginScreen.module.css'

const ERRORS: Record<string, string> = {
  'email-nao-verificado':
    'Seu e-mail do Google ainda não foi confirmado. Confirme-o e tente de novo.',
  'email-ja-vinculado': 'Este e-mail já está ligado a outra conta do Duo.',
}

const TAGLINE = 'Onde universos se encontram.'
const PITCH = 'Conheça pessoas, viva sua história e guarde o que importa, do seu jeito.'

const STAR_COUNT = 14

// Estrelas do cartão: posição, tamanho e ritmo sorteados uma vez, quando a tela abre (não a cada render).
// Boa parte nasce nas margens, para não brilhar em cima do texto.
function makeStars() {
  return Array.from({ length: STAR_COUNT }, (_, i) => {
    const inMargin = i % 5 !== 0
    const x = inMargin
      ? Math.random() < 0.5
        ? 3 + Math.random() * 8
        : 89 + Math.random() * 8
      : 10 + Math.random() * 80
    return {
      '--x': `${x}%`,
      '--y': `${3 + Math.random() * 94}%`,
      '--size': `${2 + Math.random() * 4}px`,
      '--dur': `${2 + Math.random() * 3}s`,
      '--delay': `${(Math.random() * 4).toFixed(2)}s`,
      '--peak': inMargin ? 0.9 : 0.45,
    } as CSSProperties
  })
}

// Efeitos sobre a ilustração. As posições são em % da imagem (o palco tem a mesma proporção dela), então
// acompanham a arte no desktop e no arrasto do celular. A arte é um PNG só: as estrelas desenhadas ficam
// paradas e o que pulsa é um halo de luz por cima de cada uma, no mesmo ritmo das estrelas do cartão.
const HALOS = [
  { x: 24.9, y: 10.5, dur: 3.4, delay: 0 },
  { x: 45.3, y: 8.2, dur: 4.2, delay: 1.1 },
  { x: 63.2, y: 12.9, dur: 2.8, delay: 2.3 },
  { x: 77.8, y: 17.6, dur: 3.9, delay: 0.6 },
  { x: 93.0, y: 14.0, dur: 4.6, delay: 1.8 },
  { x: 21.0, y: 38.8, dur: 3.1, delay: 2.9 },
  { x: 42.0, y: 49.2, dur: 4.0, delay: 0.3 },
  { x: 80.4, y: 49.4, dur: 3.6, delay: 1.5 },
].map(({ x, y, dur, delay }) => ({
  '--x': `${x}%`,
  '--y': `${y}%`,
  '--dur': `${dur}s`,
  '--delay': `${delay}s`,
  '--peak': 0.85,
})) as CSSProperties[]

// Vaga-lumes subindo devagar da grama, perto do casal (~36–52% da largura, ~84% da altura).
// `delay` negativo: já nascem em pleno voo quando a tela abre.
const FIREFLIES = [
  { x: 36, y: 85, dur: 9, delay: -1, rise: 9, sway: 14 },
  { x: 39, y: 83, dur: 11, delay: -5, rise: 11, sway: -18 },
  { x: 42, y: 86, dur: 8, delay: -3, rise: 8, sway: 10 },
  { x: 46, y: 84, dur: 12, delay: -8, rise: 12, sway: -12 },
  { x: 49, y: 86, dur: 9.5, delay: -2, rise: 9, sway: 16 },
  { x: 52, y: 84, dur: 10.5, delay: -6, rise: 10, sway: -14 },
  { x: 44, y: 87, dur: 13, delay: -10, rise: 13, sway: 8 },
].map(({ x, y, dur, delay, rise, sway }) => ({
  '--x': `${x}%`,
  '--y': `${y}%`,
  '--dur': `${dur}s`,
  '--delay': `${delay}s`,
  '--rise': `${rise}dvh`,
  '--sway': `${sway}px`,
})) as CSSProperties[]

export default function LoginScreen({ error }: { error: string | null }) {
  const message = error ? (ERRORS[error] ?? 'Não foi possível entrar. Tente de novo.') : null
  const scrollerRef = useRef<HTMLDivElement>(null)
  const startRef = useRef<HTMLSpanElement>(null)
  const [onPromoSide, setOnPromoSide] = useState(false)
  const [stars] = useState(makeStars)

  // Distância (em px) até o ponto de parada do casal; o outro ponto é o fim da imagem.
  function startStop(scroller: HTMLDivElement) {
    const anchor = startRef.current?.offsetLeft ?? 0
    return Math.max(0, anchor - scroller.clientWidth / 2)
  }

  // No celular a imagem é mais larga que a tela: ao abrir, mostra o lado do casal.
  useEffect(() => {
    const scroller = scrollerRef.current
    if (scroller) scroller.scrollLeft = startStop(scroller)
  }, [])

  function handleScroll() {
    const scroller = scrollerRef.current
    if (!scroller) return
    const end = scroller.scrollWidth - scroller.clientWidth
    setOnPromoSide(scroller.scrollLeft > (startStop(scroller) + end) / 2)
  }

  return (
    <main className={`screen ${styles.login}`}>
      {/* Fundo: a arte em tela cheia. No desktop ela cabe inteira atrás do cartão de vidro; no celular
          (tela estreita) ela é mais larga que a tela e se arrasta de lado, revelando a frase promocional
          escrita direto sobre o outro lado da imagem (sem caixa). */}
      <div className={styles.scroller} ref={scrollerRef} onScroll={handleScroll}>
        <div className={styles.track}>
          <div className={styles.stage}>
            <img className={styles.art} src="/images/login-art.png" alt="" aria-hidden="true" />
            <div className={styles.fx} aria-hidden="true">
              {HALOS.map((style, i) => (
                <span key={`h${i}`} className={styles.halo} style={style} />
              ))}
              {FIREFLIES.map((style, i) => (
                <span key={`f${i}`} className={styles.firefly} style={style} />
              ))}
            </div>
          </div>
          <span className={styles.anchorStart} ref={startRef} aria-hidden="true" />
          <span className={styles.anchorEnd} aria-hidden="true" />
          <div className={styles.promo}>
            <p className={styles.promoTagline}>{TAGLINE}</p>
            <p className={styles.promoPitch}>{PITCH}</p>
          </div>
        </div>
      </div>
      <div className={styles.loginPanel}>
        <div className={styles.stars} aria-hidden="true">
          {stars.map((style, i) => (
            <span key={i} className={styles.star} style={style} />
          ))}
        </div>
        <div className={styles.brand}>
          {/* Ícone provisório (opção A: Saturno e a estrela) — nenhuma das marcas é final. */}
          <img
            className={styles.loginIcon}
            src="/images/duo-simbolo.svg"
            alt=""
            aria-hidden="true"
          />
          <div className={styles.brandText}>
            <h1>Duo Universe</h1>
            <p className={styles.tagline}>{TAGLINE}</p>
          </div>
        </div>
        <div className={styles.divider} aria-hidden="true">
          <span className={styles.dot} />
        </div>
        <div className={styles.copy}>
          <p className={styles.loginPitch}>{PITCH}</p>
        </div>
        {message && (
          <p role="alert" className="login-error">
            {message}
          </p>
        )}
        <a className="button primary" href={googleLoginUrl}>
          <svg className={styles.googleIcon} viewBox="0 0 48 48" aria-hidden="true">
            <path
              fill="#EA4335"
              d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
            />
            <path
              fill="#4285F4"
              d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
            />
            <path
              fill="#FBBC05"
              d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
            />
            <path
              fill="#34A853"
              d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
            />
          </svg>
          Entrar com Google
        </a>
        {/* Aviso legal (DEC-029). As páginas de Termos e Privacidade ainda não existem: por ora é só texto,
            sem link, para não levar a lugar nenhum; vira link quando os textos revisados estiverem prontos. */}
        <p className={styles.terms}>
          Ao entrar, você concorda com os <span className={styles.termsLink}>Termos de uso</span> e
          a <span className={styles.termsLink}>Política de privacidade</span>.
        </p>
        <div className={styles.dots} aria-hidden="true">
          <span className={onPromoSide ? undefined : styles.dotActive} />
          <span className={onPromoSide ? styles.dotActive : undefined} />
        </div>
      </div>
    </main>
  )
}
