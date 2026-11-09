import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import AuroraBackground from './AuroraBackground'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuroraBackground />
    <App />
  </StrictMode>,
)
