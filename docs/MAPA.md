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

## Como os pacotes do backend estão organizados (reorganização de 2026-11-06)

Cada módulo (`shared`, `users`, `auth`, `relationships`) segue a mesma convenção, por **camada técnica**
(`docs/plano-reorganizacao-pacotes.md`):

- **Raiz do módulo = o que os outros módulos podem usar:** as pastas `controller/`, `exception/` e `dto/`, mais
  as interfaces de serviço e os enums, que ficam soltos (são o "contrato" do módulo).
- **`internal/` = só o próprio módulo usa** (o `ArchitectureTest` garante): `model/` (entidades do banco),
  `repository/` (acesso ao banco) e `service/` (implementações e orquestradores).
- **Sobras** (configurações e utilitários que não cabem numa dessas pastas) ficam soltas, sem pasta própria.

> **Atenção ao ler as seções mais abaixo:** elas são o diário de cada etapa e citam os caminhos **de antes** da
> reorganização (ex.: `users/internal/User.java`). Para achar o arquivo hoje, use a árvore desta seção: a classe
> está na subpasta da sua camada (ex.: `users/internal/model/User.java`).

```
shared/
  exception/    DuoHttpException, GlobalExceptionHandler, RateLimitExceededException
  RateLimiter, Scopeable, Scope                    (contratos, soltos)
  ClockConfig, SecurityConfig                      (configurações, soltas)
  internal/
    model/        RateLimitAttempt
    repository/   RateLimitAttemptRepository
    service/      RateLimiterImpl

users/
  controller/   UserController
  exception/    EmailAlreadyLinkedException, HandleTakenException, InvalidHandleException
  dto/          UserSummary
  UserService                                      (contrato, solto)
  internal/
    model/        User
    repository/   UserRepository
    service/      UserServiceImpl

auth/
  controller/   AuthController, MeController
  dto/          AccessToken, IssuedRefreshToken
  AccessTokenService, RefreshTokenService          (contratos, soltos)
  GoogleLoginSuccessHandler, RefreshCookies        (sobras, soltas)
  internal/
    model/        RefreshToken
    repository/   RefreshTokenRepository
    service/      JwtAccessTokenService, RefreshTokenServiceImpl
    JwtConfig                                      (configuração, solta)

relationships/
  controller/   CoupleController, CouplePairingController
  exception/    AlreadyInRelationshipException, CoupleNotFoundException, DatesNotAcceptedException,
                HandleNotInvitableException, IllegalCoupleTransitionException,
                IllegalPairingOperationException, IllegalPairingTransitionException,
                InviteCodeInvalidException, PairingNotFoundException, StoryIncompleteException
  dto/          CoupleSummary, PairingSummary, StorySummary, IssuedPairingInvite
  Channel, CoupleState, PairingState, StoryStep    (enums do contrato, soltos)
  CouplePairingService, CoupleService, CouplePermissions   (contratos, soltos)
  internal/
    model/        Couple, CoupleMember, CouplePairing, CouplePairingStory
    repository/   CoupleRepository, CoupleMemberRepository, CouplePairingRepository,
                  CouplePairingStoryRepository
    service/      CoupleServiceImpl, CouplePairingServiceImpl, CoupleFactory
```

**Os testes seguem a mesma estrutura** em `src/test/java/com/duo/`: um teste que usa membros *package-private*
fica no mesmo pacote da classe que testa (ex.: `CouplePairingTest` em `relationships/internal/model/`;
`RelationshipsHttpTest` em `relationships/controller/`, porque usa tipos internos do controller).

**Ajustes de visibilidade feitos na reorganização** (não mudam comportamento): ao separar em pacotes diferentes,
o que era visível "por estar na mesma pasta" precisou virar `public` — `RefreshCookies` (classe e
`set`/`clear`/`read`), `JwtConfig` (classe e `ISSUER`) e `CouplePairing.requireConfirmed()`.

## Épico 3 — Personal Universe (2026-11-06): perfil, social, fotos

Três módulos novos (`social`, `storage`, `media`) e um `users` maior. Dependências, sempre num sentido só:
`media → social → users`, `media → storage`. (`users` nunca conhece `social` nem `media`.)

```
users/ (ampliado)
  MaritalStatus, ProfileVisibility                (enums do contrato)
  dto/          UserProfile, ProfileUpdate, ProfilePage
  exception/    InvalidProfileException, InvalidSearchException, HandleChangeLimitException
  controller/   UserController                    (+ GET/PUT /api/me/profile)
  internal/model/    User (bio, profissão, estado civil, visibilidade, search_name, hidden_at, avatar...)
                     NameNormalizer               (minúsculas e sem acento, para a busca)

social/                                           (quem pode ver o quê; Duollow; bloqueio; denúncia)
  Access                                          (OWNER, FOLLOWER, PUBLIC_VIEWER, OUTSIDER, UNAVAILABLE)
  ProfileAccessService, FollowService, BlockService, ReportService, ProfileViewService, ReportReason
  dto/          PublicProfile, ProfileCard, SearchItem, SearchPage, Relation
  exception/    ProfileNotFoundException, FollowRequestNotFoundException, FollowLimitException,
                InvalidSocialActionException
  controller/   ProfileController, FollowController, BlockController
  internal/
    model/        Follow, FollowStatus, Block, Report, ReportTarget
    repository/   FollowRepository, BlockRepository, ReportRepository
    service/      ProfileAccessServiceImpl (a regra central), ProfileLookup, SocialMapper,
                  FollowServiceImpl, BlockServiceImpl, ReportServiceImpl, ProfileViewServiceImpl

storage/                                          (guarda bytes por uma chave; não sabe o que são)
  MediaStorageProvider
  internal/service/  LocalMediaStorageProvider    (pasta do computador, com defesa contra "..")

media/                                            (avatar e fotos do Personal)
  MediaKind, MediaVisibility, MediaService
  dto/          MediaSummary, MediaContent, UploadedFile
  exception/    InvalidMediaException, MediaQuotaExceededException, MediaNotFoundException
  controller/   MediaController
  internal/
    model/        MediaAsset
    repository/   MediaAssetRepository
    service/      MediaServiceImpl, ImageSniffer (tipo real pelos bytes), PhotoAccessRule (matriz de acesso)
```

**Migrations:** `V12` (perfil em `users`), `V13` (`follows`, `blocks`, `reports`), `V14` (`media_assets` e a
chave estrangeira do avatar). **Configuração** (`application.properties`): `duo.media.dir` (pasta das fotos, fora
do repositório; `DUO_MEDIA_DIR`), `duo.media.max-photos`, `duo.media.max-total-bytes`, limites de multipart.

**O fluxo de uma foto:** o app reduz a imagem no aparelho → `POST /api/me/photos` (multipart) → `MediaController`
→ `MediaServiceImpl` confere tipo real, tamanho e cota → `LocalMediaStorageProvider` grava o arquivo → a linha vai
para `media_assets`. Para **ver**: `GET /api/media/{id}` → `MediaServiceImpl.openFor` pergunta a
`ProfileAccessService` quem é o visitante em relação ao dono (`Access`) → `PhotoAccessRule` decide → só então o
arquivo sai (404 igual para "não existe" e "não é para você").

**Frontend** (`frontend/src/`): `api.ts` (chamadas), `labels.ts` (textos: DuoName, Duollowers...), `imageTools.ts`
(recorte e redução com canvas), `session.ts` (agora também o cache das fotos por sessão), componentes
`AuthedImage`, `Avatar`, `FollowButton`, `PhotoGrid`, `ReportForm`, `MainNav`; telas `HomeScreen` (/inicio),
`EditProfileScreen` (/perfil/editar), `PublicProfileScreen` (/u/:handle), `SearchScreen` (/buscar),
`FollowListScreen` (/lista/:kind), `NewMomentScreen` (/momentos/novo). O `Dashboard` agora leva o solteiro à
home (e quem tem casal ativo a `/casal`). Tokens de design em `index.css`.

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

## Épico 2 — frontend (Etapa 14)

- `src/api.ts` (novo): todas as chamadas HTTP do vínculo (convidar, firmar, história, datas, pausar/sair),
  mais os tipos TypeScript espelhando os DTOs do backend (`Pairing`, `Couple`, `Story`). Uma função central,
  `call<T>`, cuida de três coisas: usa sempre `authedFetch` (nunca `fetch` direto, pra não perder o token);
  trata `204 No Content` como `null` (é assim que `/api/couples/mine` diz "você não tem casal ainda"); e
  traduz qualquer erro HTTP num `ApiError` (com a mensagem que o `GlobalExceptionHandler` do backend mandou),
  pra cada tela mostrar algo sensato em vez de travar.
- `src/session.ts`: o tipo `User` ganhou `handle: string | null` — o `/api/me` já devolvia isso, só faltava
  o frontend saber.
- **`react-router-dom` (nova dependência):** antes, `App.tsx` só trocava de tela com `useState`. Isso não dá
  conta de um link de convite de verdade (`/convite/ABC123`, mandado por fora do app, que precisa abrir numa
  tela específica) sem reinventar parte de um roteador na mão. Zero custo (biblioteca de código aberto,
  padrão em apps React).
- **As telas novas, uma por arquivo** (`HandleSetupScreen`, `InviteScreen`, `AcceptInviteScreen`,
  `ConfirmScreen`, `StoryScreen`, `DateConsensusScreen`, `CoupleHomeScreen` — substitui a antiga
  `HomeScreen.tsx`, removida) e `Dashboard.tsx`: a peça que decide pra onde mandar a pessoa ao abrir o app,
  sem ela saber nenhum id de antemão — consulta `/api/couples/mine` e `/api/pairings/mine` e navega pro
  lugar certo (sem @handle → escolher um; com casal → `/casal`; com convite pendente → a tela de firmar; nada
  disso → convidar alguém).
- `App.tsx`: continua decidindo só *loading/anônimo/autenticado* (como antes); uma vez autenticado, quem
  decide a tela certa dentro do vínculo é o roteador (`<Routes>`), não mais um `useState` só.
- **Limitação conhecida, documentada no código** (`AcceptInviteScreen.tsx`): se alguém clica num link de
  convite sem estar logado, ou logado mas sem @handle ainda, o link se perde depois do login/da escolha do
  handle (o app não guarda "pra onde eu ia antes de parar"). Aceitável para o piloto; resolver isso de verdade
  precisaria de mais uma peça (guardar o destino, repassar pelo redirect do Google) — não implementado agora,
  pra não aumentar o escopo sem necessidade real ainda.
- **Sem tempo real ainda** (combinado, é o Bloco D mais pra frente): `ConfirmScreen` não avisa sozinha quando
  a outra pessoa confirma — a pessoa atualiza manualmente.
- `src/App.test.tsx`: reescrito — autenticar agora passa pelo `Dashboard` antes de chegar numa tela, então os
  testes existentes precisaram de mocks novos (`/api/couples/mine`, `/api/pairings/mine`). Dois testes novos:
  sem @handle vai pra tela de escolher um; sem casal e sem convite vai pra tela de convidar.
- `src/api.test.ts` (novo): o `call()` central — sucesso, `204 → null`, erro traduzido com a mensagem do
  backend, erro sem corpo não quebra.
- **Cobertura que ficou de fora, por tempo:** não há testes dedicados para `AcceptInviteScreen`,
  `ConfirmScreen`, `StoryScreen`, `DateConsensusScreen` ou `CoupleHomeScreen` isoladas — só o caminho
  principal, via `App.test.tsx`. Vale voltar aqui quando o fluxo estiver sendo usado de verdade.
- **Confirmado pelo fundador, no terminal real dele, em 2026-11-05:** `npm install` (507 pacotes), **15
  testes passando** (`session.test.ts` 3, `api.test.ts` 4, `App.test.tsx` 8), `npm run build` (tsc + vite) sem
  erro. A IA roda num sandbox isolado (sem `sudo`, sem ver o sistema real) — por isso não conseguiu rodar isso
  sozinha; só alcança serviços de rede expostos, como o PostgreSQL. Ver seção 19 do `CLAUDE.md` para o
  detalhe dessa descoberta.

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

**Etapa 7 — as duas datas do casal, por consenso (concluída, P3/RN-27):**
- `relationships/internal/CouplePairing.java` ganhou `proposeDates`/`acceptDates`: um propõe, o outro aceita
  (nunca quem propôs) — e qualquer proposta nova (inclusive uma contraproposta) **reabre o consenso**,
  descartando uma aceitação anterior. Guardas simples (`IllegalStateException`) impedem propor/aceitar fora de
  `PENDING`, aceitar sem proposta, ou aceitar a própria proposta — são erros de uso, não de regra de negócio
  (por isso não ganharam uma exceção própria, diferente da `DatesNotAcceptedException`).
- `relationships/DatesNotAcceptedException.java`: para quando algo exigir as datas aceitas e elas ainda não
  estiverem (a Etapa 8 vai reusar isso na hora de "firmar").
- `relationships/CouplePairingService.java`/`internal/CouplePairingServiceImpl.java` ganharam
  `proposeDates`/`acceptDates`/`areDatesAccepted`/`requireDatesAccepted`.
- `db/migration/V10__add_dates_to_pairings.sql`: acrescenta a `couple_pairings` as colunas da negociação
  (quem propôs, as duas datas propostas, quando foi aceito). As datas **finais** do casal só existem em
  `couples` depois que ele nascer (Etapa 8) — aqui é só a negociação.

**Etapa 9 — "o que levar": só a estrutura (concluída, P4/RN-09):**
- `shared/Scope.java`: os 4 "lugares" onde algo pode morar (`PERSONAL`, `ARCHIVED`, `MEMBER_SIDE`,
  `COUPLE_CENTER`) — decisão do fundador: mora em `shared`, não em `relationships`, porque fotos (Semana 4) e
  memórias (Semana 5) também vão precisar dele. Tem um método, `isVisibleToPartner()` (RN-09/RN-12a).
- `shared/Scopeable.java`: o contrato que uma entidade real (futura) vai implementar para "ter um Scope".
- Nenhuma tabela, nenhum endpoint — só a estrutura, testada com um objeto fictício (`ScopeTest`).

**Etapa 10 — pausar, retomar, sair (concluída, RN-13 a RN-18):**
- **Refatoração:** o enum `State` que morava dentro de `Couple` virou `relationships/CoupleState.java`, um
  tipo próprio no nível do módulo — mesmo padrão que `PairingState` já usava para `CouplePairing`. Motivo:
  `IllegalCoupleTransitionException` (nova) precisa citar o estado num erro público, e isso só é possível se o
  tipo não estiver escondido dentro de `internal` (regra do ArchUnit).
- `relationships/internal/Couple.java` ganhou `pause`/`resume`/`end`: só valem a partir do estado certo
  (`ACTIVE`→`PAUSED`, `PAUSED`→`ACTIVE`, e de qualquer um dos dois para `ENDED`, que é final).
- `relationships/CoupleService.java` (API pública) + `internal/CoupleServiceImpl.java`: `find`/`pause`/
  `resume`/`leave`. `leave` é a peça mais específica do Duo aqui: no MVP (RN-07, 2 membros), uma pessoa sair
  **termina o vínculo inteiro** — por isso busca todo mundo ainda "dentro" do casal
  (`findByCoupleIdAndLeftAtIsNull`) e marca a saída de todos de uma vez, não só de quem pediu.
- `relationships/CoupleNotFoundException.java`: mesma ideia do `PairingNotFoundException` — só membros atuais
  enxergam o casal (RN-12); depois que o vínculo termina, ninguém mais "encontra" esse casal por aqui.
- Testes novos: `CoupleTest` (a máquina de estados pura) e `CoupleServiceImplTest` (contra o `duo_test` real,
  incluindo "depois de sair, nenhum dos dois vê mais o casal").

**Etapa 8 — firmar o vínculo e nascer o `Couple` (concluída, RN-06/RN-08):**
- **Mudança de plano, decidida pelo fundador nesta etapa:** o vínculo firma **primeiro**; história e datas só
  existem depois, já dentro do Couple Universe nascido (antes era o contrário). Isso simplificou o `confirm`
  (não precisa mais checar história/data antes de firmar) e mudou a trava de `proposeDates`/`saveStoryDraft`
  de "enquanto `PENDING`" para "só depois de `CONFIRMED`" — ver `docs/epico-2-vinculo.md` para o fluxo novo.
- `relationships/internal/CouplePairing.java` ganhou `recordConfirmation`/`isFullyConfirmed` (cada pessoa
  confirma a sua parte; só quando as duas confirmaram é que o `Couple` nasce) e um `requireConfirmed()`
  (visível ao pacote) que `proposeDates`/`acceptDates` e o `saveStoryDraft` do serviço passaram a usar.
- `relationships/internal/Couple.java` ganhou `recordDates(...)`: o `Couple` nasce vazio (sem data de
  conhecer/namorar); essas datas só chegam depois, quando as duas são aceitas no `CouplePairing` (RN-28).
- `relationships/internal/CouplePairingRepository.java` ganhou `findByIdForUpdate`, com
  `@Lock(PESSIMISTIC_WRITE)` — trava a linha do pareamento até a transação terminar, mesmo truque do
  `RefreshTokenRepository`. **Por quê:** sem isso, as duas confirmações do mesmo pareamento (uma de cada
  pessoa) poderiam ler o banco "ao mesmo tempo" e uma delas se perderia (um "lost update" clássico) — foi
  provado com um teste (abaixo) que falha, de forma consistente, sem a trava.
- `relationships/internal/CoupleFactory.java` (novo, `@Component` próprio — precisa ser uma classe separada
  porque o Spring só aplica `@Transactional` através de um proxy em volta do *bean*, e uma chamada de um
  método para outro **dentro da mesma classe** não passa por esse proxy): cria o `Couple` + os 2
  `CoupleMember` numa transação **própria e independente** (`@Transactional(propagation = REQUIRES_NEW)`).
  Se o índice único de RN-06 recusar (a pessoa já está em outro vínculo), só essa transação de dentro desfaz
  — vira `AlreadyInRelationshipException` (novo, em `relationships`).
- `relationships/internal/CouplePairingServiceImpl.java` ganhou `confirm(pairingId, userId)`:
  trava a linha, confere se já venceu (cancela e sai, se sim), grava a confirmação, e — só quando as duas já
  confirmaram — chama o `CoupleFactory` e marca o pareamento `CONFIRMED`. Anotado com
  `@Transactional(noRollbackFor = AlreadyInRelationshipException.class)`: sem isso, se o nascimento do
  `Couple` falhar (a segunda corrida, abaixo), o Spring desfaria a transação inteira — inclusive a
  confirmação legítima que acabou de ser gravada.
- **Duas corridas, duas peças diferentes:**
  1. **Os dois membros do mesmo pareamento confirmando ao mesmo tempo** — resolvida pela trava
     (`findByIdForUpdate`) acima.
  2. **A mesma pessoa confirmando dois pareamentos diferentes ao mesmo tempo** — a trava de um pareamento não
     protege o outro (são linhas diferentes); quem impede a pessoa de ficar em dois vínculos é o índice único
     do banco (RN-06, desde a Etapa 1). A peça nova aqui não é uma trava, é a **reação correta** à recusa do
     banco: `REQUIRES_NEW` + `noRollbackFor` (acima) garantem que só o nascimento do segundo `Couple` falha —
     a confirmação da pessoa continua valendo, o pareamento "perdedor" fica com as duas confirmações
     gravadas mas sem `Couple` nenhum (um limbo raro, aceitável para o MVP).
- `relationships/internal/CouplePairingConcurrencyTest.java` (novo, **primeiro teste do tipo no projeto**):
  prova as duas corridas acima com threads de verdade (`ExecutorService` + `CountDownLatch`, sincronizadas
  para disparar "ao mesmo tempo"). Usa `@SpringBootTest` em vez do `@DataJpaTest` do resto do módulo: um
  objeto criado com `new CouplePairingServiceImpl(...)` nunca passa pelo proxy que o Spring usa para aplicar
  `@Transactional`/`@Lock` — fora do Spring essas anotações não fazem nada. Precisa do bean de verdade,
  gerenciado pelo Spring, para a trava e a transação separada funcionarem de verdade. Sem a transação de
  teste que os outros têm (que desfaz tudo sozinha no final), cada teste limpa manualmente o que criou.
  **Verificado à mão nesta sessão:** tirando a trava de propósito, o primeiro teste falha de forma
  consistente (5 de 5 execuções) — prova de que o teste realmente pega o problema, não só que "não dá erro".
- 18 testes novos (entre a entidade, o serviço e a concorrência), 146 no total. `./gradlew build` verde.

**Etapa 13 — exposição HTTP (concluída):**
- `shared/DuoHttpException.java` (novo): classe-base para exceções de negócio que viram resposta HTTP direto
  — cada exceção já sabe o próprio status (`httpStatus()`). Criada porque a primeira tentativa (o
  `GlobalExceptionHandler` importando cada exceção de `relationships`/`users` direto) criou um **ciclo de
  módulos** (`shared` dependendo de volta de quem já depende de `shared`) — o `ArchitectureTest` pegou isso.
  Com a base em `shared`, a dependência só vai num sentido: os módulos dependem de `shared`, nunca o
  contrário. Todas as exceções de negócio do projeto (`PairingNotFoundException`, `CoupleNotFoundException`,
  `HandleTakenException`, `RateLimitExceededException` etc.) agora estendem essa classe em vez de
  `RuntimeException` direto.
- `shared/GlobalExceptionHandler.java` (novo, `@RestControllerAdvice`): um método só, que pega qualquer
  `DuoHttpException` e devolve um `ProblemDetail` (formato de erro HTTP padronizado, já incluso no Spring —
  não é dependência nova) com o status que a própria exceção definiu.
- `relationships/IllegalPairingOperationException.java` (novo): peguei dois `IllegalStateException` que
  existiam dentro de `CouplePairing` (ex.: tentar escrever a história antes de firmar) e troquei por essa
  exceção própria — sem isso, o `GlobalExceptionHandler` não sabia traduzi-los e a API devolvia 500 em vez
  de 409.
- `shared/SecurityConfig.java`: o parâmetro do `@Bean securityFilterChain` trocou de
  `GoogleLoginSuccessHandler` (tipo concreto, de `auth`) para `AuthenticationSuccessHandler` (a interface do
  próprio Spring Security) — mesma causa raiz do ciclo acima: `shared` dependia de `auth`, que depende de
  `users`, que (com o `DuoHttpException`) passou a depender de `shared`. Dependendo da interface, o Spring
  ainda encontra e injeta o mesmo bean, mas o `ArchUnit` não vê mais a ligação `shared → auth`.
- `relationships/CouplePairingController.java` e `relationships/CoupleController.java` (novos): só chamam o
  `Service` — nunca repositório direto (regra verificada pelo `ArchitectureTest`). A pessoa autenticada vem
  sempre do token (`@AuthenticationPrincipal Jwt`), nunca de um id do pedido (RN-20).
- `users/UserController.java` (novo): só `PUT /api/me/handle` — a Etapa 14 (`HandleSetupScreen`) usa isso.
- `relationships/CouplePairingService.java`/`CoupleService.java` ganharam `findPendingForUser`/
  `findActiveForUser` — sem isso, o app não teria como saber, ao abrir, se a pessoa tem um convite esperando
  ou um casal ativo (não dá pra perguntar "me mostra o pareamento X" sem saber o id de X primeiro).
- `relationships/PairingSummary.java` ganhou o campo `coupleId` — faltava, e o frontend precisa saber pra
  onde navegar assim que o vínculo firma.
- `relationships/RelationshipsHttpTest.java` (novo, testes de ponta a ponta via `MockMvc`): o fluxo inteiro
  (convidar → firmar → escrever história → ver o casal) passando pelos controllers de verdade, mais os
  testes de acesso cruzado (RN-12/RN-20: quem não participa nunca enxerga, sempre 404). **Sem
  `@Transactional` de propósito** (diferente do `SessionFlowTest`) — mesma razão do `CouplePairingConcurrencyTest`
  (Etapa 8): o `CoupleFactory` roda numa transação própria (`REQUIRES_NEW`), que usa uma conexão separada e
  não enxergaria usuários ainda não commitados por uma transação de teste em volta de tudo. A limpeza manual
  no `@AfterEach` usa um `TransactionTemplate` explícito, porque `@Transactional` posto direto num método de
  `@AfterEach` não é interceptado pelo Spring (esse mecanismo só olha o método de `@Test`).
- 7 testes novos, 151 no total. `./gradlew build` verde.

**Etapa 11 — a matriz de permissões por estado (concluída, RN-11/12/12a/13a):**
- `relationships/CouplePermissions.java`: 4 perguntas puras sobre um `CoupleState` —
  `canViewCoupleCenter`/`canPostToCoupleCenter`/`canPostToOwnSide`/`canUsePersonal` — exatamente a tabela do
  `docs/epico-2-vinculo.md`. `canUsePersonal` é a RN-13a (decidida em 2026-11-05): só `true` durante `PAUSED`.
  Ainda não é chamada por ninguém "de verdade" — é a peça que fotos/memórias vão consultar mais adiante, para
  não reescrever essa tabela em cada módulo.

**Etapa 12 — limpeza de rascunhos (concluída, RN-29):**
- **O problema que faltava resolver:** desde a Etapa 2, `CouplePairing.cancel()` só trocava o estado para
  `CANCELLED` — não apagava nada. Isso já bastava para o pareamento em si, mas duas etapas depois adicionaram
  "rascunhos" presos a um pareamento: a Etapa 6 (história, numa **tabela separada**, `couple_pairing_stories`)
  e a Etapa 7 (proposta de datas, em **colunas da própria** `couple_pairings`). RN-29 diz que cancelar, recusar
  ou vencer apaga os dois — e isso não estava acontecendo.
- `relationships/internal/CouplePairing.java`: `cancel()` agora também chama um método novo e privado,
  `eraseDateProposal()`, que zera os 4 campos da negociação de data (quem propôs, as duas datas propostas,
  quando foi aceito). Como são colunas da própria entidade, a própria entidade consegue limpar sozinha.
- `relationships/internal/CouplePairingStoryRepository.java`: ganhou `deleteAllByPairingId(Long pairingId)` —
  um **método derivado** do Spring Data: não escrevemos nenhuma query, o nome do método já diz o que fazer
  ("apague tudo onde `pairing_id` for este"), e o Spring Data JPA gera o SQL sozinho a partir do nome.
- `relationships/internal/CouplePairingServiceImpl.java`: como a história mora numa tabela separada, só a
  entidade `CouplePairing` não alcança ela — por isso criamos `cancelAndEraseDrafts(pairing, when)`, que chama
  `pairing.cancel(when)` e, na sequência, `stories.deleteAllByPairingId(...)`. Os três lugares do serviço que
  cancelavam um pareamento direto (`cancel` público, a expiração "lazy" dentro de `find`, e a mesma expiração
  dentro de `redeemByLink`) passaram a usar esse método novo, para nenhum deles esquecer de apagar a história.
- **As outras duas decisões da Etapa 12 não pediram código:** a limpeza continua "lazy" (igual às Etapas 2/3 —
  só confere quando alguém olha o pareamento, sem tarefa agendada rodando sozinha); e mais de um
  `CouplePairing` `PENDING` por pessoa ao mesmo tempo já era permitido (nunca existiu um índice único
  bloqueando isso) — só ganhou um teste de regressão para deixar a intenção registrada, em vez de ser um
  comportamento "por acaso".
- Testes novos (4): em `CouplePairingTest` (puro, sem banco), cancelar apaga a proposta de datas. Em
  `CouplePairingServiceImplTest` (contra o `duo_test` real): cancelar um pareamento com histórias dos dois
  lados apaga as duas; um pareamento que expira sozinho (via `find()`) também apaga a história; e a mesma
  pessoa consegue ter dois pareamentos `PENDING` ao mesmo tempo, com convidados diferentes.
