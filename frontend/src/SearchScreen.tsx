import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Avatar from './Avatar'
import BottomNav from './BottomNav'
import { errorMessage, type Relation, searchProfiles, type SearchPage } from './api'
import FollowButton from './FollowButton'
import { TERMS } from './labels'

const MIN_LETTERS = 3
const DEBOUNCE_MS = 300

type Result = { query: string; page: SearchPage; number: number } | { query: string; error: string }

/**
 * Buscar pessoas por nome ou {TERMS.duoName} (decisão do fundador). Só busca com 3 letras ou mais, e espera a
 * pessoa parar de digitar um instante para não fazer uma requisição por tecla.
 */
export default function SearchScreen() {
  const [query, setQuery] = useState('')
  const [result, setResult] = useState<Result | null>(null)
  const term = query.trim()
  const tooShort = term.length < MIN_LETTERS

  useEffect(() => {
    if (term.length < MIN_LETTERS) return
    let active = true
    const timer = setTimeout(() => {
      searchProfiles(term, 0)
        .then((page) => {
          if (active) setResult({ query: term, page, number: 0 })
        })
        .catch((err) => {
          if (active)
            setResult({ query: term, error: errorMessage(err, 'Não foi possível buscar.') })
        })
    }, DEBOUNCE_MS)
    return () => {
      active = false
      clearTimeout(timer)
    }
  }, [term])

  const current = !tooShort && result?.query === term ? result : null

  async function loadMore() {
    if (!current || 'error' in current) return
    const next = current.number + 1
    try {
      const page = await searchProfiles(term, next)
      setResult({
        query: term,
        number: next,
        page: { items: [...current.page.items, ...page.items], hasMore: page.hasMore },
      })
    } catch (err) {
      setResult({ query: term, error: errorMessage(err, 'Não foi possível buscar.') })
    }
  }

  function setRelation(handle: string, relation: Relation) {
    setResult((previous) =>
      previous && 'page' in previous
        ? {
            ...previous,
            page: {
              ...previous.page,
              items: previous.page.items.map((item) =>
                item.profile.handle === handle ? { ...item, relation } : item,
              ),
            },
          }
        : previous,
    )
  }

  return (
    <main className="screen with-nav">
      <h1>Buscar</h1>
      <div className="form">
        <label htmlFor="search">Nome ou {TERMS.duoName}</label>
        <input
          id="search"
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Pelo menos 3 letras"
          autoFocus
        />
      </div>

      {tooShort && <p className="muted">Digite pelo menos {MIN_LETTERS} letras para buscar.</p>}
      {!tooShort && !current && (
        <p className="muted" aria-busy="true">
          Buscando…
        </p>
      )}
      {current && 'error' in current && (
        <p role="alert" className="login-error">
          {current.error}
        </p>
      )}
      {current && 'page' in current && (
        <>
          {current.page.items.length === 0 && <p className="muted">Ninguém encontrado.</p>}
          <ul className="list">
            {current.page.items.map(({ profile, relation }) => (
              <li key={profile.userId} className="list-item">
                <Link to={`/u/${profile.handle}`} className="list-main">
                  <Avatar name={profile.displayName} mediaId={profile.avatarMediaId} size="sm" />
                  <span>
                    <strong>{profile.displayName}</strong>
                    <span className="muted"> @{profile.handle}</span>
                    {profile.profession && <span className="muted"> · {profile.profession}</span>}
                  </span>
                </Link>
                <FollowButton
                  handle={profile.handle}
                  relation={relation}
                  onChange={(next) => setRelation(profile.handle, next)}
                />
              </li>
            ))}
          </ul>
          {current.page.hasMore && (
            <button type="button" className="button" onClick={loadMore}>
              Ver mais
            </button>
          )}
        </>
      )}
      <BottomNav />
    </main>
  )
}
