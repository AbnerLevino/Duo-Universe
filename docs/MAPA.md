# Mapa do projeto

> Para que serve: explicar, em português simples, **onde está cada coisa e como as peças se conectam**.
> Descreve só o **presente**. O histórico de cada etapa está no `git log` (os commits citam DEC/RN/RF).
> Termos técnicos: `docs/CONSULTA.md`. Decisões e regras: `CLAUDE.md`.
> **Atualize este arquivo sempre que um arquivo for criado, movido ou removido.**
> Última conferência contra o código: 2026-11-10.

## Visão geral

```
duo/
  CLAUDE.md      contexto e regras para quem (humano ou IA) trabalha no projeto
  docs/          MAPA.md (este) e CONSULTA.md (glossário, requisitos, ideias)
  backend/       a API (Java 21 + Spring Boot + PostgreSQL)
  frontend/      o app (React + Vite, PWA mobile-first)
  estudos/       plano de estudos de Java do fundador (tem CLAUDE.md próprio; não é o produto)
  .github/       CI (verificação automática a cada push)
  dev.sh         sobe backend e frontend juntos (Ctrl+C para os dois)
```

**O que o Duo faz hoje:** entrar com Google; sessão própria; perfil pessoal com foto; vínculo de casal
(convite, confirmação, história, datas, pausa e saída); fotos do Personal. A camada social (busca,
Duollowers, perfil de terceiros) existe no código mas está **congelada** por configuração (RF-A11).

**Caminho de uma requisição:**

```
navegador → Vite (proxy /api; em produção será o Caddy) → SecurityConfig (confere o token)
          → Controller (recebe o HTTP) → Service (regra de negócio)
          → Repository (fala com o banco) → PostgreSQL
```

**Como rodar:** variáveis de ambiente `DUO_APP_DB_PASSWORD`, `DUO_JWT_SECRET` (mínimo 32 caracteres),
`DUO_GOOGLE_CLIENT_ID` e `DUO_GOOGLE_CLIENT_SECRET`; depois `./dev.sh`, ou separado: `cd backend && ./gradlew
bootRun` e `cd frontend && npm run dev`, e abrir `http://localhost:5173`. Em `http://localhost` sem HTTPS, o
navegador pode recusar o cookie `Secure`: use `DUO_COOKIE_SECURE=false` só em desenvolvimento.

## `backend/` — a API

### Convenção dos pacotes (`src/main/java/com/duo/`)

Cada módulo segue a mesma forma:
- **Raiz do módulo = o contrato** que outros módulos podem usar: interfaces de serviço, enums e as pastas
  `controller/`, `dto/` e `exception/`.
- **`internal/` = só o próprio módulo usa** (o `ArchitectureTest` garante): `model/` (entidades),
  `repository/` (acesso ao banco) e `service/` (implementações).
- Controllers nunca acessam repository. Sem ciclos entre módulos.

### Módulos

**`shared/`** — o que todos usam
- `SecurityConfig`: tudo exige o token Bearer (RN-20), exceto `/actuator/health`, o início do login com
  Google e `POST /api/auth/refresh` e `/logout`. Também libera `GET /api/dev/**`, que só existe com o perfil
  `dev` (hoje não há nenhum controller ali; ver "Etapa B parqueada").
- `ClockConfig`: entrega o relógio como bean (os testes trocam por um fixo).
- `RateLimiter` + `internal/service/RateLimiterImpl`: limite de tentativas por chave e período (RA-08), sobre a
  tabela `rate_limit_attempts`.
- `Scope` (`PERSONAL`, `ARCHIVED`, `MEMBER_SIDE`, `COUPLE_CENTER`) e `Scopeable`: o vocabulário de "onde isto
  mora" (RN-09, RN-19). Ainda é só estrutura.
- `exception/GlobalExceptionHandler`: traduz exceções em respostas HTTP (`ProblemDetail`, mensagens fixas);
  `DuoHttpException` é a classe-base que cada módulo estende.

**`users/`** — conta e perfil
- `UserService`, `controller/UserController`: perfil (`/api/me/profile`), DuoName (`/api/me/handle`).
- Enums `MaritalStatus`, `ProfileVisibility`; `dto/` (`UserSummary`, `UserProfile`, `ProfileUpdate`,
  `ProfilePage`).
- `internal/model`: `User`, `NameNormalizer` (nome sem acento nem maiúsculas, para busca).
- Exceções: DuoName inválido/repetido/trocado demais, perfil inválido, e-mail já ligado a outro Google.

**`auth/`** — login e sessão
- `GoogleLoginSuccessHandler`: cria/acha a conta, grava o cookie `duo_refresh` e devolve a pessoa ao frontend.
- `AccessTokenService` (JWT de 15 min, HS256) e `RefreshTokenService` (token de renovação aleatório, guardado
  como hash, rotacionado a cada uso; reuso de um token antigo derruba a família). `RefreshCookies` monta o
  cookie (`HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/auth`).
- `controller/AuthController`: `POST /api/auth/refresh`, `/logout`, `/logout-all`. `MeController`: `GET /api/me`.

**`relationships/`** — o vínculo do casal
- `CouplePairingService` (convite → confirmação → história e datas) e `CoupleService` (consultar, pausar,
  retomar, sair). `CouplePermissions`: a matriz "o que cada estado permite" (4 perguntas puras; ainda não
  usada por fotos do casal).
- Enums `PairingState`, `CoupleState`, `StoryStep`, `Channel`.
- `internal/model`: `CouplePairing` (o convite e a máquina de estados), `CouplePairingStory` (história de
  cada pessoa), `Couple`, `CoupleMember`.
- `internal/service`: `CouplePairingServiceImpl`; `CoupleFactory` (cria o `Couple` numa transação própria, para
  a corrida de "um vínculo por pessoa" desfazer só o nascimento); `CoupleServiceImpl`.
- Endpoints: `/api/pairings` (`invite-by-handle`, `invite-by-link`, `redeem`, `mine`, `{id}`, `confirm`,
  `cancel`, `story/{step}`, `partner-story`, `dates`, `dates/accept`) e `/api/couples` (`mine`, `{id}`, `pause`,
  `resume`, `leave`).
- Depende de `social` (só para perguntar "há bloqueio entre A e B?").

**`social/`** — camada social (**congelada**, RF-A11)
- `Access` + `ProfileAccessService`: a regra central de quem vê o quê.
- `FollowService` (Duollow, pedidos), `BlockService`, `ReportService`, `ProfileViewService`.
- Controllers: `FollowController` e `ProfileController` (busca e perfil de terceiros) **só existem com
  `duo.features.social.enabled=true`**; `BlockController` e `ReportController` ficam sempre ligados.
- `SocialFeature`: a chave que liga/desliga.

**`media/`** — fotos
- `MediaService` e `controller/MediaController`: avatar (`/api/me/avatar`), fotos (`/api/me/photos`), ler,
  mudar visibilidade, apagar e denunciar (`/api/media/{id}`). Arquivo só sai por `GET /api/media/{id}` depois
  da regra de acesso (RN-19/21).
- `internal/service`: `ImageSniffer` (tipo real pelos bytes, RN-23), `PhotoAccessRule`, `MediaServiceImpl`.
- Limites provisórios: avatar 1 MB, foto 2 MB, 200 fotos e 100 MB por pessoa.

**`storage/`** — onde o arquivo fica
- `MediaStorageProvider` (contrato) + `internal/service/LocalMediaStorageProvider` (pasta local `~/duo-media`).
  Trocar para R2 depois muda só esta peça (DEC-019/021).

### Banco (Flyway, `src/main/resources/db/migration/`)

Nunca se edita uma migration já aplicada; muda-se com uma nova.

| Migration | O que faz |
| --- | --- |
| V1, V2, V7, V12 | `users` e suas colunas: e-mail único, `google_sub`, `handle` (DuoName), perfil e `search_name` |
| V3 | `refresh_tokens` |
| V4 | `couples`, `couple_members` (índice único parcial garante um vínculo por pessoa, RN-06) |
| V5, V6, V10, V11 | `couple_pairings`, código de convite (só o hash), datas, confirmações |
| V8 | `rate_limit_attempts` |
| V9 | `couple_pairing_stories` |
| V13 | `follows`, `blocks`, `reports` |
| V14 | `media_assets` |

### Configuração (`application.properties`)

Segredos vêm de variáveis de ambiente, nunca do arquivo. Pontos úteis: `ddl-auto=validate` (o schema é do
Flyway; o Hibernate só confere), `duo.features.social.enabled` (padrão `false`), `duo.media.*` (pasta e
limites), `duo.frontend-url`. Perfil `test` (`application-test.properties`) aponta para o banco `duo_test`.

### Testes (`src/test/java/com/duo/`, mesma estrutura dos pacotes)

Rodam contra um PostgreSQL local de verdade (`duo_test`), sem mocks de banco. Destaques:
- `ArchitectureTest`: sem ciclos, controller sem repository, `internal` privado ao módulo.
- `CouplePairingConcurrencyTest` e `RateLimiterConcurrencyTest`: corridas com threads reais.
- `RelationshipsHttpTest`, `MediaHttpTest`, `SocialHttpTest`, `UserProfileHttpTest`: HTTP via MockMvc,
  incluindo acesso cruzado (RN-12, RN-20).
- `SocialFrozenHttpTest`: com o social desligado, as rotas respondem 404.
- `SessionFlowTest`: refresh, rotação, reuso, token vencido, logout.

### Etapa B parqueada (`backend/wip-etapa-b-pendente/`)

Esboço de entrar por **código de 6 dígitos por e-mail** (`DevLoginController` e uma migration), **fora do
build** de propósito: a Etapa B foi adiada (DEC-030). O `build.gradle.kts` já lista `spring-boot-starter-mail`
para ela. Antes de ativar: a migration precisa de número novo (`V12` já existe) e o provedor de e-mail
(Q-18) está em aberto.

## `frontend/` — o app

Vite 8 + React 19 + TypeScript, PWA (`vite-plugin-pwa`; o service worker **não** guarda cache da API).
Alias `@/` aponta para `src/`. O proxy do Vite manda `/api` para `localhost:8080`.

```
src/
  app/        a moldura: main.tsx (entrada), App (rotas), AppLayout (painel lateral + navegação),
              Dashboard (decide para onde mandar a pessoa), MainNav, AuroraBackground (fundo)
  shared/
    api/      client.ts (chamadas HTTP e erros), types.ts, account.ts (escolher DuoName)
    session/  token (token de acesso só em memória, renovação com uma chamada só), usuário atual,
              cache de fotos
    ui/       Avatar, AuthedImage, BottomSheet, FollowButton, PhotoGrid, ReportForm
    lib/      imageTools (recorte e redução no aparelho), names, labels (textos da interface),
              features (a chave do social congelado), useMediaQuery (diz se uma media query do CSS vale agora)
    context/  meContext (o "eu" que o AppLayout carrega e reparte com as telas)
    styles/   tokens, base, buttons, forms, avatar, lists, layout, aurora (CSS global compartilhado)
    test/     test-setup, testHelpers
  features/   cada assunto com suas telas e seu api.ts; uma feature não importa de outra
    auth/           LoginScreen, HandleSetupScreen
    profile/        HomeScreen, EditProfileForm, SideProfile, AvatarMenu, PublicProfileScreen
    social/         SearchScreen, FollowListScreen (congelado)
    media/          NewMomentScreen
    relationships/  InviteScreen, AcceptInviteScreen, ConfirmScreen, StoryScreen,
                    DateConsensusScreen, CoupleHomeScreen
  index.css   só importa os arquivos de shared/styles
```

**Rotas** (`app/App.tsx`): `/inicio`, `/casal`, `/momentos/novo`, `/lista/:kind`,
`/convite/:code`, `/vinculo/handle`, `/vinculo/convidar`, `/vinculo/:pairingId` (+ `/historia`, `/datas`);
`/buscar` e `/u/:handle` só com o social ligado. Qualquer outra cai no `Dashboard`: sem DuoName vai escolher,
com casal vai a `/casal`, senão a `/inicio`.

**CSS:** o que várias telas usam fica global em `shared/styles/`; o que é de uma tela só fica em
`Componente.module.css` ao lado dela (CSS Modules). A ordem de import em `main.tsx` importa: `index.css`
precisa vir antes de qualquer tela, senão o CSS global vence o do módulo num empate.

**Fundo aurora:** 4 manchas (roxo, azul escuro, azul claro, rosa) com véu escuro para o texto ficar legível;
cores em `shared/styles/tokens.css` (`--aurora-*`), regras em `aurora.css`. Respeita `prefers-reduced-motion`.

**Vidro (glassmorphism) sobre a aurora:** os tokens `--glass-*` (`tokens.css`: tom roxo translúcido, brilho/gloss,
desfoque, borda e sombra) valem para o cartão do login, para a área de conteúdo (`.app-content`, vidro na tela
toda, sem margem e sem cantos) e para a barra lateral. Sem suporte a `backdrop-filter` o tom fica mais opaco.

**Barra lateral** (`layout.css`, `SideProfile`, `AvatarMenu`): vidro no roxo da marca (tokens `--sidebar-*`; dentro
dela as cores do app são trocadas pelas claras). No celular o vidro sai do painel e vai para cada faixa (o
`backdrop-filter` no painel prenderia a faixa fixa de baixo). `SideProfile` mostra, abaixo da foto, nome, DuoName e situação (profissão · estado civil), só no computador; no
celular esse bloco some da faixa e o nome continua na `HomeScreen`. `AvatarMenu` (só no computador, `min-width: 768px`):
a foto é um botão com um círculo em degradê da aurora que gira; ao clicar abre um cartão ao lado ("Colocar foto de
perfil" sem foto, "Trocar foto de perfil" com foto), posicionado com `position: fixed` num portal porque o painel
rola por dentro. No celular a foto continua só uma imagem (o perfil por lá ainda vai ser pensado).

**Tela de login** (`features/auth/LoginScreen`): a ilustração (`public/images/login-art.png`, 3840×2160) é o fundo
da tela inteira, dentro de um "palco" 16:9 para os efeitos por cima dividirem as coordenadas em % da imagem:
halos pulsando sobre as estrelas com anéis e vaga-lumes subindo perto do casal (só `opacity`/`transform`). No
computador: cartão de vidro de altura total à direita (marca, lema, descrição, botão do Google com o "G" e o aviso
dos termos, que por ora é **texto sem link**). No celular: a imagem é mais larga que a tela e se arrasta de lado
(`scroll-snap`): num lado o casal, no outro a frase promocional; um painel de vidro embaixo guarda o botão.

**Fonte:** Instrument Sans, hospedada no app (`@fontsource-variable/instrument-sans`, importada em `main.tsx`; os
`.woff2` entram no cache offline do service worker).

**Outros:** `public/images/` guarda arte pesada (fora do cache offline do service worker, limite de 2 MB por
arquivo; a `login-art.png` tem 13 MB e ainda não foi otimizada); `public/pwa-*.png` são ícones **provisórios**.
Comandos: `npm run dev | test | lint | format:check | build`. Dependências de runtime: React, React Router,
`lucide-react` (ícones) e `@fontsource-variable/instrument-sans` (fonte), ambos em versão fixa.

## `.github/workflows/ci.yml`

A cada push ou pull request na `main`, dois jobs em paralelo: **backend** (sobe um Postgres descartável e
roda `./gradlew build`: formatação Spotless, compilação e testes incluindo ArchUnit) e **frontend** (`npm ci`,
lint, `format:check`, testes e build). A senha do Postgres do CI não é segredo real.
