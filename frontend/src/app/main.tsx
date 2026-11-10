import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// O CSS global precisa carregar antes de qualquer tela: senão, o estilo de uma tela específica (um
// .module.css) pode perder "empate" de cascata para uma classe global como `.screen` (ex.: `.login`
// perdendo `max-width: none` para o `max-width: 32rem` de `.screen`).
import '../index.css'
import App from './App'
import AuroraBackground from './AuroraBackground'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuroraBackground />
    <App />
  </StrictMode>,
)
