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
  a tabela `users`. `V2__add_google_sub_to_users.sql` acrescenta `google_sub` (único, obrigatório; DEC-030). Cada migration nova vira um arquivo `V2__...`, `V3__...`, nunca editamos um já aplicado.

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
