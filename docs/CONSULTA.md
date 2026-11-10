# Consulta

> Material de **consulta**, não de leitura obrigatória. Quem precisa do contexto do projeto lê o `CLAUDE.md`;
> quem precisa saber onde está cada arquivo lê o `docs/MAPA.md`. Aqui ficam quatro coisas que se procuram
> quando surge a dúvida:
>
> 1. **Glossário técnico** — "vi esse nome/termo, o que é?"
> 2. **O vínculo (Épico 2)** — o fluxo e a matriz de estados, para quem mexe no módulo `relationships`.
> 3. **Requisitos (v5)** — a lista de RF com o estado de cada um.
> 4. **Ideias e pendências** — o que está guardado fora do MVP e o que espera decisão do fundador.
>
> As regras de negócio (RN) e as decisões (DEC) moram no `CLAUDE.md`. Última conferência: 2026-11-10.
> Termo técnico novo que aparecer no código entra na seção 1 (regra do "Modo acompanhamento").

---

## 1. Glossário técnico

### Dicionário rápido: classe → o que ela é

| Classe | O que ela é, em português simples |
| --- | --- |
| `User` | A pessoa: conta, e-mail, nome, DuoName (`handle`), perfil. |
| `UserService` | A porta de entrada do módulo `users`: criar, achar e atualizar contas e perfil. |
| `Couple` | O Couple Universe: estado (`ACTIVE`/`PAUSED`/`ENDED`) e as datas do casal. |
| `CoupleMember` | O elo pessoa ↔ casal: "Ana está neste casal desde tal dia". |
| `CouplePairing` | O convite/momento do vínculo, antes de virar um `Couple`; carrega a máquina de estados. |
| `CouplePairingStory` | A história de uma pessoa (4 passos) dentro de um pareamento. |
| `CouplePairingService` | Quem toca o convite do início ao fim: convidar, aceitar, firmar, história, datas. |
| `CoupleFactory` | Cria o `Couple` numa transação própria, para a corrida de "um vínculo por pessoa" desfazer só o nascimento. |
| `CouplePermissions` | A matriz "o que cada estado do casal permite", em perguntas puras. |
| `RefreshTokenService` | Quem decide se você continua logado: emite, renova e revoga o token de 30 dias. |
| `AccessTokenService` | Quem fabrica e confere o crachá de 15 minutos (JWT) usado em cada chamada. |
| `RateLimiter` | O freio de mão contra quem tenta mil vezes por minuto (RA-08). |
| `SecurityConfig` | A portaria da API: decide quais rotas pedem login. |
| `GlobalExceptionHandler` | Traduz exceções em respostas HTTP, sempre com mensagem fixa. |
| `Access` / `ProfileAccessService` | A regra central "o que este visitante pode ver desta pessoa?" (camada social). |
| `PhotoAccessRule` | Decide quem pode ver uma foto. |
| `ImageSniffer` | Descobre o tipo real da imagem pelos primeiros bytes (RN-23). |
| `MediaStorageProvider` | O contrato de "onde o arquivo fica"; hoje pasta local, amanhã R2. |
| `*Exception` | Um motivo específico de erro, com a regra de negócio por trás. |

### Build e projeto

- **Gradle (Kotlin DSL) e wrapper (`gradlew`):** compila, testa e empacota o backend (`backend/build.gradle.kts`).
  O wrapper baixa a versão exata do Gradle, sem depender do que está instalado.
- **Toolchain (Java):** diz ao Gradle qual Java usar (21), mesmo que o do sistema seja outro; se faltar, o plugin
  `foojay` baixa sozinho.
- **Starter (Spring Boot):** pacote de dependências prontas para uma função (web, segurança, banco...).
- **Spotless:** corrige formatação e falha o build se algo não bater. No Duo é mínimo: imports e espaços.

### Spring Boot

- **Spring Boot:** sobe o servidor HTTP, liga ao banco e organiza tudo por configuração (`DuoBackendApplication`).
- **Bean:** objeto que o Spring cria e gerencia (em vez de `new`). Ex.: `securityFilterChain` em `SecurityConfig`.
- **`@Configuration`:** a classe define beans.
- **Actuator:** expõe dados operacionais; no Duo só `/actuator/health`, a única rota pública (RN-20).
- **`@ConfigurationProperties` / `@Value`:** lêem `application.properties` (ex.: `duo.media.max-photos`).

### Segurança

- **`SecurityFilterChain`:** quais rotas pedem login e como (`SecurityConfig`).
- **CSRF:** ataque em que outro site faz o navegador enviar uma requisição usando sua sessão. Está desligado no
  Spring porque a API usa token Bearer e o cookie de renovação é `SameSite=Strict`, restrito a `/api/auth`.
- **JWT:** texto assinado com o id e o vencimento; o servidor confere a assinatura sem consultar o banco
  (token de acesso de 15 min, `JwtConfig`, segredo `DUO_JWT_SECRET`).
- **Token de acesso x de renovação:** o de acesso é curto e vai em cada chamada; o de renovação (30 dias) fica num
  cookie e só serve para pedir um novo. Se o de acesso vazar, o estrago acaba em minutos.
- **Bearer:** `Authorization: Bearer <token>`. O frontend o envia em toda chamada a `/api/...`
  (`shared/session/token.ts`).
- **`oauth2-resource-server`:** módulo do Spring Security que valida o Bearer (assinatura, validade, emissor).
- **Cookie `HttpOnly` / `Secure` / `SameSite=Strict`:** o JavaScript não lê; só vai por HTTPS (e `localhost`);
  não vai quando a requisição nasce em outro site. Em desenvolvimento sem HTTPS: `DUO_COOKIE_SECURE=false`.
- **Rotação e família de tokens:** cada uso do token de renovação troca por um novo e invalida o antigo (RA-05).
  Se um token **já usado** reaparece, alguém o copiou: toda a família (`family_id`, um aparelho) é encerrada.
- **Hash (SHA-256):** impressão digital irreversível. O banco guarda o hash do token e do código de convite, não
  o valor: se o banco vazar, nada serve.
- **Lock pessimista (`PESSIMISTIC_WRITE`):** trava a linha até o fim da transação, para duas requisições não usarem
  o mesmo token ou confirmarem o mesmo pareamento ao mesmo tempo.
- **Anti-enumeração:** nunca deixar descobrir, por tentativa e erro, quem tem conta ou está em vínculo. O convite
  por DuoName devolve a **mesma** `HandleNotInvitableException` para handle inexistente, o próprio, já vinculado,
  bloqueado ou oculto.
- **404 igual para "não existe" e "não é para você":** responder de forma idêntica para não revelar que algo
  existe (ou que você foi bloqueado). Ex.: `ProfileNotFoundException`, `MediaNotFoundException`.
- **Rate limiting e janela de tempo:** contar tentativas de "alguém" num período e bloquear acima do máximo.
  Tabela `rate_limit_attempts`, uma linha por `(scope, attempt_key)`. Passada a janela, o contador volta a zero.
- **Acesso cruzado (teste):** A tenta ver o que é de B; quem não pode ver recebe 404 mesmo sabendo o id.
- **Assinatura de arquivo (magic bytes):** os primeiros bytes revelam o formato real (PNG começa com
  `89 50 4E 47`); `ImageSniffer` não confia no nome nem no tipo declarado.
- **Caminho que escapa (path traversal):** chave como `../../etc/senha` tentando sair da pasta; o
  `LocalMediaStorageProvider` só aceita caracteres seguros e confere que o caminho final fica dentro da pasta.

### Banco de dados

- **Flyway / migration:** o schema é uma sequência de arquivos SQL numerados (`V1__`, `V2__`...), aplicados em
  ordem e **nunca editados** depois de aplicados.
- **JPA / Hibernate:** traduz objetos Java em linhas de tabela. `ddl-auto=validate`: o Hibernate só confere se as
  entidades batem com o banco; quem manda no schema é o Flyway.
- **`open-in-view=false`:** a conexão com o banco não fica aberta durante toda a resposta HTTP.
- **Índice único parcial (`CREATE UNIQUE INDEX ... WHERE ...`):** só vale para as linhas que batem com o `WHERE`.
  `couple_members_active_person_unique` proíbe dois vínculos com o mesmo `user_id` **enquanto** `left_at` for
  nulo (RN-06).
- **`@Modifying`:** marca `update`/`delete` escrito à mão; não atualiza objetos já em memória, por isso usamos
  `flushAutomatically` e `clearAutomatically`.
- **`Slice` x `Page`:** `Page` faz um `COUNT` extra; `Slice` só descobre se há próxima página. A busca usa `Slice`.
- **Escape no `LIKE`:** `%` e `_` são curingas; como `_` é válido num DuoName, o termo buscado os escapa.
- **`CHAR` x `VARCHAR`:** `CHAR(64)` tem tamanho fixo (bom para hash hexadecimal). O Hibernate valida o tipo.
- **Race condition:** duas requisições disputando o mesmo recurso. O índice único decide e o perdedor reage.
- **Transação *rollback-only*:** se uma exceção passa por dentro de uma transação, o Spring a marca "só reverter"
  mesmo que você a capture. Por isso `follow` e `reportPhoto` não abrem transação externa: repetição bate no
  índice único e precisa ser aceita em silêncio.
- **`REQUIRES_NEW` / `noRollbackFor`:** transação própria, que desfaz só a si mesma. Usada no `CoupleFactory`.
- **Idempotente:** repetir a ação dá o mesmo resultado, sem erro (seguir quem já segue, denunciar duas vezes).
- **Moderação por `hidden_at`:** carimbo na linha (`users`, `media_assets`) que faz o item sumir sem apagá-lo.
- **Expiração "lazy":** sem job agendado; o código confere a validade quando alguém acessa (`find` marca o convite
  vencido na hora, RN-29).

### Arquitetura e módulos

- **Monólito modular:** um programa só, com módulos de fronteira clara (DEC-020): `com.duo.auth`, `users`...
- **Convenção `internal` e camadas:** a API pública do módulo fica na raiz (`com.duo.users.UserService`); o que só o
  módulo usa fica em `internal/` dividido em `model/`, `repository/` e `service/`. `controller/`, `dto/` e
  `exception/` ficam na raiz.
- **DTO:** objeto que viaja para fora (ex.: `UserSummary`), nunca a entidade do banco.
- **Package-private:** sem `public`/`private`/`protected`, só o mesmo pacote enxerga. Por isso um teste fica no
  mesmo pacote da classe que testa quando ela não é `public`.
- **ArchUnit:** testa regras de organização (ex.: "controller não acessa o banco"). `ArchitectureTest`.
- **Dublê de teste:** objeto escrito à mão que faz o papel de uma dependência, sem Mockito. Útil quando a
  dependência real está em outro módulo e é `internal`.
- **Feature flag:** chave de configuração que liga/desliga um pedaço do sistema. O social está atrás de
  `duo.features.social.enabled` (back) e `VITE_FEATURE_SOCIAL` (front).

### Testes

- **`@SpringBootTest` / `@DataJpaTest`:** o primeiro liga o app inteiro; o segundo só a camada JPA. Como não há
  banco em memória, usamos `@AutoConfigureTestDatabase(replace = NONE)` para usar o Postgres real (`duo_test`).
  **Pegadinha do Spring Boot 4.1:** essas anotações mudaram de pacote (agora
  `org.springframework.boot.data.jpa.test.autoconfigure` e `...jdbc.test.autoconfigure`); confira no `.jar`
  antes de copiar tutorial antigo.
- **`@ActiveProfiles("test")`:** usa `application-test.properties` (banco `duo_test`).
- **`MockMvc`:** simula requisições HTTP contra os controllers, sem servidor real.
- **Teste de concorrência:** threads reais (`ExecutorService` + `CountDownLatch`) disparando a corrida; precisa de
  `@SpringBootTest`, porque um objeto criado com `new` não passa pelo proxy que aplica `@Transactional`/`@Lock`.
- **Testcontainers:** estava no plano (DEC-018), mas está **fora por ora**: os testes usam o Postgres local.

### Frontend

- **Vite:** builda e serve o frontend em desenvolvimento. **PWA:** site que se instala no celular
  (`vite-plugin-pwa`); **manifest** dá nome e ícones; **service worker** roda em segundo plano e serve arquivos do
  cache, e **Workbox** gera esse script. No Duo cacheia só estáticos, **nunca** a API.
- **ESLint / Prettier / Vitest / jsdom:** acha problemas / formata / testa / simula o navegador em memória.
- **Alias `@/`:** aponta para `src/`, evitando `../../..` nos imports.
- **Feature (pasta):** um assunto com suas telas e seu `api.ts`; uma feature não importa de outra, só de `shared/`.
- **CSS Modules:** `Componente.module.css` tem escopo só do componente. O que várias telas usam fica global em
  `shared/styles/`.
- **Design token:** valor de design com nome (`--color-primary`) em vez do número solto repetido; todos em
  `shared/styles/tokens.css`. **`color-mix()`** calcula uma cor a partir de outras.
- **DuoName / Duollowers / Duollowing:** nomes na tela para `handle`, quem te segue e quem você segue
  (`shared/lib/labels.ts`; trocar a palavra é uma linha).
- **Rota de layout (`Outlet`):** rota sem página própria que desenha a moldura. `AppLayout` envolve as telas de
  quem está logado.
- **Contexto do React:** reparte um dado com várias telas sem passar de pai para filho. `MeContext` reparte o seu
  perfil.
- **Blob URL:** endereço `blob:...` de dados na memória. `<img src>` não envia o token, então o app baixa a foto
  com o token e a mostra por um blob URL.
- **Debounce:** espera a pessoa parar de digitar (300 ms) antes de buscar.
- **Bottom sheet:** modal que sobe de baixo, com alça (`BottomSheet`). **Pointer Events** unificam toque, mouse e
  caneta e fazem a alça funcionar igual no celular e no computador.
- **Scroll snap:** a rolagem horizontal "encaixa" em cada item (faixa de navegação do celular).
- **Área segura (`env(safe-area-inset-bottom)`):** espaço que o iPhone reserva para a barra de gestos.
- **`box-sizing: border-box`:** `width` e `min-width` passam a incluir o espaçamento interno.
- **Multipart:** formato HTTP para enviar arquivo junto com campos de texto (`POST /api/me/photos`).
- **StrictMode (React):** em desenvolvimento executa efeitos duas vezes. Como o token de renovação é de uso único,
  o frontend divide uma única chamada de refresh entre pedidos simultâneos, senão leria isso como roubo.

### PostgreSQL local

- **Role:** o "usuário" do próprio Postgres, separado dos usuários do sistema. Precisa de `LOGIN` para conectar.
- **Owner:** a role dona do banco; `duo_app` é dona de `duo` e de `duo_test`.
- **`pg_hba.conf`:** lista, por origem + banco + role, o método de autenticação. O Spring conecta por rede
  (`localhost:5432`), então precisa de uma regra `host` que exija senha (`scram-sha-256`).
- **`\password` (no `psql`):** pede a senha duas vezes, escondida, e a envia já criptografada; evita deixar a
  senha em texto no histórico do shell.
- **Variável de ambiente como segredo:** `DUO_APP_DB_PASSWORD` etc. são lidas do ambiente, nunca do repositório
  (regra 11).

### CI (GitHub Actions)

- **CI:** roda build e testes a cada mudança (`.github/workflows/ci.yml`). **Job:** grupo de passos numa máquina
  própria (`backend` e `frontend` em paralelo). **Service container:** container extra só durante o job (o
  Postgres descartável). **Concurrency group:** cancela a execução anterior da mesma branch.

---

## 2. O vínculo (Épico 2)

Duas pessoas já cadastradas criam juntas o Couple Universe. Não é um pedido de namoro (o casal já existe na vida
real): é o momento em que decidem guardar a história no Duo. Implementado no módulo `relationships`.

### Fluxo

1. A inicia e escolhe B por **DuoName exato** (padrão) ou gera um **link com código** (alternativa; vale 7 dias,
   uso único). Nasce um `CouplePairing` `PENDING`.
2. B aceita o convite no Duo ou entra pelo link (já logado com Google).
3. Cada um **firma**. Só com os dois confirmados o `Couple` nasce **vazio** e `ACTIVE` (RN-08).
4. Já dentro do Couple Universe, cada um escreve a sua história em 4 passos, tudo obrigatório, com rascunho:
   (1) como nos conhecemos, (2) o tempo antes de namorarmos, (3) o que eu pensava, (4) o que penso agora.
5. Os dois combinam as duas datas (se conheceram; começaram a namorar) por **propor e aceitar** (RN-27); aceitas,
   são copiadas para o `Couple`.
6. As histórias são reveladas **ao mesmo tempo**, quando as duas estão completas (RN-26).

> A ordem é "firma primeiro, história depois" (decisão de 2026-11-05): a história é "sobre nós" e só faz sentido
> depois que o vínculo existe. Um pareamento `CONFIRMED` não cancela mais.

### Estados

```
Pairing:   PENDING ──(os dois firmam)──► CONFIRMED  → cria o Couple
              └──(recusa / expira / cancela)──► CANCELLED

Couple:    ACTIVE ⇄ PAUSED
              │         │
              └────►  ENDED   (final)
```

| Ação | ACTIVE | PAUSED | ENDED |
| --- | --- | --- | --- |
| Ver o centro do casal | sim | sim | não (hoje; a visão do ex-membro, RN-17a, ainda não existe) |
| Postar no centro / editar | sim | não | não |
| Postar no próprio lado | sim | não | não |
| Usar o Personal (ler e postar) | não | sim (RN-13a) | — (a pessoa volta a ser solteira) |
| Pausar | — | não | não |
| Retomar | — | sim | não |
| Sair | sim | sim | — |

No MVP de 2 membros, **sair termina o vínculo para os dois** (`leave`). O centro do casal nunca é apagado. A tabela
reflete `CouplePermissions` (4 perguntas puras), que ainda não é consultada por nenhuma tela ou endpoint.

### Regras específicas deste épico

- **RN-25:** a história é obrigatória; cada passo precisa de texto não vazio depois de remover espaços (sem mínimo
  de caracteres, decidido em 2026-11-05).
- **RN-26:** as histórias só ficam visíveis uma à outra quando ambas estão completas.
- **RN-27:** as duas datas são únicas por casal; um propõe, o outro aceita (nunca quem propôs); nova proposta
  reabre o consenso.
- **RN-28:** o contador de tempo juntos usa a data de início do namoro.
- **RN-29:** pareamento expirado, recusado ou cancelado apaga rascunhos e proposta de datas.
- **RN-30:** o convite de vínculo só aceita DuoName **exato**, com limite de tentativas (RA-08). A busca por nome
  (DEC-034) é outra coisa e está congelada.
- **RN-13a:** durante a `PAUSED`, a pessoa volta a ver e a editar o que é dela no Personal (revisa a DEC-002).

### Casos de borda

- Mais de um `CouplePairing` `PENDING` por pessoa ao mesmo tempo é **permitido** (deixa a porta aberta ao "pedido
  de namoro" futuro). RN-06 só é verificada ao **firmar**: se a pessoa confirmar dois, o primeiro cria o `Couple` e o
  segundo esbarra no índice único e falha com `AlreadyInRelationshipException` (a confirmação fica registrada).
- Os dois se chamam ao mesmo tempo; alguém já vinculado recebe um código; alguém sai enquanto o outro edita;
  transição ilegal (`ENDED` → `ACTIVE`); os dois discordam das datas (a tela precisa de uma saída).

### Requisitos não funcionais do vínculo

- **Concorrência (RN-06):** índice único parcial + validação transacional + teste com duas confirmações
  simultâneas. Duas corridas, duas soluções: dois membros confirmando o mesmo pareamento (lock pessimista);
  a mesma pessoa confirmando dois pareamentos (índice único + `CoupleFactory` em `REQUIRES_NEW`).
- **Autorização (RN-12, RN-20):** o casal é sempre derivado do usuário autenticado, nunca de um `coupleId` enviado
  pelo frontend. Teste de acesso cruzado (C tentando ver o casal de A).
- **Privacidade:** histórias, códigos e rascunhos nunca em logs (RA-09).
- **Celebração em tempo real (P6, decidida, ainda não feita):** avisar os dois "ao vivo" quando o `Couple` nasce
  exige presença online e um canal (WebSocket, SSE ou polling, a decidir). Se não estiverem juntos online, a
  celebração acontece quando estiverem; o vínculo não fica bloqueado esperando.
- **"O que levar" (RN-09):** a estrutura (`Scope`) existe; a tela e o fluxo estão **fora do MVP** (v5).

---

## 3. Requisitos (v5, foco no casal)

> Prioridade: **M** = deve ter no MVP, **S** = deveria ter. Estados em 2026-11-09: `feito`, `próximo`,
> `aguarda decisão`, `—`. A camada social (busca, Duollowers, perfil de terceiros) está **congelada**
> (RF-A11), não apagada.

**Dentro do MVP:** conta e sessão (Google); perfil pessoal (o atual, melhorado); convite e vínculo; fotos do casal;
linha do tempo e contador; pergunta do dia; pausa, saída, lixeira, exclusão de conta e exportação; bloqueio,
denúncia e moderação mínima.
**Fora do MVP:** busca/Duollowers/perfil de terceiros (congelados), "o que levar", vídeo, álbuns, datas especiais com
lembrete, celebração em tempo real, R2 e URLs assinadas, verificação de idade por CPF (só antes de abrir ao
público), passkeys e planos pagos. Abrir o app a quem é de fora vem **depois da Fatia 5**.

### Épico A — Fundação segura (Fatia 0)

| ID | Requisito | Pri. | Estado |
| --- | --- | --- | --- |
| RF-A01 | Convite por DuoName respeita bloqueio, perfil oculto e conta excluída, com resposta indistinguível de "não existe" | M | feito |
| RF-A02 | Só um convite pendente por par de pessoas | M | feito |
| RF-A03 | Refresh recusa conta excluída ou oculta | M | próximo |
| RF-A04 | Limites de tamanho em todo texto livre e no corpo JSON (proposta: história 1.000 caracteres por passo; JSON 64 KB) | M | aguarda decisão |
| RF-A05 | Rate limit nas rotas de auth, no convite por link e no resgate | M | aguarda decisão (valores) |
| RF-A06 | O limitador não gera 500 em corrida | M | feito |
| RF-A07 | Duas abas ou resposta perdida não derrubam todas as sessões | M | aguarda decisão (desenho) |
| RF-A08 | Tratamento global de erro: nenhuma exceção prevista vira 500 | M | feito |
| RF-A09 | Limpeza agendada de tokens, tentativas, convites vencidos e rascunhos | M | aguarda decisão (conflita com a limpeza *lazy* de convites; tokens e tentativas não) |
| RF-A10 | Convite expirado aparece e some corretamente (RN-29) | M | feito |
| RF-A11 | Congelamento do social por chave de configuração | M | feito |
| RF-A12 | Refatorações pequenas: tirar `initiate` da API pública; unificar `userId(Jwt)` e o hash SHA-256 | S | próximo |

Consequência do congelamento: denunciar um perfil (`POST /api/profiles/{handle}/report`) responde 404, porque a
denúncia exige perfil "visível". Para o convite de vínculo isso precisa de decisão (RF-H01).

### Épico B — Conta e perfil pessoal

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-B01 | Entrar com Google; sair de um aparelho ou de todos (existe) | M |
| RF-B02 | DuoName, com 2 trocas por dia (existe) | M |
| RF-B03 | Perfil: nome, foto, bio até 100, profissão até 20, estado civil (existe) | M |
| RF-B04 | O servidor decodifica a imagem enviada, limita as dimensões e remove o EXIF | M |
| RF-B05 | O Personal é privado: fotos só do dono | M |
| RF-B06 | Tela do solteiro sem vínculo, com chamada para convidar ou aceitar convite | M |
| RF-B07 | Tratamento global de sessão expirada; carregamento e erro unificados | M |
| RF-B08 | Melhorias do perfil a definir pelo fundador | S |

### Épico C — Vínculo (Fatia 1)

Backend e telas existem; falta validar de verdade. **Aceitação:** o fundador e outra pessoa se vinculam em dois
aparelhos reais.

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-C01 | Convidar por DuoName exato ou por link com código de 7 dias | M |
| RF-C02 | Aceitar, recusar e cancelar convite | M |
| RF-C03 | O `Couple` só nasce com a confirmação dos dois (RN-08) | M |
| RF-C04 | Um vínculo ativo por pessoa, mesmo sob concorrência (RN-06) | M |
| RF-C05 | História em 4 passos, revelada quando as duas estão completas (RN-25/26) | M |
| RF-C06 | Datas do casal por propor e aceitar (RN-27) | M |

### Épico D — Fotos do casal (Fatia 2; aceitação: o parceiro vê a foto e um terceiro não)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-D01 | Enviar fotos para o centro do casal e para o lado de cada um | M |
| RF-D02 | Só membros atuais acessam (RN-12); no lado de cada um, só o dono posta e apaga (RN-12a) | M |
| RF-D03 | Miniatura gerada no servidor e carregamento sob demanda na grade | M |
| RF-D04 | Cota de fotos por casal (provisório: 200 fotos e 100 MB; rever na Fatia 2) | M |
| RF-D05 | Qualquer membro edita o centro; apagar manda para a lixeira (RN-11) | M |
| RF-D06 | Sem corrida na cota e sem arquivo órfão se a transação falhar | M |
| RF-D07 | Paginação por cursor na galeria | M |

### Épico E — Linha do tempo e contador (Fatia 3)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-E01 | O vínculo é o primeiro evento da linha do tempo (RN-10) | M |
| RF-E02 | Fotos e memórias em ordem cronológica, paginadas | M |
| RF-E03 | Contador de tempo juntos a partir da data de namoro acordada; sem consenso, convida a definir | M |
| RF-E04 | Memória com texto | S |

### Épico F — Pergunta do dia (Fatia 4)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-F01 | Uma pergunta por dia, de um banco curado pelo Duo (Claude propõe 100; o fundador revisa) | M |
| RF-F02 | A resposta do parceiro aparece assim que ele responder | M |
| RF-F03 | A pergunta nunca expira; contador de dias, sem marca de "atrasada" | M |
| RF-F04 | As respostas viram registros na linha do tempo (DEC-023) | S |

### Épico G — Liberdade e piso mínimo (Fatia 5; aceitação: sair não apaga nada de ninguém)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-G01 | Qualquer membro pausa e retoma (RN-13); nada é apagado | M |
| RF-G02 | Qualquer membro sai sozinho (RN-14); no MVP de 2 membros o vínculo termina para os dois | M |
| RF-G03 | Matriz de permissões por estado (RN-11/12/12a/13a) | M |
| RF-G04 | O término nunca apaga memórias (RN-17); cada ex-membro pode esconder o centro da própria visão (RN-17a) | M |
| RF-G05 | Lixeira de 30 dias com limpeza automática | M |
| RF-G06 | Exclusão em massa no centro exige o consentimento dos dois (RN-11a) | M |
| RF-G07 | Excluir a própria conta dentro do app, com aviso do que será apagado | M |
| RF-G08 | Exportar os próprios dados | M |
| RF-G09 | Sem notificação pública de pausa ou término (RN-16) | M |

### Épico H — Confiança (antes do piloto)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-H01 | Bloquear e denunciar usuário ou foto (existe); o bloqueio passa a barrar convites | M |
| RF-H02 | Papel de admin, endpoint e fila para revisar denúncias e ocultar usuário ou foto | M |
| RF-H03 | Denúncia por suspeita de menor tem prioridade | M |
| RF-H04 | Termos e política de privacidade, revisados por advogado antes de abrir | M |
| RF-H05 | Registros de acesso (data, hora, IP) por 6 meses, em sigilo | M |

### Requisitos não funcionais

| Área | Requisito (valores com \* são metas iniciais) |
| --- | --- |
| Segurança | Autorização no backend em toda operação; nenhum id do cliente é confiado; teste de acesso cruzado em todo endpoint que toca dado de duas pessoas; sem segredo em log, repositório ou frontend; mídia só pela API autorizada |
| Privacidade (LGPD) | Dado mínimo; exportação e exclusão; EXIF removido; privado por padrão |
| Desempenho \* | p95 abaixo de 500 ms; miniaturas até 60 KB; tela inicial abaixo de 3 s em 4G; sem N+1 no acesso a imagem |
| Confiabilidade | Migrations só para frente; backup diário com restauração testada (depende do deploy); sem corrida em vínculo, cota e avatar |
| Manutenibilidade | Módulos sem ciclos (ArchUnit); cobertura com JaCoCo (\*meta 70% nos serviços); CI verde obrigatório |
| Usabilidade | Mobile-first, PWA; diálogos com foco, Escape e teclado; interface em português |
| Custo | R$ 0; aviso antes de qualquer serviço que exija cartão |
| Observabilidade | Logs estruturados sem dados pessoais; métricas básicas via Actuator |
| Portabilidade | Armazenamento atrás de `MediaStorageProvider` |

### Plano por fatias

| Fatia | Conteúdo | Pronto quando |
| --- | --- | --- |
| 0 | Épico A | Testes de acesso cruzado novos passam; interface sem social |
| 1 | Épico C + RF-B06/B07 | Vínculo feito em dois aparelhos reais |
| 2 | Épico D + RF-B04 | O parceiro vê a foto e um terceiro não |
| 3 | Épico E | A linha do tempo mostra o vínculo e as fotos |
| 4 | Épico F | Os dois respondem e se veem |
| 5 | Épico G | Sair não apaga nada de ninguém |
| Antes do piloto | Épico H, deploy e backups | Denúncia revisada sem tocar no banco |
| Piloto | Fundador + 4 casais por 30 dias | 3 casais voltam sozinhos por semana |

**Definição de pronto (toda fatia):** build verde, testes passando, teste de acesso cruzado quando houver dado de
duas pessoas, changelog, `MAPA.md` e glossário atualizados, commit citando RF/RN.

---

## 4. Ideias e pendências

### Pendências que dependem do fundador

| # | Pergunta | Recomendação |
| --- | --- | --- |
| P1 | Quais são as melhorias do perfil? | O fundador lista; entram na Fatia 1 |
| P2 | O que acontece com o centro do casal quando um membro exclui a conta? | O centro fica preservado para o outro (RN-18); os dados de quem saiu voltam ou são apagados por ele |
| P3 | Cota de fotos do casal | Manter o provisório (200 fotos, 100 MB), agora por casal; rever na Fatia 2 |
| P4 | Limites de texto | História 1.000 caracteres por passo; corpo JSON 64 KB |
| P5 | Álbuns e datas especiais | Depois do piloto |
| P6 | Deploy e backups dependem de cartão (Q-14) | Resolver antes do Épico H, ou outra hospedagem |
| P7 | Quem escreve as perguntas do dia? | O Claude propõe 100; o fundador revisa |
| P8 | Fotos do Personal continuam, só privadas? | Sim (assumido) |

Também aguardam decisão: valores do rate limit (RF-A05), o desenho da correção da corrida de abas (RF-A07) e a
denúncia de perfil com o social congelado.

### Ideias guardadas (nada daqui é implementado sem decisão registrada — regra 3)

- **Universo de estrelas e planetas (2026-11-04):** em vez de linha do tempo, o casal vê o seu universo; momentos
  comuns são estrelas e marcos (início do namoro) são planetas; entrar num planeta mostra as fotos daquele dia ou
  ano. Dado e apresentação são separados: o banco guarda acontecimentos com data e marco; a tela é uma forma de
  olhá-los. A primeira versão da linha do tempo pode ser uma lista simples. Também seria o formato próprio de
  navegar por fotos e vídeos.
- **Juntar as duas histórias numa só visão, com fotos.**
- **Gesto de celebração (2026-11-04):** no nascimento do Couple Universe os dois fazem um gesto ao mesmo tempo
  (ex.: segurar um botão). Exige os dois online. Ver P6 acima.
- **Chat do casal (Fase 2):** exige tempo real, armazenamento, moderação, denúncia, bloqueio e retenção sob a LGPD.
- **"Dar um tempo da rede" (Fase 2):** interruptor da conta, diferente da pausa do vínculo; se mistura com a
  exclusão de conta.
- **Rede social de longo prazo:** Personal e Couple Universe como espaços de postar; casal privado por padrão e só
  visível a outros casais se os dois escolherem (DEC-027). Busca por nome, Duollowers, bloqueio e denúncia já
  existem no código, **congelados** na v5. Religar traz as obrigações da seção 16 do `CLAUDE.md`.
- **Jogos e perguntas entre o casal (Fase 2):** perguntas de um para o outro em momentos determinados e jogos
  interativos ("vergonha alheia", picantes). Depende do chat; toca o Cofre do casal (18+, criptografia de ponta a
  ponta) e os princípios de liberdade e maturidade (o app oferece, não arbitra).
- **"Pedido de namoro" para quem está solteiro no Duo:** hoje o vínculo assume que o casal já namora. Um pedido
  de verdade, como gesto fofo para quem se conheceu dentro do Duo, seria fluxo diferente. Depende de pessoas se
  conhecerem dentro do app. É parte do motivo de permitir mais de um `CouplePairing` `PENDING` por pessoa.
- **Home como "o seu universo", não como feed:** perfil, momentos recentes e, com casal, o banner "N meses
  juntos". Feed de outras pessoas é passo à parte (Horizonte).
- **Formatos próprios no lugar do reels** (princípio: nada de rolagem infinita com reprodução automática):
  - **Recados que se abrem depois ("abra quando…")**: foto, áudio ou vídeo curto (ex.: até 30 s) selado até uma
    data ou situação; privado entre duas pessoas. Candidato a primeiro formato de vídeo. Depende de Duo Cloud,
    vídeo (Fase 2), cotas e de lembrar a pessoa quando puder ser aberto.
  - **Feed com fim:** se houver feed, ele acaba ("você viu tudo de hoje").
  - **Resumo automático ("nosso ano em 1 minuto"):** o app monta um vídeo curto a partir das datas e momentos do
    casal. Gerar vídeo é pesado: decidir aparelho x servidor e o custo antes de prometer.
- **Fora de questão:** cards e compartilhamento para o Instagram. Não fazer.
