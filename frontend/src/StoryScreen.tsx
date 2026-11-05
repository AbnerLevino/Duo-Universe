import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError, partnerStory, saveStoryStep, STORY_STEPS, type Story, type StoryStep } from './api'

const LABELS: Record<StoryStep, string> = {
  HOW_WE_MET: 'Como vocês se conheceram?',
  TIME_BEFORE_DATING: 'Como foi o tempo conhecendo a pessoa, antes de namorarem?',
  WHAT_I_THOUGHT_THEN: 'O que você pensava dela naquela época?',
  WHAT_I_THINK_NOW: 'O que você pensa agora?',
}

type StoryField = 'howWeMet' | 'timeBeforeDating' | 'whatIThoughtThen' | 'whatIThinkNow'

function fieldOf(step: StoryStep): StoryField {
  switch (step) {
    case 'HOW_WE_MET':
      return 'howWeMet'
    case 'TIME_BEFORE_DATING':
      return 'timeBeforeDating'
    case 'WHAT_I_THOUGHT_THEN':
      return 'whatIThoughtThen'
    case 'WHAT_I_THINK_NOW':
      return 'whatIThinkNow'
  }
}

/** RN-25/RN-26: cada passo salva sozinho (sem mínimo de caracteres); a história da outra pessoa só aparece
 * quando as duas estiverem completas. */
export default function StoryScreen() {
  const { pairingId } = useParams<{ pairingId: string }>()
  const navigate = useNavigate()
  const id = Number(pairingId)
  const [texts, setTexts] = useState<Record<StoryStep, string>>({
    HOW_WE_MET: '',
    TIME_BEFORE_DATING: '',
    WHAT_I_THOUGHT_THEN: '',
    WHAT_I_THINK_NOW: '',
  })
  const [savedStep, setSavedStep] = useState<StoryStep | null>(null)
  const [partner, setPartner] = useState<Story | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    partnerStory(id).then((story) => {
      if (active) setPartner(story)
    })
    return () => {
      active = false
    }
  }, [id])

  async function save(step: StoryStep) {
    const text = texts[step].trim()
    if (!text) return
    setError(null)
    try {
      await saveStoryStep(id, step, text)
      setSavedStep(step)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível salvar.')
    }
  }

  return (
    <main className="screen">
      <h1>A história de vocês</h1>
      <p className="muted">Escreva com calma — cada passo salva sozinho quando você sai do campo.</p>
      {STORY_STEPS.map((step) => (
        <div key={step} className="form">
          <label htmlFor={step}>{LABELS[step]}</label>
          <textarea
            id={step}
            rows={3}
            value={texts[step]}
            onChange={(e) => setTexts((prev) => ({ ...prev, [step]: e.target.value }))}
            onBlur={() => save(step)}
          />
          {savedStep === step && <p className="muted">Salvo.</p>}
        </div>
      ))}
      {error && (
        <p role="alert" className="login-error">
          {error}
        </p>
      )}
      <section>
        <h2>A história da sua pessoa</h2>
        {partner ? (
          STORY_STEPS.map((step) => (
            <p key={step}>
              <strong>{LABELS[step]}</strong>
              <br />
              {partner[fieldOf(step)]}
            </p>
          ))
        ) : (
          <p className="muted">Aparece aqui assim que as duas histórias estiverem completas.</p>
        )}
      </section>
      <button type="button" className="button" onClick={() => navigate('/casal')}>
        Voltar
      </button>
    </main>
  )
}
