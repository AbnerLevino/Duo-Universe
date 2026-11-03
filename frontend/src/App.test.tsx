import { render, screen } from '@testing-library/react'
import App from './App'

describe('App', () => {
  it('mostra o nome e o lema do produto', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: 'Duo Universe' })).toBeInTheDocument()
    expect(screen.getByText('Two lives. One universe.')).toBeInTheDocument()
  })
})
