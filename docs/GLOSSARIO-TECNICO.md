# Glossário técnico

> Cada termo técnico novo que aparecer no código, explicado em 1-2 frases com um exemplo do próprio Duo.
> Exigido pela regra "Modo acompanhamento" do `CLAUDE.md`. **Acrescente um termo aqui sempre que ele
> aparecer pela primeira vez no código** — não precisa reler os antigos.

### Build e projeto

- **Gradle (Kotlin DSL):** a ferramenta que compila, testa e empacota o backend, configurada em Kotlin em
  vez do Groovy tradicional. No Duo: `backend/build.gradle.kts`.
- **Gradle wrapper (`gradlew`):** um script que baixa e usa a versão exata do Gradle que o projeto espera,
  para não depender do que está instalado na máquina. É por isso que rodamos `.\gradlew.bat build`.
- **Toolchain (Java):** diz ao Gradle qual versão do Java usar para compilar, mesmo que o JDK instalado no
  sistema seja outro. No Duo: `java.toolchain.languageVersion = 21` em `build.gradle.kts`.
- **Starter (Spring Boot starter):** um pacote de dependências prontas para uma função (web, segurança,
  banco...), para não escolher cada biblioteca na mão. Ex.: `spring-boot-starter-security` já traz tudo
  que o Spring Security precisa.
- **Spotless:** ferramenta que corrige formatação automaticamente (e falha o build se algo não bater). No
  Duo, está configurada para ser mínima: só remove imports não usados e espaços sobrando.

### Spring Boot

- **Spring Boot:** o framework que faz o backend existir — sobe um servidor HTTP, liga ao banco e organiza
  tudo por configuração, não por código escrito à mão. No Duo: `DuoBackendApplication.java` é o ponto que
  liga tudo isso.
- **Bean:** um objeto que o Spring cria e gerencia para você (em vez de você escrever `new MinhaClasse()`).
  No Duo: `securityFilterChain` em `SecurityConfig.java` é um bean — o Spring chama esse método uma vez e
  guarda o resultado para usar em toda requisição.
- **`@Configuration`:** anotação que diz ao Spring "esta classe define beans". É o que torna o `@Bean` de
  dentro de `SecurityConfig` visível para o resto da aplicação.
- **Actuator:** módulo do Spring que expõe informações operacionais do app, como `/actuator/health`
  (está no ar? está saudável?). No Duo, é a única rota pública (RN-20).

### Segurança

- **`SecurityFilterChain` / `HttpSecurity`:** a configuração de quais rotas pedem login e como. No Duo,
  `SecurityConfig.java` define: tudo autenticado, exceto `/actuator/health`.
- **CSRF (Cross-Site Request Forgery):** um ataque onde um site malicioso faz o navegador da vítima enviar
  uma requisição para outro site usando a sessão dela sem querer. A proteção padrão do Spring é pensada
  para formulários com cookie de sessão; como a API hoje não usa isso, está desligada
  (`csrf(AbstractHttpConfigurer::disable)`) — isso será revisto quando o RA-04 (cookie `HttpOnly` de
  renovação) entrar.
- **`httpBasic`:** a forma mais simples de login HTTP (usuário e senha numa janela do navegador). É um
  placeholder temporário em `SecurityConfig.java` só para os testes conseguirem simular "autenticado" —
  será substituído pelo login sem senha do Épico 1 (DEC-026).

### Banco de dados

- **Flyway / migration:** uma ferramenta que versiona o schema do banco como uma sequência de arquivos SQL
  numerados (`V1__`, `V2__...`), aplicados em ordem e nunca editados depois de aplicados. No Duo:
  `V1__create_users.sql` cria a tabela `users`.
- **JPA / Hibernate:** a camada que traduz objetos Java em linhas de tabela e vice-versa. No Duo ainda não
  há nenhuma entidade (`@Entity`) — vem com a tarefa da entidade `User`.
- **`ddl-auto=validate`:** configuração que diz ao Hibernate "confira se as entidades batem com o banco,
  mas nunca crie ou altere tabelas sozinho" — quem manda no schema é o Flyway, não o Hibernate.
- **`open-in-view=false`:** desliga um comportamento padrão do Spring em que a conexão com o banco fica
  aberta durante toda a resposta HTTP (inclusive na hora de montar o JSON). Desligado é mais seguro e
  evita bugs sutis de performance.

### Arquitetura e módulos

- **Monólito modular:** um único programa (não vários serviços separados), mas organizado em módulos com
  fronteiras claras por dentro (DEC-020). No Duo: pacotes como `com.duo.auth`, `com.duo.users`.
- **`package-info.java`:** um arquivo sem código, só com um comentário Javadoc, que documenta o propósito
  de um pacote inteiro. No Duo: `auth/package-info.java` e `users/package-info.java` explicam o que cada
  módulo vai fazer antes mesmo de ter classes.
- **Convenção `internal`:** a API pública de um módulo vive na raiz do pacote (`com.duo.auth.X`); a
  implementação que só o próprio módulo deve usar vive em `com.duo.auth.internal.X`. Ainda não há exemplo
  no código — vem com a primeira classe de verdade.
- **ArchUnit:** biblioteca de teste que não testa comportamento, testa **regras de organização do código**
  (ex.: "nenhum controller acessa o banco direto"). No Duo: `ArchitectureTest.java`.
- **"Slice" (em ArchUnit):** um agrupamento de classes por um critério (aqui, por módulo) usado para checar
  se há dependência circular entre eles (`modulesHaveNoCycles`).

### Testes

- **`@SpringBootTest`:** liga a aplicação Spring inteira (ou boa parte dela) dentro de um teste, em vez de
  testar uma classe isolada. Mais lento, mais realista.
- **`@ActiveProfiles("test")`:** diz ao Spring para usar `application-test.properties` em vez do arquivo
  principal, trocando o banco `duo` pelo `duo_test`.
- **`MockMvc`:** simula requisições HTTP contra os controllers sem precisar de um servidor real escutando
  numa porta. No Duo: `OpenApiDocsTest.java`.
- **Testcontainers:** ferramenta que sobe um banco de dados real (num container) só para os testes, e
  descarta depois. Estava no plano original (DEC-018), mas está **fora por ora** (seção 19) — os testes usam
  um Postgres local instalado de verdade (`duo_test`).
- **`@DataJpaTest` / `@AutoConfigureTestDatabase`:** anotações de teste que isolam só a camada JPA (sem
  subir o app inteiro) e, por padrão, tentam **trocar** o banco configurado por um embutido em memória — o
  que falha se não houver um na dependência (não temos H2). `@AutoConfigureTestDatabase(replace = NONE)`
  desliga essa troca e usa o Postgres real (`duo_test`). **Pegadinha do Spring Boot 4.1:** essas duas classes
  mudaram de pacote em relação a versões antigas/tutoriais na internet — agora vivem em
  `org.springframework.boot.data.jpa.test.autoconfigure` e `org.springframework.boot.jdbc.test.autoconfigure`
  (não mais em `org.springframework.boot.test.autoconfigure.*`). Vale lembrar isso para a próxima slice de
  teste (`@WebMvcTest`, etc.) — o pacote provavelmente também mudou.

### Documentação da API

- **Springdoc / OpenAPI:** gera automaticamente a documentação da API a partir do código (rota
  `/v3/api-docs`), e uma interface visual para explorá-la (Swagger UI). No Duo, está protegida por login
  como qualquer outra rota (RN-20) — `OpenApiDocsTest.java` garante isso.

### Frontend

- **Vite:** a ferramenta que builda e serve o frontend durante o desenvolvimento (recarrega a página
  sozinho a cada salvamento) e gera os arquivos finais para produção.
- **PWA (Progressive Web App):** um site que pode ser "instalado" no celular como se fosse um app, com
  ícone próprio e funcionando (em parte) offline. No Duo: configurado pelo plugin `vite-plugin-pwa` em
  `vite.config.ts`.
- **Manifest (PWA):** um arquivo JSON que diz ao navegador o nome do app, os ícones e a cor de fundo ao
  instalar. No Duo, é gerado a partir do bloco `manifest` dentro de `VitePWA(...)`.
- **Service worker:** um script que o navegador roda em segundo plano, separado da página, capaz de
  interceptar requisições de rede e servir arquivos do cache. É o que permite o PWA abrir mesmo sem
  internet.
- **Workbox:** a biblioteca que o `vite-plugin-pwa` usa por baixo dos panos para gerar o service worker.
  No Duo, configurada para cachear só arquivos estáticos (`globPatterns`) e **nunca** a API
  (`navigateFallbackDenylist: [/^\/api/]`), porque os dados do casal são privados (seção 15 do CLAUDE.md).
- **ESLint:** acha problemas de código (bugs comuns, más práticas), diferente do Prettier, que só formata.
- **Prettier:** formata o código automaticamente (espaços, aspas, quebras de linha) sem opinar sobre lógica.
- **Vitest:** o framework de teste do frontend, feito para funcionar bem com Vite.
- **jsdom:** simula um navegador dentro do Node.js, para os testes do Vitest conseguirem renderizar
  componentes React sem abrir uma janela de verdade.

### PostgreSQL e sistema operacional (setup local no Linux)

- **`initdb` (via `postgresql-setup --initdb` no Fedora):** cria, em disco, a estrutura de arquivos onde o
  PostgreSQL guarda tudo (tabelas, índices, configuração) — como "formatar" um espaço antes de usá-lo. Sem
  isso, o serviço não tem banco nenhum para servir. Roda uma única vez por instalação.
- **Serviço systemd:** o jeito padrão do Linux de manter um programa rodando em segundo plano. `systemctl
  enable` faz ele iniciar sozinho a cada boot; `--now` também inicia imediatamente; `systemctl status` só
  consulta, não muda nada. No Duo: `systemctl enable --now postgresql`.
- **Role (PostgreSQL):** o sistema de "usuários" do próprio Postgres, **separado** dos usuários do Linux,
  mesmo que tenham o mesmo nome por coincidência (ex.: role `postgres` ≠ usuário Linux `postgres`). Uma role
  só pode efetivamente logar como cliente se tiver o atributo `LOGIN`.
- **Autenticação `peer`:** um dos métodos do Postgres para conexões **locais** (via socket, não rede): deixa
  entrar sem senha se o nome do usuário do sistema operacional for igual ao nome da role pedida. É por isso
  que `sudo -u postgres psql` não pede senha — não funciona para conexões por rede (`host`).
- **`pg_hba.conf`:** o arquivo que lista, para cada combinação de origem da conexão (local ou rede) + banco +
  role, qual método de autenticação vale (`peer`, `scram-sha-256`/senha, `trust`...). O Spring Boot conecta
  via rede (`jdbc:postgresql://localhost:5432/...`), então precisa de uma regra `host` que exija senha.
- **`\password` (comando do `psql`):** pede uma senha nova duas vezes, com o texto escondido, e manda já
  criptografada ao servidor — ao contrário de `ALTER ROLE ... PASSWORD '...'` digitado direto, que ficaria
  salvo em texto puro no histórico do shell (RA-09 / regra 11: segredo nunca em log).
- **Owner (dono) de um banco:** a role que automaticamente tem todas as permissões dentro daquele banco, sem
  precisar de `GRANT` manual depois. No Duo: `duo_app` é dono de `duo` e de `duo_test`.
- **Variável de ambiente como segredo:** valores como senha de banco nunca ficam escritos no código — o
  Spring Boot lê `${DUO_APP_DB_PASSWORD}` do ambiente do sistema operacional em tempo de execução. Assim a
  senha nunca entra no Git (e no histórico do Git para sempre, mesmo se removida depois).

### CI (GitHub Actions)

- **CI (Continuous Integration):** rodar automaticamente build e testes a cada mudança no repositório, para
  pegar erros antes de "confiar" no código. No Duo: `.github/workflows/ci.yml`.
- **Job:** um grupo de passos que roda numa máquina própria. No Duo: `backend` e `frontend` rodam em
  paralelo, cada um na sua máquina.
- **Service container (GitHub Actions):** um container extra, disponível só durante o job, para o código
  conversar com algo que normalmente estaria "por fora" (aqui, um Postgres descartável para os testes do
  backend).
- **Concurrency group:** regra que cancela a execução anterior do CI na mesma branch quando um push novo
  chega, para não gastar a cota gratuita de minutos rodando uma versão já desatualizada.

### Sessão e tokens (Épico 1, passos 5 a 7)

- **JWT (JSON Web Token):** um texto assinado que carrega informações (aqui, o id do usuário e a hora de
  vencimento). O servidor confere a assinatura e não precisa consultar o banco. No Duo: o token de acesso de
  15 minutos, assinado com `DUO_JWT_SECRET` (`JwtConfig.java`).
- **Token de acesso x token de renovação:** o de acesso é curto (15 min) e vai em cada chamada; o de renovação
  é longo (30 dias), fica num cookie e só serve para pedir um token de acesso novo. Se o de acesso vazar, o
  estrago acaba em minutos; o de renovação é a chave que merece mais proteção.
- **Bearer:** o jeito de enviar o token de acesso: cabeçalho `Authorization: Bearer <token>`. No Duo: o frontend
  o envia em toda chamada a `/api/...` (`session.ts`).
- **`oauth2-resource-server`:** módulo do Spring Security que valida o token Bearer em toda requisição
  (assinatura, validade, emissor). No Duo: configurado em `SecurityConfig` com `oauth2ResourceServer`.
- **Cookie `HttpOnly`:** cookie que o JavaScript da página não consegue ler, então um script malicioso não o
  rouba. No Duo: `duo_refresh`.
- **Cookie `Secure`:** o navegador só o envia por HTTPS (e por `localhost`). Em desenvolvimento sem HTTPS pode
  ser desligado com `DUO_COOKIE_SECURE=false`.
- **Cookie `SameSite=Strict`:** o navegador não envia o cookie quando a requisição nasce em outro site. É o que
  protege `/api/auth/refresh` e `/logout` de CSRF, por isso o CSRF do Spring continua desligado.
- **Rotação do token de renovação:** cada uso troca o token por um novo e invalida o antigo (RA-05).
- **Família de tokens (`family_id`):** o conjunto de tokens de renovação que descendem do mesmo login (um
  aparelho). Se um token **já usado** reaparece, alguém o copiou: toda a família é encerrada.
- **Hash (SHA-256):** uma "impressão digital" de um texto, impossível de reverter. O banco guarda o hash do
  token de renovação, não o token: se o banco vazar, os tokens não servem para nada.
- **Lock pessimista (`PESSIMISTIC_WRITE`):** trava a linha do banco até o fim da transação, para duas requisições
  simultâneas não usarem o mesmo token de renovação. No Duo: `findForUpdateByTokenHash`.
- **`@Modifying` (Spring Data):** marca uma query de `update`/`delete` escrita à mão. Ela não atualiza os objetos
  que o Hibernate já tem em memória; por isso usamos `flushAutomatically` e `clearAutomatically`.
- **`Clock` (como bean):** o relógio do sistema entregue pelo Spring, para os testes poderem fixar o "agora" e
  testar expiração sem esperar. No Duo: `ClockConfig.java`.
- **Race condition (condição de corrida):** duas requisições simultâneas que disputam o mesmo recurso. No Duo: dois
  primeiros logins com o mesmo `sub`; o índice único do banco decide e o perdedor busca a conta criada.

### Épico 2 — o vínculo

- **Índice único parcial (`CREATE UNIQUE INDEX ... WHERE ...`):** um índice único que só vale para as linhas
  que batem com a condição do `WHERE` — as outras linhas podem repetir o valor sem problema. No Duo:
  `couple_members_active_person_unique` só proíbe duas linhas com o mesmo `user_id` **enquanto** `left_at` for
  nulo (RN-06); depois que a pessoa sai de um vínculo (`left_at` preenchido), aquela linha "antiga" não conta
  mais para a regra, e a pessoa pode entrar em outro vínculo.
