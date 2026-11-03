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

Hoje só existe o **esqueleto**: o backend sobe, responde "estou vivo" e exige login para qualquer outra
rota; o frontend mostra uma tela única. Ainda não há login real, nem casal, nem fotos — isso vem com o
Épico 1 (seção 12 do `CLAUDE.md`).

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
- `shared/SecurityConfig.java`: a regra de acesso de hoje — **tudo exige login, exceto `/actuator/health`**
  (RN-20). Quando o Épico 1 trouxer o login de verdade (código por e-mail, depois passkey), este arquivo
  muda para usar esse mecanismo em vez do login básico provisório que está aqui agora.
- `auth/package-info.java` e `users/package-info.java`: ainda não têm código, só uma "placa" (Javadoc do
  pacote) explicando o que cada módulo vai guardar. `auth` vai cuidar de login/sessão; `users` vai cuidar
  da conta e do Personal Universe. Eles ainda não se falam porque ainda não têm nada dentro.

**Configuração (`src/main/resources/`):**
- `application.properties`: onde o backend acha o banco de dados (endereço, usuário, senha — a senha vem
  de uma variável de ambiente, nunca do arquivo, regra 11) e ajustes finos (ex.: `ddl-auto=validate` diz ao
  Hibernate "só confira se o banco bate com as entidades, não crie tabelas sozinho — isso é trabalho do
  Flyway").
- `db/migration/V1__create_users.sql`: a primeira "migration" do Flyway — um script SQL versionado que cria
  a tabela `users`. Cada migration nova vira um arquivo `V2__...`, `V3__...`, nunca editamos um já aplicado.

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
`V1`). Tem um construtor protegido vazio (exigido pelo Hibernate) e um construtor público com os campos
obrigatórios; `createdAt` é preenchido sozinho (`@CreationTimestamp`), nunca pelo código que cria o `User`.

**`users/internal/UserRepository.java`:** a interface Spring Data JPA que dá acesso ao banco para `User`
(`save`, `findById`, etc., herdados de `JpaRepository`) mais uma busca própria por e-mail sem diferenciar
maiúsculas (`findByEmailIgnoreCase`), usada pelo futuro login.

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
- `src/App.tsx`: o componente principal. Hoje só mostra o título "Duo Universe"; vai crescer conforme as
  telas do Épico 1 (criar conta, entrar) forem chegando.
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
