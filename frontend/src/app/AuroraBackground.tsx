/**
 * O fundo "aurora" (o mesmo conceito do Astrodny): quatro manchas de cor, enormes e desfocadas, que giram devagar e
 * se misturam. Fica fixo atrás de todas as telas e não recebe cliques. Montado uma vez só, em `main.tsx`; as
 * cores e a intensidade vêm dos tokens `--aurora-*` de `index.css`. Enfeite puro: some para leitores de tela.
 */
export default function AuroraBackground() {
  return (
    <div className="aurora" aria-hidden="true">
      <span className="blob blob-1" />
      <span className="blob blob-2" />
      <span className="blob blob-3" />
      <span className="blob blob-4" />
    </div>
  )
}
