import { fitWithin, squareCrop } from './imageTools'
import { initialsOf } from './names'

describe('fitWithin', () => {
  it('reduz mantendo a proporção até caber no lado máximo', () => {
    expect(fitWithin(4000, 3000, 1600)).toEqual({ width: 1600, height: 1200 })
    expect(fitWithin(3000, 4000, 1600)).toEqual({ width: 1200, height: 1600 })
  })

  it('nunca amplia uma imagem que já é pequena', () => {
    expect(fitWithin(800, 600, 1600)).toEqual({ width: 800, height: 600 })
  })
})

describe('squareCrop', () => {
  it('pega o maior quadrado centralizado de uma imagem horizontal', () => {
    expect(squareCrop(1000, 600)).toEqual({ sx: 200, sy: 0, side: 600 })
  })

  it('pega o maior quadrado centralizado de uma imagem vertical', () => {
    expect(squareCrop(600, 1000)).toEqual({ sx: 0, sy: 200, side: 600 })
  })

  it('numa imagem já quadrada não corta nada', () => {
    expect(squareCrop(512, 512)).toEqual({ sx: 0, sy: 0, side: 512 })
  })
})

describe('initialsOf', () => {
  it('usa as iniciais das duas primeiras palavras, em maiúsculas', () => {
    expect(initialsOf('maria souza')).toBe('MS')
    expect(initialsOf('  Ana  Clara   de Souza ')).toBe('AC')
  })

  it('com uma palavra só usa uma letra, e sem nome mostra um ponto de interrogação', () => {
    expect(initialsOf('Zeca')).toBe('Z')
    expect(initialsOf('   ')).toBe('?')
  })
})
