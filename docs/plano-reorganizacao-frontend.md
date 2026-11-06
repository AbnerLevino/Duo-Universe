# Plano: reorganização do frontend por feature

> Status: **PLANEJADO, aguardando decisão e execução.** Nenhum arquivo foi movido ainda.
> Origem: vistoria pedida pelo fundador em 2026-11-06, logo depois do Épico 3 (moldura com painel lateral).
> Ele pediu para guardar o processo para fazer mais tarde. Mesmo espírito da reorganização do backend
> (`docs/plano-reorganizacao-pacotes.md`, executada em 2026-11-06): só organização, **nenhuma mudança de
> comportamento**, uma etapa por vez, com testes verdes e um commit por etapa.

## O que a vistoria encontrou (fatos de 2026-11-06)

`frontend/src/` tem **45 arquivos soltos** (~4.600 linhas): 13 telas, 11 componentes, 8 utilitários e 11 testes
lado a lado, sem separar o que é do vínculo, do perfil, da busca ou das fotos.

| Problema | Evidência |
| --- | --- |
| Tudo numa pasta só | 45 arquivos em `src/`; impossível ver de relance o que pertence a cada assunto |
| `api.ts` mistura quatro assuntos | 269 linhas, 62 exportações: vínculo, perfil, social e mídia (no backend são 4 módulos) |
| `session.ts` faz três coisas | token e `authedFetch`; o usuário atual (`User`, `getCurrentUser`, logout); e o cache de fotos |
| Código repetido | o padrão "carregar + flag `active` + tratar erro" em 12 telas; o bloco `role="alert"` 21 vezes |
| Nome que mente | a classe `login-error` é usada para qualquer erro, em todas as telas |
| `index.css` monolítico | 615 linhas e 42 classes, de todas as telas, num arquivo só |
| `App.tsx` conhece tudo | 16 imports de telas |
| Código morto | `getCouple` (em `api.ts`) não é usado em lugar nenhum |

## Estrutura proposta

Espelha os módulos do backend (`users`, `social`, `media`, `relationships`), para o fundador ter um mapa mental só.

```
frontend/src/
  app/                 App, AppLayout, Dashboard, rotas, main.tsx
  shared/
    api/               client.ts (call, ApiError, errorMessage, withJsonBody)
    session/           token e authedFetch, usuário atual e logout
    ui/                Avatar, AuthedImage, BottomSheet, ErrorMessage
    hooks/             useResource (carregar + erro + recarregar)
    lib/               imageTools, names
    styles/            tokens.css, base.css, layout.css, sheet.css, ...
    test/              testHelpers, test-setup
  features/
    auth/              LoginScreen, HandleSetupScreen
    profile/           HomeScreen, EditProfileForm, SideProfile, PublicProfileScreen, api, labels
    social/            SearchScreen, FollowListScreen, FollowButton, ReportForm, api
    media/             PhotoGrid, NewMomentScreen, cache de fotos, api
    relationships/     InviteScreen, AcceptInviteScreen, ConfirmScreen, StoryScreen,
                       DateConsensusScreen, CoupleHomeScreen, api
```

Os testes ficam ao lado do que testam (como hoje); o que é só de teste vai para `shared/test/`.

**Regra de fronteira** (o equivalente do `ArchitectureTest` do backend): uma feature só importa de `shared/`, nunca de
outra feature. A pasta `app/` é a exceção documentada, porque monta tudo. A regra pode ser imposta pelo ESLint que o
projeto já usa (`no-restricted-imports`), sem dependência nova.

> Ponto a resolver na execução: `PublicProfileScreen` (profile) usa `FollowButton` (social). Ou o botão sobe para
> `shared/ui`, ou a tela de perfil de outra pessoa passa a morar em `social`. Decidir quando chegar nessa etapa.

## Ordem de execução

Uma etapa por vez. Cada uma termina com `npm run lint`, `npm test`, `npm run build` e `prettier --check` verdes, o
changelog (regra 6) e um commit. Para os `git mv` e a troca de `import`s dá para reaproveitar a ideia do script usado
no backend (mapa "caminho antigo → novo", atualizando os imports sozinho).

1. **Base compartilhada.** Separar o `api.ts` em `shared/api/client.ts` mais uma API por assunto, e quebrar o
   `session.ts` em token/`authedFetch`, usuário atual e cache de fotos. Mover `imageTools`, `names` e os helpers de teste.
2. **Pastas por feature.** Mover as telas e componentes para `features/*`, atualizar os imports e configurar o alias
   `@/` (Vite e `tsconfig`), para os caminhos não virarem `../../..`.
3. **CSS em arquivos por assunto.** Dividir o `index.css`: tokens, base, moldura, modal e um arquivo por feature,
   importados por quem usa. Nenhuma regra muda de lugar sem necessidade.
4. **Tirar as repetições.** Hook `useResource` nas 12 telas, componente `ErrorMessage` nos 21 blocos de erro,
   renomear `login-error` (ex.: `error-text`) e apagar o `getCouple`.
5. **Trava e documentação.** Regra de fronteira no ESLint; atualizar o `docs/MAPA.md` (árvore nova) e o
   `docs/GLOSSARIO-TECNICO.md`; marcar este plano como executado.

## Decisões a tomar antes de começar (com a recomendação)

1. **Pastas por feature** (recomendado, espelha o backend) ou **por tipo** (`components/`, `screens/`, `hooks/`)? Por
   tipo é mais familiar, mas cada mudança num assunto espalha arquivos por todas as pastas.
2. **Alias `@/`** nos imports: **sim** (só configuração, sem dependência).
3. **CSS:** **dividir em arquivos** (risco baixo) ou migrar para *CSS Modules* (escopo por componente, mais trabalho
   agora; o Vite já suporta, sem dependência)? Recomendado: dividir agora, avaliar Modules depois.
4. **Etapa 4** (hook e componente de erro) junto da reorganização ou depois? Recomendado: depois das etapas 1 a 3,
   ainda no mesmo mutirão, porque as telas já estarão nas pastas novas.

## Riscos e como cuidamos

- **Quebrar imports sem perceber:** o `tsc --noEmit` e o build pegam na hora; os 64 testes cobrem o comportamento.
- **Ciclos entre features:** a regra de fronteira do ESLint impede; o `ArchitectureTest` do backend é a referência.
- **Mudar comportamento sem querer:** proibido nesta reorganização. Qualquer melhoria de verdade (nova tela, texto,
  visual) vira outra tarefa. Só entram aqui as correções de organização listadas acima.
- **Ordem dos estilos (CSS):** ao dividir o `index.css`, a ordem de importação preserva a cascata atual. Conferir o
  visual com o Edge sem tela (1100px e 390px) antes de fechar a etapa 3, como foi feito na moldura do Épico 3.
