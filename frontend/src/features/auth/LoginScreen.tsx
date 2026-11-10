import { googleLoginUrl } from '@/shared/session'
import styles from './LoginScreen.module.css'

const ERRORS: Record<string, string> = {
  'email-nao-verificado':
    'Seu e-mail do Google ainda não foi confirmado. Confirme-o e tente de novo.',
  'email-ja-vinculado': 'Este e-mail já está ligado a outra conta do Duo.',
}

export default function LoginScreen({ error }: { error: string | null }) {
  const message = error ? (ERRORS[error] ?? 'Não foi possível entrar. Tente de novo.') : null
  return (
    <main className={`screen ${styles.login}`}>
      {/* Rascunho de layout (ainda em discussão): no desktop, a arte entra pela esquerda e o quadrado do
          login (o cartão com o botão de entrar) fica à direita, do lado dela. Some no celular — não há
          espaço pra duas colunas numa tela estreita, só o cartão continua, centrado sobre a aurora. */}
      <img className={styles.loginArt} src="/images/login-art.png" alt="" aria-hidden="true" />
      <div className={styles.loginPanel}>
        {/* Ícone provisório (opção A: Saturno e a estrela) — nenhuma das marcas é final. */}
        <img className={styles.loginIcon} src="/images/duo-simbolo.svg" alt="" aria-hidden="true" />
        <h1>Duo Universe</h1>
        <p className={styles.tagline}>Onde universos se encontram.</p>
        <p className={styles.loginPitch}>
          Conheça pessoas, viva sua história e guarde o que importa, do seu jeito.
        </p>
        {message && (
          <p role="alert" className="login-error">
            {message}
          </p>
        )}
        <a className="button primary" href={googleLoginUrl}>
          Entrar com Google
        </a>
      </div>
    </main>
  )
}
