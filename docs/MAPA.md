# Mapa do projeto

> Para que serve este arquivo: explicar, em português simples, o que cada pasta e cada arquivo importante
> faz e como as peças se conectam. Exigido pela regra "Modo acompanhamento" do `CLAUDE.md`.
> **Atualize-o sempre que um arquivo for criado, movido ou removido.**

## Visão geral

```
Duo-Universe/
  CLAUDE.md        contexto do projeto para quem (humano ou IA) for trabalhar nele
  docs/            este mapa e o glossário técnico
  backend/         a API (Spring Boot + Java)
  frontend/        o app que roda no navegador/celular (React + Vite, PWA)
  .github/         configuração do CI (verificação automática a cada push)
```

Hoje existe o **login completo**: entrar com Google, sessão própria (tokens), sair de um aparelho ou de todos,
e uma tela inicial provisória no frontend. Ainda não há casal, nem fotos — isso vem a partir da Semana 3
(seção 17 do `CLAUDE.md`). A seção "Sessão própria" no fim deste arquivo explica como as peças do login se
conectam.

## `backend/` — a API

**Arquivos de build (como o projeto é compilado):**
- `build.gradle.kts`: a "receita" do projeto em Kotlin. Lista as dependências (bibliotecas prontas que o
  projeto usa, como Spring Security) e as ferramentas de qualidade (Spotless, que corrige formatação).
- `settings.gradle.kts`: diz ao Gradle o nome do projeto.
- `gradlew` / `gradlew.bat`: scripts que baixam e rodam a versão certa do Gradle sozinhos, sem precisar
  instalar o Gradle na mão. É por isso que rodamos `.\gradlew.bat build` e não `gradle build`.
- `gradle/wrapper/`: arquivos de apoio do script acima (não precisa abrir).

**Código (`src/main/java/com/duo/`):**
- `DuoBackendApplication.java`: o `main()` do projeto — o arquivo que, ao ser executado, liga toda a
  aplicação Spring Boot (lê as configurações, conecta no banco, abre a porta HTTP).
- `shared/SecurityConfig.java`: a regra de acesso — **tudo exige o token de acesso (Bearer), exceto
  `/actuator/health`, o início do login com Google e `POST /api/auth/refresh` e `/logout`** (RN-20).
- `shared/ClockConfig.java`: entrega o relógio do sistema como bean; os testes o trocam por um relógio fixo.
- `auth/package-info.java` e `users/package-info.java`: a "placa" (Javadoc do pacote) de cada módulo. `auth`
  cuida de login e sessão; `users` cuida da conta e, no futuro, do Personal Universe.

**Configuração (`src/main/resources/`):**
- `application.properties`: onde o backend acha o banco de dados (endereço, usuário, senha — a senha vem
  de uma variável de ambiente, nunca do arquivo, regra 11) e ajustes finos (ex.: `ddl-auto=validate` diz ao
  Hibernate "só confira se o banco bate com as entidades, não crie tabelas sozinho — isso é trabalho do
  Flyway").
- `db/migration/V1__create_users.sql`: a primeira "migration" do Flyway — um script SQL versionado que cria
  a tabela `users`. `V2__add_google_sub_to_users.sql` acrescenta `google_sub` (único, obrigatório; DEC-030).
  `V3__create_refresh_tokens.sql` cria `refresh_tokens`. `V4__create_couples.sql` (Épico 2, Etapa 1) cria
  `couples` e `couple_members`. Cada migration nova vira um arquivo `V5__...`, `V6__...`, nunca editamos um
  já aplicado.

**Testes (`src/test/java/com/duo/`):**
- `DuoBackendApplicationTests.java`: só liga a aplicação inteira e confere que nada quebra ao subir.
- `HealthEndpointTest.java`: confirma que `/actuator/health` é público e que qualquer outra rota pede login.
- `OpenApiDocsTest.java`: confirma que a documentação da API (gerada pelo Springdoc) também exige login.
- `ArchitectureTest.java`: não testa o que o código faz, testa **como ele está organizado** — garante que os
  módulos não dependem um do outro em círculo, que controllers nunca acessam o banco direto, e que a pasta
  `internal` de um módulo só é usada por ele mesmo.
- `users/internal/UserRepositoryTest.java`: primeiro teste que fala com o PostgreSQL de verdade (`duo_test`),
  usando `@DataJpaTest` — confirma que `User` é salvo e encontrado pelo `UserRepository`.
- `application-test.properties` (em `src/test/resources`): troca o banco de desenvolvimento (`duo`) pelo
  banco de teste (`duo_test`), para os testes nunca sujarem dados reais.

**`users/internal/User.java`:** a primeira entidade JPA do projeto — mapeia a tabela `users` (da migration
`V1` e `V2`). Tem um construtor protegido vazio (exigido pelo Hibernate) e um construtor público com os campos
obrigatórios; `createdAt` é preenchido sozinho (`@CreationTimestamp`), nunca pelo código que cria o `User`.

**`users/internal/UserRepository.java`:** a interface Spring Data JPA que dá acesso ao banco para `User`
(`save`, `findById`, etc., herdados de `JpaRepository`) mais uma busca própria por e-mail sem diferenciar
maiúsculas (`findByEmailIgnoreCase`) e por `googleSub` (`findByGoogleSub`), usada pelo futuro login com Google.

**O que falta para existir um fluxo completo:** ainda não há `controller` nem `service` — faltam essas duas
camadas para formar o caminho de uma requisição:

```
navegador → SecurityConfig (confere login) → Controller (recebe o HTTP)
          → Service (regra de negócio) → UserRepository (fala com o banco) → PostgreSQL
```

## `frontend/` — o app

- `vite.config.ts`: a configuração do Vite (a ferramenta que builda e serve o frontend). Aqui também vive o
  plugin de PWA (o que faz o site "se instalar" no celular) e o proxy que redireciona `/api/...` para o
  backend em `localhost:8080` durante o desenvolvimento, imitando a mesma origem que teremos em produção.
- `index.html`: a página HTML única do app (todo o resto é desenhado por React dentro dela).
- `src/main.tsx`: o ponto de entrada — pega a `<div id="root">` do `index.html` e manda o React desenhar o
  `<App />` dentro dela.
- `src/App.tsx`: o componente principal. Decide qual tela mostrar (carregando, entrar ou início); veja a seção "Sessão própria".
- `src/App.test.tsx` / `src/test-setup.ts`: o teste do componente acima e a configuração que liga o Vitest
  (framework de teste) ao `jsdom` (um navegador falso que roda em memória, sem abrir uma janela de verdade).
- `src/index.css`: estilo global básico.
- `public/pwa-192.png`, `public/pwa-512.png`: os ícones do app quando instalado no celular — **provisórios**.
- `package.json`: lista as dependências (React, Vite, ferramentas de teste e lint) e os comandos disponíveis
  (`npm run dev`, `npm test`, etc.).
- `eslint.config.js`, `.prettierrc`, `.prettierignore`: regras de qualidade de código (ESLint acha erros e
  maus hábitos; Prettier só formata, sem opinar sobre lógica).
- `tsconfig.json`: as regras do TypeScript (o quão estrito ele é ao checar tipos).

## `.github/workflows/ci.yml` — a verificação automática

A cada `push` ou pull request na branch `main`, o GitHub sobe uma máquina temporária e roda dois jobs em
paralelo:
- **`backend`**: sobe um Postgres descartável (só para esse job) e roda `./gradlew build`, que compila,
  formata (Spotless) e testa (incluindo o `ArchitectureTest`).
- **`frontend`**: instala as dependências com `npm ci` e roda lint, checagem de formatação, testes e build.

Se qualquer um desses passos falhar, o GitHub marca o commit como vermelho — é esse sinal que ainda
precisamos confirmar na aba Actions do repositório (seção 19 do `CLAUDE.md`).

## Login com Google (Épico 1, etapa 4 — DEC-030)

- `users/UserService.java` + `UserSummary.java`: a **API pública** do módulo `users`. Outros módulos só conversam com ele por aqui. `findOrCreateByGoogle` acha a pessoa pelo `sub` do Google ou cria a conta no primeiro acesso.
- `users/internal/UserServiceImpl.java`: a implementação (pacote `internal`, invisível para outros módulos — o ArchUnit garante).
- `auth/GoogleLoginSuccessHandler.java`: roda quando o Google confirma a identidade. Recusa e-mail não verificado (RA-01) e chama o `UserService`.
- `auth/MeController.java`: `GET /api/me`, devolve quem está logado. Serve para provar o login.
- `shared/SecurityConfig.java`: liga o `oauth2Login`; sem login, qualquer rota responde 401 (RN-20).
- `application.properties`: registro do Google lendo `DUO_GOOGLE_CLIENT_ID` e `DUO_GOOGLE_CLIENT_SECRET` do ambiente.

Fluxo: navegador → `/oauth2/authorization/google` → Google → `/login/oauth2/code/google` → handler → `UserService` → banco → `/api/me`.

## Sessão própria (Épico 1, passos 5 a 7 — RA-04, RA-05, H4)

Depois do login com Google, o Duo não usa a sessão padrão do Spring: usa dois tokens.

| Token | Dura | Onde vive | Para quê |
| --- | --- | --- | --- |
| **Acesso (JWT)** | 15 min | só na memória do frontend | provar quem você é em cada chamada (`Authorization: Bearer`) |
| **Renovação** | 30 dias, uso único | cookie `HttpOnly` (o JavaScript não lê) | pedir um token de acesso novo |

**Backend, módulo `auth`** (`src/main/java/com/duo/auth/`):
- `RefreshTokenService.java` + `IssuedRefreshToken.java`: a API pública do token de renovação (emitir, rotacionar,
  sair deste aparelho, sair de todos).
- `AccessTokenService.java` + `AccessToken.java`: a API pública do token de acesso.
- `AuthController.java`: `POST /api/auth/refresh`, `/logout` e `/logout-all`.
- `MeController.java`: `GET /api/me`; agora lê quem é a pessoa **do token**, nunca de um id enviado.
- `RefreshCookies.java`: monta e lê o cookie `duo_refresh` (`HttpOnly`, `Secure`, `SameSite=Strict`, `/api/auth`).
- `GoogleLoginSuccessHandler.java`: ao terminar o login com Google, cria/acha a conta, grava o cookie e manda a
  pessoa de volta ao frontend (com `?erro=...` se for recusada).
- `internal/RefreshToken.java`, `RefreshTokenRepository.java`, `RefreshTokenServiceImpl.java`: a tabela
  `refresh_tokens` (migration `V3`) e a regra de rotação: cada uso troca o token por outro da mesma "família";
  se um token **já usado** reaparece, a família inteira é encerrada. O banco guarda só o hash (SHA-256).
- `internal/JwtConfig.java`, `JwtAccessTokenService.java`: assinam e validam o JWT com `DUO_JWT_SECRET`.

**Módulo `users`:** `UserService.findById` (usado pelo `/api/me`) e `EmailAlreadyLinkedException` (e-mail já
ligado a outra conta).

**Frontend** (`frontend/src/`):
- `session.ts`: guarda o token de acesso em memória, renova quando vence e repete a chamada; junta pedidos
  simultâneos de renovação numa só (renovação é de uso único).
- `App.tsx`: decide a tela — carregando, `LoginScreen.tsx` (botão "Entrar com Google") ou `HomeScreen.tsx`
  (tela inicial provisória com "Sair" e "Sair de todos os aparelhos").
- `session.test.ts` e `App.test.tsx`: testes com um backend simulado.

**Testes novos no backend:** `SessionFlowTest` (fluxo completo pelos filtros de segurança), `RefreshToken*Test`,
`UserServiceConflictTest` (primeiro login simultâneo e e-mail repetido) e o `GoogleLoginSuccessHandlerTest`.

Fluxo completo:

```
Entrar com Google → Google → GoogleLoginSuccessHandler → cookie duo_refresh → frontend
frontend → POST /api/auth/refresh (cookie) → token de acesso (JWT) na memória
frontend → GET /api/me (Bearer) → quem sou eu
JWT venceu (401) → refresh de novo → repete a chamada
```

## Épico 2 — o vínculo (módulo `relationships`, em andamento)

Especificação aprovada em `docs/epico-2-vinculo.md`; progresso etapa a etapa na seção 19 do `CLAUDE.md`.

**Etapa 1 — esqueleto do casal (concluída):**
- `relationships/package-info.java`: a "placa" do módulo novo — o vínculo entre duas pessoas, do convite ao
  casal nascido, até a saída. Ainda não sabe nada de fotos/memórias.
- `relationships/internal/Couple.java`: a entidade do casal (migration `V4`) — estado (`ACTIVE`/`PAUSED`/
  `ENDED`) e as duas datas do relacionamento, ainda sempre nulas (ninguém as preenche até a Etapa 7). Sem
  nenhuma regra de transição ainda — isso chega nas Etapas 8 e 10.
- `relationships/internal/CoupleMember.java`: uma pessoa dentro de um casal. `leftAt` nulo = "ainda dentro do
  vínculo"; é esse campo que o índice único da migration usa para valer a RN-06.
- `relationships/internal/CoupleRepository.java`, `CoupleMemberRepository.java`: acesso ao banco para as duas
  entidades acima; `findByUserIdAndLeftAtIsNull` é a consulta que vai apoiar a regra de concorrência mais
  adiante.
- `db/migration/V4__create_couples.sql`: cria `couples` e `couple_members`, com o índice único **parcial**
  `couple_members_active_person_unique` — só existe para linhas com `left_at IS NULL`. É a peça que faz o
  banco recusar, sozinho, uma segunda linha "ativa" para a mesma pessoa (RN-06).
- `relationships/internal/CoupleMemberRepositoryTest.java`: prova as três coisas acima contra o banco de
  teste real: salva e acha a filiação ativa; recusa uma segunda filiação ativa para a mesma pessoa; permite
  uma nova filiação depois que a pessoa "sai" da anterior (`leftAt` preenchido).

**Etapa 2 — o convite como máquina de estados pura (concluída):**
- `relationships/Channel.java`, `PairingState.java`: os dois "vocabulários" públicos do convite — como ele
  chegou (`HANDLE`/`LINK`) e em que pé está (`PENDING`/`CONFIRMED`/`CANCELLED`).
- `relationships/PairingSummary.java`: o que o resto do sistema pode saber sobre um convite, sem tocar na
  entidade JPA (que é `internal`).
- `relationships/PairingNotFoundException.java`, `IllegalPairingTransitionException.java`: os dois erros de
  negócio deste pedaço — "não existe (ou não é seu)" e "esse convite já foi decidido".
- `relationships/CouplePairingService.java` (API pública) + `internal/CouplePairingServiceImpl.java`:
  `initiate` (cria `PENDING`, vence em 7 dias — RN-29), `cancel` (qualquer um dos dois desiste) e `find`
  (busca o convite; se já venceu e ainda estava `PENDING`, marca `CANCELLED` **na hora** — é a "expiração
  lazy": não existe nenhum processo rodando sozinho de tempos em tempos, só confere quando alguém olha).
- `relationships/internal/CouplePairing.java`: a entidade. `cancel`/`markConfirmed` só funcionam se o estado
  ainda for `PENDING` — tentar de novo (ex.: cancelar um convite já cancelado) lança
  `IllegalPairingTransitionException`. `markConfirmed` já existe, mas só será chamado de verdade na Etapa 8
  (quando os dois lados tiverem confirmado).
- `db/migration/V5__create_couple_pairings.sql`: cria `couple_pairings`. Sem índice único aqui — ainda é
  permitido alguém ter mais de um convite pendente ao mesmo tempo (decisão de deixar para a Etapa 12).
- `relationships/internal/CouplePairingTest.java`: testa a máquina de estados sozinha, sem banco (rápido).
- `relationships/internal/CouplePairingServiceImplTest.java`: testa o serviço contra o `duo_test` real, com
  o relógio "congelado" em um instante escolhido — é assim que se testa "passou uma semana" sem esperar uma
  semana de verdade.

**Etapa 3 — convite por link/código (concluída):**
- `relationships/IssuedPairingInvite.java`: o código do convite em texto puro, só para A mandar por fora
  (WhatsApp etc.) — o `toString` o esconde de propósito (nunca em log, RA-09), igual ao `IssuedRefreshToken`
  do login.
- `relationships/InviteCodeInvalidException.java`: um erro só, de propósito, para quatro motivos diferentes
  (código não existe, já foi usado, venceu, ou a própria pessoa que criou o link tentando usá-lo) — não
  revelar qual deles é o real.
- `relationships/CouplePairingService.java` ganhou `inviteByLink` (gera o código, guarda só o hash) e
  `redeemByLink` (alguém usa o código; se já tiver vencido, o pareamento é cancelado na hora — mesma
  "expiração lazy" da Etapa 2).
- `relationships/internal/CouplePairing.java` ganhou `inviteCodeHash` e `redeemLink` — preenche quem aceitou
  o convite; só funciona uma vez.
- `db/migration/V6__add_invite_code_to_pairings.sql`: acrescenta `invite_code_hash` a `couple_pairings`, com
  índice único parcial (só conta quando não é nulo — convites por @handle, da Etapa 5, não têm código).
- Testes: a máquina de estados do código (sem banco, em `CouplePairingTest`) e o fluxo completo contra o
  `duo_test` real (`CouplePairingServiceImplTest`), incluindo "o código nunca fica salvo em texto puro".

**Etapa 4 — coluna `handle` em `users` (concluída):**
- `db/migration/V7__add_handle_to_users.sql`: acrescenta `handle` a `users`, opcional (quem já tinha conta
  ainda não escolheu o seu) com índice único *case-insensitive* (`lower(handle)`), igual ao que já existe
  para `email` desde a `V1`.
- `users/internal/User.java` ganhou o campo `handle` e `getHandle`/`setHandle`.
- `users/HandleTakenException.java`, `InvalidHandleException.java`: os dois erros de negócio deste pedaço —
  "já pertence a outra conta" e "não tem o formato certo" (3 a 20 letras, números ou `_`).
- `users/UserService.java` (API pública) ganhou `chooseHandle` e `findByHandle`; `UserSummary` agora carrega
  o `handle` (`null` para quem ainda não escolheu).
- `users/internal/UserServiceImpl.java`: valida o formato antes de tocar no banco; se o handle já estiver em
  uso, o índice único da migration recusa o `saveAndFlush` e o erro é traduzido para `HandleTakenException`
  — mesmo padrão já usado para e-mail duplicado em `findOrCreateByGoogle`.
- Testes novos em `UserServiceImplTest`: escolhe e encontra por handle ignorando maiúsculas, recusa handle já
  usado por outra conta, recusa formato inválido.

**Etapa 5 — convite por @handle, caminho padrão (concluída):**
- `relationships/HandleNotInvitableException.java`: o erro genérico de "não posso convidar esse @handle" —
  junta três motivos (não existe, é seu, já está vinculado) numa mensagem só, de propósito (anti-enumeração,
  RN-30/RA-07). Ver `docs/GLOSSARIO-TECNICO.md`.
- `relationships/CouplePairingService.java` ganhou `inviteByHandle`: busca a pessoa por `UserService.findByHandle`
  (módulo `users`, Etapa 4), confere que não é a própria pessoa e que ela não está em outro vínculo
  (`CoupleMemberRepository.findByUserIdAndLeftAtIsNull`), e só então cria o convite — reaproveitando o
  `initiate` que já existia.
- `relationships/internal/CouplePairingServiceImpl.java`: antes de qualquer busca, chama o `RateLimiter`
  (abaixo) para o RA-08 — a tentativa conta mesmo se o handle não existir, senão o limite não protegeria nada.
- Novo módulo auxiliar `shared/RateLimiter.java` (interface) + `shared/RateLimitExceededException.java` +
  `shared/internal/RateLimitAttempt.java`/`RateLimitAttemptRepository.java`/`RateLimiterImpl.java`: o freio
  de mão contra força bruta (RA-08), guardado na tabela nova `rate_limit_attempts` (migration `V8`). Pensado
  para ser reaproveitado por outras rotas depois (ex.: quando a Etapa 13 adicionar o IP, além da conta).
- Testes novos: 5 em `CouplePairingServiceImplTest` (acha pelo handle, rejeita handle inexistente/próprio/já
  vinculado, respeita o limite de tentativas) e 5 em `RateLimiterImplTest` (permite até o máximo, bloqueia
  depois, reseta quando a janela passa, não mistura contadores de chaves/rotas diferentes).

Ainda falta: tela para escolher o @handle (Etapa 14) e o controller HTTP de convite (Etapa 13) — hoje tudo
isso só existe no `Service`, chamado pelos testes.

**Etapa 6 — história em 4 passos (concluída):**
- `relationships/StoryStep.java`: os 4 passos fixos da história (como nos conhecemos, o tempo antes de
  namorar, o que eu pensava, o que eu penso agora — mesma ordem de `docs/epico-2-vinculo.md`).
- `relationships/StorySummary.java`: o que o resto do sistema pode saber sobre a história de alguém (os 4
  textos), sem tocar na entidade JPA.
- `relationships/StoryIncompleteException.java`: erro de "ainda falta preencher algum passo" — usado por
  quem exigir a história pronta (a Etapa 8 vai reusar isso na hora de "firmar" o vínculo).
- `relationships/internal/CouplePairingStory.java`: a entidade — uma linha por pessoa por pareamento, com os
  4 passos em colunas (não em linhas separadas: são sempre os mesmos 4, sempre lidos e escritos juntos).
  `updateStep` troca o texto de um passo por vez, sem validar nada — é assim que o **rascunho** existe pela
  metade enquanto a pessoa ainda escreve. `isComplete()` só confere se os 4 passos têm texto depois de
  `trim()` — **revisão de 2026-11-05:** o plano original pedia um mínimo de 20 caracteres (RN-25); o fundador
  decidiu que isso pune quem responde curto e sincero sem garantir resposta de verdade, então o texto ficou
  livre (só não pode ficar vazio).
- `relationships/internal/CouplePairingStoryRepository.java`: acesso ao banco (`findByPairingIdAndUserId`).
- `db/migration/V9__create_couple_pairing_stories.sql`: cria `couple_pairing_stories`, com índice único em
  `(pairing_id, user_id)` — uma história por pessoa por pareamento.
- `relationships/internal/CouplePairing.java` ganhou `partnerOf(userId)`: devolve a outra pessoa do
  pareamento (ou `null` se for um convite por link ainda não usado por ninguém) — é como `readPartnerStory`
  sabe de quem buscar a história.
- `relationships/CouplePairingService.java`/`internal/CouplePairingServiceImpl.java` ganharam
  `saveStoryDraft` (grava um passo, sem validar), `isStoryComplete`/`requireStoryComplete` (RN-25) e
  `readPartnerStory` — só devolve a história do parceiro quando **as duas** (a própria e a dele) estiverem
  completas (RN-26); nunca antes, mesmo se só a minha estiver pronta.
- Testes novos (17): `CouplePairingStoryTest` (a lógica de `isComplete`, sem banco), 3 testes de `partnerOf`
  em `CouplePairingTest`, e 8 testes de fluxo completo em `CouplePairingServiceImplTest` contra o `duo_test`
  real (rascunho incompleto, texto livre sem mínimo, acesso cruzado recusado, `readPartnerStory` só liberando
  com as duas completas, e vazio para convite por link ainda não usado).
- **Sem tela nova nesta etapa** (decisão do fundador, 2026-11-05): a demonstração é só pelos testes — a tela
  real do vínculo é a Etapa 14, quando já houver dados reais (casal, fotos) para ligar nela.
