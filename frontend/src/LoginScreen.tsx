import { googleLoginUrl } from './session'

const ERRORS: Record<string, string> = {
  'email-nao-verificado':
    'Seu e-mail do Google ainda não foi confirmado. Confirme-o e tente de novo.',
  'email-ja-vinculado': 'Este e-mail já está ligado a outra conta do Duo.',
}

export default function LoginScreen({ error }: { error: string | null }) {
  const message = error ? (ERRORS[error] ?? 'Não foi possível entrar. Tente de novo.') : null
  return (
    <main className="screen login">
      <h1>Duo Universe</h1>
      <p className="tagline">Two lives. One universe.</p>
      {message && (
        <p role="alert" className="login-error">
          {message}
        </p>
      )}
      <a className="button primary" href={googleLoginUrl}>
        Entrar com Google
      </a>
    </main>
  )
}
