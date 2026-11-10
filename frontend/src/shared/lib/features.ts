/**
 * RF-A11: a camada social (busca, Duollowers/Duollowing, perfil de outras pessoas) está congelada porque o Duo
 * tem foco no casal. O código das telas continua aqui, só não aparece. Para religar no desenvolvimento, defina
 * `VITE_FEATURE_SOCIAL=true` (e `DUO_FEATURE_SOCIAL=true` no backend). É uma função, e não uma constante, para os
 * testes poderem ligar e desligar com `vi.stubEnv`.
 */
export function socialEnabled(): boolean {
  return import.meta.env.VITE_FEATURE_SOCIAL === 'true'
}
