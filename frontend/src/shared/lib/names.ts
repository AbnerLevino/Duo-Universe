/** As iniciais do nome ("Maria Souza" → "MS"), o que aparece enquanto a pessoa não tem foto. */
export function initialsOf(name: string) {
  const letters = name
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0].toUpperCase())
  return letters.join('') || '?'
}
