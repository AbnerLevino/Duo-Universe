# DUO UNIVERSE — Contexto de engenharia para a IA

> Este arquivo é o contexto oficial do projeto para assistentes de IA que trabalham no repositório.
> Leia-o inteiro antes de qualquer ação. Ele é a fonte da verdade sobre visão, decisões, regras, escopo e plano.
> **Versão: 2026-11-03 (v4).** Consolida a v3 (Master Prompt: Java/Spring + Duo Cloud) com as decisões de 2026-11-03:
> frontend React + Vite (PWA), modelo "eu → nós" no vínculo, infraestrutura de custo zero, login sem senha
> (proposta), obrigações legais do app, épico 1 especificado e plano de execução.
> Nenhuma decisão antiga foi apagada: as que mudaram estão marcadas como `REVISADA` ou `SUBSTITUÍDA`, com o motivo.
>
> **Atenção:** a seção 19 (final) registra as decisões e o progresso posteriores à v4. Onde ela contradiz o texto
> acima, a seção 19 vale.
>
> **Atualização de 2026-11-03 (mesmo dia):** a regra 5 e a seção "Modo acompanhamento" foram reformadas a pedido
> do fundador. Ele percebeu que o projeto estava avançando mais rápido do que o seu próprio entendimento dele — a
> relação de trabalho muda de "parceiro técnico" para "mentor formando um estagiário". Nada do progresso ou das
> decisões já tomadas (seção 19) é desfeito; só muda **como** se trabalha a partir de agora.

---

## 0. Como começar (leia primeiro)

1. Leia este arquivo inteiro.
2. Execute a **Fase 0** da seção 17. Não escreva código de produção antes de concluí-la.
3. Antes de qualquer coisa, peça ao fundador as decisões marcadas como **bloqueantes** na seção 18.
4. Comece a programar apenas pela **Semana 1** (seção 17), seguindo o épico especificado na seção 12.

O time é **uma pessoa** (o fundador), que trabalha e estuda. O projeto é pensado **como empresa desde o primeiro
código**: decisões documentadas, cuidado com LGPD, segurança e código que outra pessoa conseguiria manter.

## 1. Seu papel

Você atua como arquiteto de software sênior, engenheiro full-stack e analista de requisitos do Duo Universe.
Ajude a construir um produto real, sustentável, seguro, escalável e financeiramente viável, respeitando as decisões
registradas aqui. Atue como parceiro técnico, não como gerador de código: questione decisões com risco arquitetural e
não concorde automaticamente com propostas tecnicamente inadequadas.

## 2. Regras de trabalho (obrigatórias)

1. **Não tome decisões de produto ou arquitetura sozinho.** Itens `PROPOSTA`, `EM ABERTO` ou `EM REVISÃO` não podem
   ser implementados como se estivessem aprovados. Apresente alternativas, vantagens, desvantagens e consequências,
   recomende uma com justificativa e aguarde a decisão.
2. **Decisões pendentes têm hora certa** (seção 18): só cobre as que bloqueiam a etapa atual; as demais entram
   na semana em que forem necessárias.
3. **Não programe fora do escopo do MVP** (seção 11). Ideias novas vão para as fases futuras, nunca direto no código.
4. **Trabalhe por etapas** (seção 17). Ao fim de cada etapa: demonstração, changelog e **pausa para aprovação**.
5. **Trate o fundador como um estagiário que você está formando** (seção "Modo acompanhamento", logo abaixo).
   Ele programa em Java e TypeScript, mas está aprendendo Spring Boot, Gradle, segurança e arquitetura neste
   projeto. Nunca assuma que ele já conhece uma ferramenta, anotação, padrão ou termo técnico.
6. **Após cada mudança técnica relevante, entregue um changelog** neste formato:

   ```
   ### Changelog
   - Arquivo: <caminho>
     - O que mudou: <descrição curta>
     - Por quê: <motivo, citando DEC/RN/RA/RF/RNF quando houver>
   ```

7. **Cite os códigos** (DEC-xxx, RN-xx, RA-xx, RF-xx, RNF-xx) em commits, comentários relevantes e testes.
8. **Antes de introduzir uma tecnologia nova**, explique: qual problema resolve, por que é necessária, quanto custa,
   quais alternativas existem e qual o impacto na manutenção. Não recomende por popularidade.
9. **Não invente APIs, configurações ou funcionalidades de bibliotecas.** Consulte a documentação oficial.
10. **Nada está concluído sem compilar e sem os testes relevantes passando.**
11. **Segurança primeiro:** nenhum segredo no frontend nem no repositório; CPF nunca em texto aberto; autorização
    sempre no backend; nunca confiar em IDs enviados pelo frontend para autorizar operações; códigos, tokens e URLs
    assinadas nunca aparecem em logs.
12. **Custo zero:** não introduza serviço pago. Se algo exigir cartão ou cobrança, avise antes.
13. **Se algo neste documento parecer contraditório ou incompleto, pergunte antes de assumir.**

### Modo acompanhamento (obrigatório em todas as sessões)

O objetivo não é só construir o Duo, é o fundador **entender e conseguir explicar** tudo o que existe nele.
O projeto não para; o ritmo é ajustado para que ele acompanhe.

**Dois ritmos dentro do Modo acompanhamento (acordado em 2026-11-03):** o fundador já domina CRUD básico em
Spring Boot (entidade, repository, controller, service, nos padrões comuns) — ensinar isso do zero seria
perda de tempo.
- **CRUD mecânico, dentro de padrões que ele já conhece:** implemente direto, sem pausar para ensinar
  conceito por conceito. Changelog (regra 6) continua obrigatório; o resto deste "Modo acompanhamento" (abaixo)
  não se aplica a essa categoria.
- **Qualquer coisa concreta e específica do domínio do Duo** (regras de negócio, concorrência, segurança,
  autenticação, fluxos que não são CRUD genérico): volta ao Modo acompanhamento, mas numa **versão mais leve**
  do que o resto desta seção descreve — explique o conceito novo antes de implementar, mas sem o ritual
  inteiro (sem bloco "o que você precisa saber" nem perguntas de verificação formais a cada passo).
- Na dúvida sobre qual ritmo vale para uma tarefa, pergunte antes de seguir.

**Antes de cada passo**
- Diga em 2 ou 3 frases o que vai fazer, **por quê**, e onde isso se encaixa no Duo (qual épico, qual regra).
- Se o passo usar algo novo (uma anotação do Spring, um comando do Gradle, um conceito como JPA, migration,
  token, container), explique o conceito antes, com um exemplo ou analogia simples.

**Durante**
- **Passos pequenos:** um conceito novo por vez. Prefira vários passos curtos a um grande.
- **Um arquivo por vez** quando o assunto for novo: crie ou altere, explique o que cada parte importante faz,
  e só então siga para o próximo.
- Ao criar um arquivo, explique **para que ele existe** e **o que aconteceria se ele não existisse**.
- Ao adicionar uma dependência, explique o que ela resolve e por que não fazer na mão.
- Sempre que fizer sentido, **deixe uma parte para o fundador escrever** (uma regra de negócio, um teste, um campo),
  com dicas, e revise o que ele escreveu com feedback direto e gentil.

**Depois de cada passo**
- Um bloco **"O que você precisa saber"**: 3 a 5 pontos com o essencial do que foi feito, em linguagem simples.
- Uma ou duas **perguntas de verificação** ("o que acontece se...?", "por que essa classe não acessa o banco
  direto?"). Se a resposta mostrar dúvida, explique de outro jeito antes de seguir.
- **Só avance quando o fundador disser que entendeu.**
- O changelog continua obrigatório (regra 6).

**Mapa do projeto**
- Mantenha um arquivo `docs/MAPA.md`, em português simples, explicando o que cada pasta e cada arquivo importante
  faz e como as peças se conectam (ex.: requisição → controller → service → repository → banco).
- Atualize o mapa a cada mudança que crie, mova ou remova arquivos.
- Mantenha também um `docs/GLOSSARIO-TECNICO.md` com cada termo técnico novo que aparecer (ex.: bean, DTO,
  migration, JWT), explicado em uma ou duas frases com um exemplo do próprio Duo.

**Tom**
- Seja um mentor paciente: explique sem pressa, sem jargão desnecessário e sem fazer o fundador se sentir mal
  por não saber algo. Dúvida é parte do trabalho.
- Explicar não é escrever textos enormes: seja claro e objetivo, e aprofunde quando ele pedir.

## 3. Visão do produto

> "Two lives. One universe."

O Duo Universe é um lugar para **viver e guardar a história de uma pessoa e de um casal**.

### A junção (DEC-023)

O Duo é a junção de dois produtos que se completam:

- **O espaço íntimo do casal (a alma):** o momento do vínculo como um ritual, a pergunta do dia, a pausa pensada para
  quem brigou feio, o princípio da liberdade, o "lugar quentinho".
- **A plataforma de memórias (a utilidade):** guardar e organizar fotos e lembranças, aliviando o espaço do celular.

Como se ligam: o momento do vínculo é a primeira memória do casal; respostas da pergunta do dia e acontecimentos
importantes viram registros na linha do tempo; a pausa e as regras de saída existem para proteger as memórias.

**Diretriz de produto:** a parte do relacionamento é a porta de entrada; o armazenamento é o motivo para ficar.
A comunicação destaca o vínculo e o casal, nunca "um lugar para guardar fotos".
**Modelo de negócio previsto:** experiência do relacionamento gratuita; mais espaço, originais e vídeos nos planos pagos.

### Do "eu" ao "nós" (DEC-002, revisada em 2026-11-03)

- **Solteiro:** tem o seu **Personal Universe** (perfil, fotos e registros próprios).
- **Vinculado:** não existe mais um "eu" separado. Tudo vive no **Couple Universe** — "uma só carne" — com:
  - **Centro do casal:** fotos e memórias de "nós".
  - **Lado de cada um:** onde cada pessoa posta os próprios momentos; o parceiro vê, porque é um só universo.
- **No momento do vínculo, cada pessoa escolhe o que leva** da vida de solteira para o seu lado (diploma, viagens,
  família). O que não for levado fica guardado, privado e invisível, e volta para ela se sair do vínculo.
- **No término:** o que cada um postou no próprio lado e o que estava guardado volta para a pessoa. O centro
  do casal nunca é apagado (RN-17/RN-18); cada ex-membro escolhe, individualmente, esconder o centro do casal
  da própria visão — a escolha de um não afeta o que o outro vê, e qualquer um pode desfazer depois (Q-17,
  decidido em 2026-11-05).

### Princípios

- **Liberdade:** "o relacionamento é deles, não nosso". O app oferece; o casal decide. Liberdade precisa virar regra
  concreta: todo botão tem comportamento definido, inclusive quando o casal discorda.
- **Maturidade:** o Duo é feito para pessoas maduras; o app não fiscaliza nem arbitra relacionamentos.
- **Pisos mínimos** (o que o app sempre garante, sem interferir na relação): qualquer um sai sozinho a qualquer
  momento; o que é seu volta para você; ninguém apaga sozinho o espaço inteiro do casal.
- **Lugar quentinho:** privado por padrão.
- **Formatos próprios:** nada de copiar stories ou feed infinito. Interface limpa, animada e com identidade própria.

## 4. Objetivos técnicos

1. Custo zero de operação no início (seção 13).
2. Independência de fornecedores: infraestrutura própria e substituível.
3. Código organizado, testável e de fácil manutenção.
4. Segurança e privacidade como requisito, não como acabamento.
5. Escalar gradualmente, sem arquitetura cara antes da hora.

## 5. Glossário (linguagem ubíqua)

Convenção decidida: **código e banco em inglês; interface em português.**

| Termo (PT) | Nome no código | Definição |
| --- | --- | --- |
| Pessoa / usuário | `User` | Quem usa o app. Conta e login próprios. |
| Personal Universe | escopo `PERSONAL` | Espaço do solteiro. Durante o vínculo, o que não foi levado fica guardado (`ARCHIVED`). |
| Couple Universe | `Couple` + escopo `COUPLE` | O universo do casal. |
| Centro do casal | `CoupleCenter` / campos de `Couple` | O que é de "nós". |
| Lado de cada um | `CoupleMember` + escopo `MEMBER_SIDE` | Momentos de cada pessoa dentro do casal, visíveis ao parceiro. |
| Membro do casal | `CoupleMember` | Associação pessoa ↔ casal. |
| Momento do vínculo | `CouplePairing` | Momento especial em que o vínculo nasce. **Não é "convite"**. Na interface: "Couple Universe" (Q-10, 2026-11-04); no código segue `CouplePairing`. |
| Pausa / Encerramento | `Couple.state = PAUSED / ENDED` | Estados do vínculo. |
| Duo Cloud | módulos `storage` + `media` | Sistema de armazenamento de fotos e vídeos. |
| Mídia | `MediaAsset` | Foto ou vídeo armazenado. |
| Cota | `StorageQuota` | Espaço disponível, usado e contratado. |
| Sessão de upload | `UploadSession` | Envio em andamento. |
| Memória | `Memory` | Registro com significado na linha do tempo. |
| Passkey | `PasskeyCredential` | Credencial biométrica do aparelho usada para entrar (DEC-026). |
| Cantinho | — | Módulo opcional (Bíblia, financeiro, metas, jogos). Fase 3. |
| Cofre do casal | — | Cantinho íntimo criptografado, 18+. Fase 3. |

## 6. Status dos conflitos da v3

| Conflito | Status |
| --- | --- |
| C-01 Web vs. app | **Resolvido:** PWA com React + Vite no MVP; app nativo (backup automático) junto com os planos pagos (DEC-013, DEC-024) |
| C-02 "Convites" | **Resolvido:** a experiência nunca é de "convite"; backend genérico (DEC-012) |
| C-03 Edição livre vs. proteção | **Encaminhado:** edição livre + lixeira; prazo e exclusão em massa em aberto (Q-04, Q-19) |
| C-04 Personal permanente | **Resolvido:** modelo "eu → nós" (DEC-002) |
| C-05 Término e memórias | **Resolvido:** lado de cada um e arquivados voltam à pessoa; centro do casal nunca é apagado, cada ex-membro pode esconder da própria visão (Q-17) |
| C-06 Custo do armazenamento | **Encaminhado:** fotos comprimidas sem vídeo no gratuito; cotas em aberto (Q-20) |
| C-07 Hospedagem sem custo | **Resolvido:** Oracle Cloud Always Free (DEC-025) — **revisto na seção 19: o fundador não tem cartão** |
| C-08 Escopo do MVP | **Encaminhado:** seção 11 |

## 7. Registro de decisões (ADRs)

Status: `DECIDIDO`, `PROPOSTA`, `EM ABERTO`, `REVISADA` (vale, com mudança), `SUBSTITUÍDA` (histórico).

### DEC-001 — Foco inicial · `REVISADA`
MVP com o universo do solteiro (mínimo) e o Couple Universe. A parte social dos solteiros (descobrir pessoas,
comunidades) fica no horizonte.

### DEC-002 — Do "eu" ao "nós" · `DECIDIDO` (revisada em 2026-11-03; pausa revisada em 2026-11-04)
Solteiro tem Personal Universe; ao se vincular, deixa de existir um "eu" separado e tudo vive no Couple Universe
(centro do casal + lado de cada um). No vínculo, a pessoa escolhe o que leva; o resto fica guardado e volta no
término. Login sempre individual; autoria sempre rastreável. (A v3 propunha manter o Personal Universe permanente;
o fundador decidiu pela visão original.) **Revisão de 2026-11-04 (P5, Épico 2):** durante a `PAUSED`, a pessoa
volta a **ver** (leitura) o que postou no próprio lado e o que estava arquivado — RN-13a, `docs/epico-2-vinculo.md`.
Falta decidir se dá para postar/editar no Personal durante a pausa.

### DEC-003 — Perfil do casal = centro + lado de cada um · `DECIDIDO`
O lado de cada um agora inclui fotos e momentos, não só nome, foto e bio.

### DEC-004 — Edição livre por todos os membros · `DECIDIDO` (com lixeira)
Qualquer membro edita o conteúdo do casal; o que for apagado vai para uma lixeira com prazo (Q-04).
Excluir o espaço inteiro do casal sozinho não é permitido (piso mínimo).

### DEC-005 — Um vínculo ativo por pessoa · `DECIDIDO`
Garantido no banco (índice único parcial) **e** validado de forma transacional, à prova de concorrência.

### DEC-006 — Vínculo com N membros; MVP com 2 · `DECIDIDO`

### DEC-007 — Estados do vínculo · `DECIDIDO`
`ACTIVE → PAUSED`, `PAUSED → ACTIVE`, `ACTIVE → ENDED`, `PAUSED → ENDED`. `ENDED` é final.
Máquina de estados no domínio, transições num único serviço transacional.

### DEC-008 — Saída livre e individual · `DECIDIDO`

### DEC-009 — Retorno ao solteiro · `DECIDIDO`
No término, a pessoa volta a ter o Personal Universe com o que postou no próprio lado e o que estava guardado.

### DEC-010 — Apenas 18+ · `DECIDIDO`
Por causa do ECA Digital (Lei 15.211/2025). Regras de 16–17 anos documentadas para o futuro (DEC-016).

### DEC-011 — Verificação de idade por CPF em camadas · `DECIDIDO`
Implementada no backend (módulo `verification`). Obrigatória **antes de abrir para o público**; o piloto com casais
conhecidos pode começar antes dela (Q-21 confirma). O CPF prova a idade do titular, não de quem digita.

### DEC-012 — O vínculo nasce por um momento especial, não por convite · `REVISADA` (2026-11-04)
**Formato decidido:** cada um conta a **sua história** (como se conheceram, o tempo antes de namorar, o que
pensava, o que pensa agora), tudo obrigatório. As duas histórias são reveladas ao mesmo tempo quando as duas
estão completas, e o Couple Universe nasce. O vínculo **não é um pedido de namoro** (o casal já existe na vida
real); continua sendo um momento especial, e pode ganhar celebração visual (gesto, em `docs/IDEIAS.md`).
Backend: proposta + confirmação de todos (RN-08). A ideia da pergunta secreta com resposta aproximada foi
**abandonada**: o vínculo não é uma prova. Detalhes e perguntas abertas em `docs/epico-2-vinculo.md`.

### DEC-013 — Entrega mobile · `REVISADA` (2026-11-03)
**PWA mobile-first** no MVP: roda no iPhone e no Android sem custo e sem lojas. App nativo (com backup automático
da galeria) entra junto com os planos pagos, consumindo a mesma API. Versão desktop não é foco.

### DEC-014 — Stack React Native (Expo) + Supabase · `SUBSTITUÍDA`
Histórico: React Native → Flutter → React Native → Java/Spring (DEC-018) com React + Vite (DEC-024).

### DEC-015 — Escopo do MVP · `REVISADA` (ver seção 11)

### DEC-016 — Regras de 16–17 anos · `EM ABERTO` (fase futura; não implementar)
Parceiro entre 16 e 19; consentimento dos pais; visão dos pais sem intimidade; regra vale na formação do vínculo.

### DEC-017 — Compatibilidade com Expo Go · `SUBSTITUÍDA` (não há app React Native no MVP)

### DEC-018 — Backend Java 21 + Spring Boot · `DECIDIDO` (frontend revisado pela DEC-024)
Java 21 LTS, Spring Boot (Web, Security, Data JPA/Hibernate, Validation, Actuator), PostgreSQL, Flyway, Springdoc
OpenAPI, JUnit 5, Mockito, Testcontainers. **O Spring é a única camada de lógica e API.**

### DEC-019 — Independência de fornecedores · `DECIDIDO`
Infraestrutura própria e substituível. Fornecedores atrás de abstrações próprias (ex.: `MediaStorageProvider`).
Supabase Auth e Supabase Database não são usados como núcleo.

### DEC-020 — Monólito modular · `DECIDIDO` (sem microserviços)

### DEC-021 — Duo Cloud · `DECIDIDO`
Buckets privados, URLs assinadas temporárias, armazenamento local no desenvolvimento e S3 compatível em produção.

### DEC-022 — Cotas próprias e monetização futura · `DECIDIDO`
Planos futuros: Duo Free, Duo Plus, Duo Premium. Limites comerciais só depois de medir custos reais.

### DEC-023 — O Duo é a junção do casal com as memórias · `DECIDIDO`

### DEC-024 — Frontend React + Vite como PWA · `DECIDIDO` (2026-11-03)
- **Alternativas:** Next.js; Next.js como camada de lógica entre o frontend e o Spring.
- **Decisão:** React + Vite + TypeScript, PWA mobile-first. **Sem Next.js.**
- **Justificativa:** o app é todo privado (sem ganho de SEO ou renderização no servidor); Next como camada de lógica
  criaria um segundo backend, autenticação em dois lugares e regras espalhadas. O fundador já usa Vite.
- **Consequência:** o frontend é gerado como arquivos estáticos e servido pela mesma origem do backend (DEC-025).

### DEC-025 — Infraestrutura de custo zero · `REVISADA` (armazenamento de mídia: R2, ver seção 19; deploy adiado)
- **Topologia:** VM ARM da **Oracle Cloud Always Free** (2 OCPUs, 12 GB) rodando em Docker: Spring Boot,
  **PostgreSQL próprio** e **Caddy** (HTTPS automático com Let's Encrypt; serve a PWA em `/` e faz proxy de `/api`
  para o Spring). Mesma origem: sem CORS e com cookies `SameSite=Strict`.
- **Mídia:** Object Storage S3 compatível. Recomendação: **Cloudflare R2** (10 GB gratuitos, saída gratuita).
  O fundador sugeriu o Supabase Storage; o plano gratuito tem 1 GB de arquivos e 5 GB de saída por mês, o que não
  comporta o piloto. Decisão final em Q-15; o código não depende disso (`MediaStorageProvider`).
- **Backups (obrigatório desde o primeiro deploy):** `pg_dump` diário enviado para um bucket separado; teste de
  restauração documentado. Motivo: o banco vive na VM, e a Oracle pode recuperar instâncias ociosas por 7 dias.
- **Domínio:** subdomínio gratuito no piloto (ex.: DuckDNS); domínio próprio (~R$ 40/ano) antes do lançamento.
- **CI:** GitHub Actions, enxuto (repositório privado tem cota de minutos).
- **Atenção:** Oracle exige cartão de crédito para verificação (sem cobrança); o R2 deve exigir meio de pagamento
  na ativação. Confirmar com o fundador (Q-14).

### DEC-026 — Entrar sem senha: passkeys + código por e-mail · `DECIDIDO` em 2026-11-03 (Q-16 confirmada)
- **Contexto:** o fundador quer uma entrada inovadora, sem e-mail e senha tradicionais.
- **Alternativa rejeitada:** usar uma pergunta pessoal (cor favorita, flor) como senha. Motivo: respostas de baixa
  entropia, fáceis de adivinhar por força bruta e conhecidas justamente pelo parceiro, ex e amigos. A ideia da
  pergunta secreta foi movida para o momento do vínculo (DEC-012), como ritual, não como autenticação.
- **Decisão:** passkeys (WebAuthn, biometria do aparelho) como forma principal; código de 6 dígitos por e-mail como
  alternativa e recuperação. Nenhuma senha em nenhum momento. Verificar na documentação oficial o suporte a passkeys
  da versão do Spring Security usada.
- **Consequências:** precisa de um serviço de e-mail transacional gratuito (Q-18); o e-mail continua obrigatório para
  recuperação e contato (LGPD). Passkeys exigem HTTPS (ou `localhost`); a ordem de implementação está na seção 19.

### DEC-027 — Visibilidade para outros casais · `DECIDIDO` (fase futura)
Privado por padrão, sempre. O casal só aparece para outros casais se **os dois** escolherem; se um desistir, volta a
ser privado na hora; visibilidade por pedaço (perfil ou um álbum), nunca tudo. Fora do MVP. O modelo de dados já
nasce com nível de visibilidade (seção 14).

### DEC-028 — Repositório privado; código em inglês, interface em português · `DECIDIDO`

### DEC-029 — Responsabilidades legais do app · `DECIDIDO` (ver seção 16)
O Duo não arbitra relacionamentos nem vigia conteúdo privado, mas é responsável pelos dados que guarda.

## 8. Regras de negócio

**Cadastro e idade**
- **RN-01:** só pessoas com 18 anos ou mais.
- **RN-02:** idade comprovada pelo CPF em camadas (dígitos → data de nascimento → cruzamento com nome e data).
- **RN-03:** bloqueio de 1 hora após 2 tentativas que não batem; passado esse tempo, uma nova tentativa é
  liberada (Q-07, decidido em 2026-11-05).
- **RN-04:** um CPF por conta.
- **RN-05:** CPF guardado apenas como HMAC-SHA256 com segredo do servidor.
- **RN-05a:** se a consulta falhar, a verificação fica pendente, nunca aprovada.

**Vínculo**
- **RN-06:** um vínculo `ACTIVE` ou `PAUSED` por pessoa.
- **RN-07:** no MVP, exatamente 2 membros.
- **RN-08:** o vínculo só nasce com a confirmação de todos.
- **RN-09:** no momento do vínculo, cada pessoa escolhe o que leva para o seu lado; o resto fica `ARCHIVED`,
  privado e invisível ao parceiro. Nada vai automaticamente.
- **RN-10:** a data do vínculo é o primeiro evento da linha do tempo.

**Universo do casal**
- **RN-11:** qualquer membro edita o conteúdo do casal; exclusões vão para a lixeira por 30 dias, depois
  apagadas de vez (Q-04, decidido em 2026-11-05; o job de limpeza automática é detalhe de implementação,
  a decidir na Semana 4).
- **RN-11a:** exclusão em massa (vários itens de uma vez) no centro do casal exige consentimento explícito
  dos dois membros, dado nas duas contas — não é uma ação solo (Q-19, decidido em 2026-11-05). Motivo do
  fundador: assuntos de relacionamento sempre têm conversa, saudade, diálogo que muda as coisas; o app não
  resolve isso, só acolhe.
- **RN-12:** só membros atuais acessam o Couple Universe.
- **RN-12a:** o lado de cada um é visível ao parceiro; só o dono posta e apaga no próprio lado.

**Pausa, saída e retorno**
- **RN-13:** qualquer membro pausa sozinho; nada é apagado (quem retoma e o que se pode fazer na pausa: Q-02, Q-06).
- **RN-14:** qualquer membro sai sozinho, a qualquer momento.
- **RN-15:** ao sair, a pessoa volta ao Personal Universe com o que postou no próprio lado e o que estava guardado.
- **RN-16:** sem alardes ou notificações públicas sobre pausa ou término.
- **RN-17:** o término nunca apaga as memórias de ninguém.
- **RN-17a:** após o término, cada ex-membro escolhe, individualmente, esconder o centro do casal da própria
  visão (e desfazer depois, se quiser); essa escolha é pessoal e não afeta o que o ex-parceiro vê — o dado
  em si nunca é apagado, só a visualização muda (Q-17, decidido em 2026-11-05).
- **RN-18:** ninguém apaga sozinho o espaço inteiro do casal.

**Duo Cloud**
- **RN-19:** cada mídia tem dono, escopo e visibilidade; o acesso é decidido no backend por essas três informações.
- **RN-20:** toda autorização é verificada no backend pelo usuário autenticado.
- **RN-21:** arquivos só por URL assinada de curta duração.
- **RN-22:** todo upload respeita a cota antes de ser aceito.
- **RN-23:** arquivos validados por tipo real, tamanho e checksum.
- **RN-24:** no gratuito, fotos comprimidas no próprio aparelho, sem vídeo (Q-20 define cotas e tamanhos).

## 9. Arquitetura

**Visão geral:** PWA (React + Vite) → Caddy (mesma origem) → API Spring Boot → PostgreSQL próprio; mídias enviadas
direto do aparelho ao Object Storage por URL assinada.

**Organização do repositório (monorepo, proposta para confirmar na Fase 0):**

```
duo/
  backend/     Spring Boot (Gradle ou Maven: propor na Fase 0)
  frontend/    React + Vite + TypeScript (PWA)
  infra/       docker-compose, Caddyfile, scripts de backup e deploy
  docs/        especificações dos épicos, ADRs complementares
  CLAUDE.md
```

**Módulos do backend (monólito modular):**

```
com.duo
  auth/           passkeys, códigos por e-mail, sessões, Spring Security
  users/          conta e Personal Universe
  verification/   verificação de idade por CPF
  relationships/  casal, membros, momento do vínculo, estados
  storage/        MediaStorageProvider (local e S3)
  media/          upload, download autorizado, miniaturas, cotas, lixeira
  albums/         álbuns
  memories/       linha do tempo, pergunta do dia, datas especiais
  subscriptions/  planos (fase 2)
  shared/         erros, utilidades, configuração
```

Regras: controllers sem regra de negócio e sem acesso direto a repositories; um módulo não usa o interior de outro;
sem dependências circulares; sem abstrações sem justificativa. Spring Modulith ou ArchUnit podem ser propostos para
verificar as fronteiras nos testes.

**Fluxo de upload:** o app comprime a foto → o backend confere permissão e cota e cria uma `UploadSession` com URL
assinada → o app envia direto ao storage → o app confirma → o backend valida tamanho e checksum e marca como pronta →
a miniatura é gerada de forma assíncrona (uma tabela de tarefas pendentes basta no início).

**Ambiente de desenvolvimento:** Postgres e emulador/implementação local de storage em Docker Compose; JDK 21 e
Node.js LTS instalados no computador (melhor experiência na IDE). Dev Containers são uma alternativa aceitável.
(*Revisto na seção 19: por ora tudo instalado direto no sistema (agora Linux), sem Docker — pendente confirmar
a instalação do JDK 21 e do PostgreSQL.*)

## 10. Funcionalidades por fase

| Fase | Funcionalidades |
| --- | --- |
| **MVP mínimo** (piloto com casais conhecidos) | Cadastro e entrada; perfil; Personal Universe básico; momento do vínculo com a escolha do que levar; centro do casal e lado de cada um com fotos; linha do tempo; contador de tempo juntos; pergunta do dia |
| **MVP completo** (antes de abrir ao público) | Verificação de idade; álbuns; datas especiais com lembretes; pausa, saída e retorno; lixeira; cotas visíveis; exclusão e exportação de dados; denúncia; termos e política; celebração visual do vínculo |
| **Fase 2** | Planos pagos; vídeos; originais; app nativo com backup da galeria; diário compartilhado; sonhos e objetivos; jogos de conhecimento mútuo; música (player do Spotify); sinal de idade das lojas |
| **Fase 3** | Cantinhos (Bíblia, financeiro, metas); cofre do casal; visibilidade para outros casais; trisal; regras de 16–17 anos |
| **Horizonte** | Universo social dos solteiros |

**Cofre do casal (registro, não implementar):** todos os membros 18+ verificados, sem exigir casamento;
criptografia de ponta a ponta; bloqueio de print; arquivos só no app; posicionado como "cofre privado".

## 11. Escopo do MVP

O MVP mínimo e o MVP completo da seção 10. Nada além disso é implementado sem decisão registrada.

## 12. Épico 1 — Conta e acesso (especificado; Q-16 confirmada)

**Objetivo:** criar a conta, entrar e sair com segurança, sem nunca usar senha.

**Histórias**
- **H1 Criar conta:** como pessoa nova, quero criar minha conta com nome, e-mail e a biometria do celular.
- **H2 Entrar:** como usuário, quero entrar com a biometria do celular.
- **H3 Entrar em outro aparelho:** como usuário num aparelho novo, quero entrar com um código por e-mail e cadastrar a
  biometria desse aparelho.
- **H4 Sair:** como usuário, quero sair deste aparelho ou de todos.
- **H5 Recuperar acesso:** como usuário que perdeu o celular, quero recuperar a conta pelo e-mail e remover o aparelho
  perdido.

**Regras**
- **RA-01:** e-mail único, confirmado por código antes de a conta ficar ativa.
- **RA-02:** nenhuma senha em nenhum momento.
- **RA-03:** código de 6 dígitos, válido por 10 minutos, no máximo 5 tentativas; guardado só como hash.
- **RA-04:** token de acesso de 15 minutos; token de renovação de 30 dias em cookie `HttpOnly`, `Secure`,
  `SameSite=Strict`.
- **RA-05:** o token de renovação é rotacionado a cada uso; reutilização de um token antigo encerra todas as sessões
  da pessoa.
- **RA-06:** várias passkeys por conta (uma por aparelho), removíveis pelo dono.
- **RA-07:** entrada e recuperação nunca revelam se um e-mail tem conta.
- **RA-08:** limite de tentativas por e-mail e por IP em todas as rotas de entrada.
- **RA-09:** códigos, tokens e chaves nunca em logs.

**Critérios de aceitação**
- *Dado* uma pessoa nova, *quando* informa nome e e-mail e confirma o código, *então* o app pede a biometria e a conta
  é criada com uma passkey.
- *Dado* um e-mail que já tem conta, *quando* alguém tenta cadastrar de novo, *então* recebe a mesma mensagem genérica
  e o dono do e-mail é avisado.
- *Dado* um usuário com passkey neste aparelho, *quando* toca em "Entrar" e confirma a biometria, *então* entra sem
  digitar nada.
- *Dado* um aparelho novo, *quando* o usuário confirma o código do e-mail, *então* entra e o app oferece cadastrar a
  biometria.
- *Dado* cinco códigos errados, *então* o código é invalidado e é preciso pedir outro.
- *Dado* "Sair de todos os aparelhos", *então* todas as sessões são encerradas.
- *Dado* um token de renovação já usado, *quando* ele é reapresentado, *então* todas as sessões são encerradas.

**Exceções:** navegador sem suporte a passkey (o código por e-mail funciona como entrada completa); e-mail não chega
(reenviar com intervalo mínimo); passkey apagada do aparelho; dois cadastros simultâneos com o mesmo e-mail (unicidade
no banco); celular roubado (entrar em outro aparelho, remover a passkey, encerrar sessões).

**Modelo de dados do épico**
- `users`: id, email (único, sem diferenciar maiúsculas), display_name, email_verified_at, created_at, deleted_at.
- `passkey_credentials`: id, user_id, credential_id (único), public_key, sign_count, label, created_at, last_used_at.
- `email_login_codes`: id, email, code_hash, expires_at, attempts, consumed_at.
- `refresh_tokens`: id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by.

## 13. Infraestrutura e custos

| Peça | Onde | Custo | Observação |
| --- | --- | --- | --- |
| API Spring, PostgreSQL, Caddy | VM ARM Oracle Always Free | R$ 0 | Cartão para verificação; risco de recuperação por ociosidade (**sem cartão: ver seção 19**) |
| Mídias | R2 recomendado (Q-15) | R$ 0 até 10 GB | Saída gratuita |
| Backups do banco | Bucket separado | R$ 0 | Diário, com teste de restauração |
| Domínio | Subdomínio gratuito no piloto | R$ 0 / ~R$ 40 por ano depois | — |
| E-mail transacional | A pesquisar (Q-18) | R$ 0 no plano gratuito | — |
| CI | GitHub Actions | R$ 0 dentro da cota | — |

Estimativa de mídia no R2 (foto comprimida ~0,5 MB): 500 fotos por usuário ≈ 250 MB ≈ US$ 0,004 por mês; os 10 GB
gratuitos cobrem cerca de 40 usuários cheios. Originais e vídeos só nos planos pagos.

## 14. Modelo de dados (proposta, além do épico 1)

Entidades: `User`, `Couple`, `CoupleMember`, `CouplePairing`, `Album`, `MediaAsset`, `StorageQuota`, `UploadSession`,
`Memory`, `DailyQuestion`, `QuestionAnswer`, `SpecialDate`, `TrashItem` (ou exclusão lógica), `AgeVerification`.

`MediaAsset` precisa distinguir:
- **Autoria:** quem enviou.
- **Propriedade:** de quem é (volta para quem no término).
- **Escopo:** `PERSONAL`, `ARCHIVED`, `MEMBER_SIDE`, `COUPLE_CENTER`.
- **Visibilidade:** `ONLY_ME`, `COUPLE`, futuramente `OTHER_COUPLES` (DEC-027).

Garantias: coerência entre escopo e casal por constraint; RN-06 por índice único parcial; exclusão lógica onde houver
lixeira; índices para galeria por escopo e data. Autorização no backend; Postgres RLS opcional como defesa extra.

## 15. Segurança

- Autenticação sem senha (DEC-026); sessões conforme RA-04 e RA-05.
- Autorização por recurso em toda operação, com **testes automatizados de acesso cruzado** (casal A tentando ver
  dados do casal B; pessoa vendo o arquivado do parceiro) para cada endpoint de mídia.
- Validação de entrada e de arquivos (tipo real, tamanho, nome).
- Buckets privados e URLs assinadas curtas.
- Segredos só em variáveis de ambiente do servidor; nunca no repositório.
- Logs sem CPF, códigos, tokens ou URLs assinadas.

## 16. Responsabilidades legais do app (DEC-029)

**É responsabilidade do Duo:**
1. Proteger os dados (LGPD): segurança adequada, mínimo de dados, comunicar incidentes à ANPD e aos usuários.
2. Termos de uso e política de privacidade claros, antes do piloto.
3. Direitos do titular: ver, corrigir, exportar e excluir os dados, com canal de contato.
4. Exclusão de conta dentro do próprio app (exigência das lojas).
5. Verificação de idade (ECA Digital) antes de abrir ao público.
6. Denúncia e bloqueio, exigidos pelas lojas para conteúdo de usuários.
7. Remover conteúdo ilícito quando notificado e comunicar as autoridades em casos graves, especialmente envolvendo
   menores.
8. Guardar registros de acesso (data, hora, IP) por 6 meses, em sigilo (Marco Civil da Internet).
9. Com planos pagos: preço claro, cancelamento fácil e regra clara para as fotos de quem deixar de pagar.

**Não é responsabilidade do Duo:** arbitrar relacionamentos; vigiar conteúdo privado; julgar a maturidade dos casais.

Os textos legais precisam de revisão de um advogado antes da abertura ao público.

## 17. Plano de execução

Cada etapa termina com demonstração, changelog e **pausa para aprovação**.

**Fase 0 — antes de codar (cerca de uma semana)**
1. Obter as decisões bloqueantes (seção 18).
2. Pesquisar e confirmar os requisitos de cada serviço (Oracle, R2 ou alternativa, e-mail transacional, GitHub).
3. Criar as contas e a VM (o fundador executa; você orienta). Testar cedo a criação da VM ARM, que pode falhar
   por falta de capacidade.
4. Preparar o computador: JDK 21, Node.js LTS, Docker, Git, IDE.
5. Confirmar a estrutura do repositório e a ferramenta de build.
6. Revisar a especificação do épico 1 (seção 12) com o fundador.

**Semana 1 — fundação local:** monorepo; projeto Spring (Web, Security, Data JPA, Validation, Flyway, PostgreSQL,
Actuator, Springdoc); Docker Compose com Postgres; migration `V1`; endpoint de saúde; testes com Testcontainers;
PWA do Vite abrindo no celular; lint e formatação; CI básica.

**Semana 2 — conta, acesso e primeiro deploy:** épico 1 completo; deploy na VM com Caddy e HTTPS; backups
automáticos funcionando; o app abrindo no iPhone do fundador pelo endereço real.

**Semana 3 — vínculo:** momento do vínculo (backend genérico), escolha do que levar, estados, regra de um vínculo
por pessoa com teste de concorrência.

**Semana 4 — fotos:** `MediaStorageProvider` (local e S3), compressão no aparelho, upload por URL assinada, centro do
casal e lado de cada um, cotas básicas, testes de acesso cruzado.

**Semana 5 — a alma:** linha do tempo (vínculo como primeira memória), contador e pergunta do dia.

**Semana 6 — uso real:** o fundador e a parceira usando no dia a dia; correções.

**Depois:** itens do MVP completo, antes de abrir para casais de fora.

**Meta de sucesso do piloto (proposta):** o fundador e mais quatro casais usando por 30 dias, com três voltando
sozinhos toda semana.

## 18. Decisões pendentes e quando entram

**Bloqueantes da Fase 0** (estado atual na seção 19)
- **Q-13:** quantas horas por semana o fundador consegue dedicar? (ajusta o cronograma)
- **Q-14:** o fundador tem cartão de crédito para a verificação da Oracle e do R2? Se não, propor alternativas.
- **Q-15:** mídias no R2 (recomendado) ou no Supabase Storage?
- **Q-16:** confirma a entrada sem senha com passkeys e código por e-mail (DEC-026)?
- **Q-18:** qual serviço de e-mail transacional gratuito? (pesquisar opções e limites atuais)
- **Q-22:** Gradle ou Maven?

**Antes da semana 2:** Q-23: login com Google também no MVP?

**Antes da semana 3:** ~~Q-01~~, ~~Q-05~~, ~~Q-10~~, ~~Q-06~~ decididas em 2026-11-04; **P1 a P6 do Épico 2
também decididas em 2026-11-04** (convite por @handle dentro do Duo com link/código como alternativa — DEC-031;
história com mínimo de 20 caracteres; consenso das datas por propor/aceitar; "o que levar" modelado já na
Semana 3; Personal Universe volta em leitura durante a pausa, revisando a DEC-002; celebração em tempo real já
na Semana 3). Especificação **aprovada** em `docs/epico-2-vinculo.md`. Ainda aberta: identidade visual e design
system mínimo.

**Antes da semana 4:** ~~Q-04~~, ~~Q-19~~, ~~Q-17~~ decididas em 2026-11-05 (seção 19, RN-17a); Q-20 cotas,
tamanho máximo e compressão das fotos (o fundador quer pensar junto quando chegarmos ao módulo de storage,
por causa da monetização).

**Antes da semana 5:** Q-11/Q-12 decididas em 2026-11-05 (detalhe na seção 19): o app cuida da pergunta do dia;
a resposta do parceiro aparece assim que ele responder, mesmo que eu ainda não tenha respondido a minha; a
pergunta nunca expira (sem marca de "atrasada" — um contador de dias ainda a detalhar na Semana 5); perguntas
de um para o outro com jogos interativos ficam para a Fase 2 (`docs/IDEIAS.md`). Falta só: datas especiais no
MVP mínimo ou completo.

**Antes de abrir ao público:** ~~Q-07~~ (bloqueio de 1h) e ~~Q-21~~ (verificação de idade só antes do público, não
no piloto) decididas em 2026-11-05; domínio próprio; termos de uso e política de privacidade revisados.

---

## 19. Progresso e decisões posteriores à v4 (2026-11-03)

> Esta seção prevalece sobre o texto acima onde houver conflito. Atualize-a a cada etapa concluída.
> **Revisão de 2026-11-04:** limpeza de contradições (PostgreSQL já instalado e confirmado — não é mais
> pendência) e resumo abaixo para retomar o contexto rapidamente.

### Onde paramos / próximo passo (2026-11-04)
- **Ambiente local: pronto.** JDK 21 (via toolchain do Gradle) e PostgreSQL nativo (Fedora, `duo`/`duo_test`)
  funcionando de ponta a ponta. Sem Docker (adiado), sem deploy (adiado — Q-14, sem cartão).
- **Semana 1 (seção 17): concluída**, exceto confirmar o CI verde no GitHub (pendência, ver abaixo).
- **Épico 1, etapa A — passo 1: concluído.** Entidade `User` + `UserRepository` (commit `1e478e0`), testada
  contra `duo_test` de verdade.
- **Épico 1, etapa A (login com Google e sessão própria): CONCLUÍDA em 2026-11-04.** Passos 1 a 7 feitos:
  entidade `User`; migration `V2` (`google_sub`); login com Google (`oauth2-client`); tabela `refresh_tokens`
  (`V3`) com `RefreshTokenService` (emissão, rotação por família, reuso derruba a família, logout por aparelho
  e de todos); JWT de acesso de 15 min (`oauth2-resource-server`, HS256, `DUO_JWT_SECRET`); endpoints
  `POST /api/auth/refresh`, `/logout`, `/logout-all`; `/api/me` por Bearer; frontend com "Entrar com Google",
  tela inicial provisória e "Sair" / "Sair de todos os aparelhos". Commits: `e1780d8` (refresh token),
  `0e334ea` (JWT, endpoints e correções do primeiro login); frontend e docs no commit seguinte (ver `git log`).
  - **Fluxo:** botão → `/oauth2/authorization/google` → Google → `GoogleLoginSuccessHandler` (cria/acha a conta,
    grava o cookie `duo_refresh` — `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/auth` — e redireciona ao
    frontend; a sessão do Spring é descartada) → frontend chama `POST /api/auth/refresh` → recebe o JWT, que
    guarda **só em memória** → chama a API com `Authorization: Bearer`. O refresh é de uso único (RA-05): o
    frontend divide uma única chamada entre pedidos simultâneos (`session.ts`), senão o StrictMode do React o
    leria como roubo.
  - **Erros do login:** o handler redireciona ao frontend com `?erro=email-nao-verificado` ou
    `?erro=email-ja-vinculado` (e-mail já ligado a outro `sub`; não se juntam contas sozinho).
  - **Como rodar (Windows):** definir `DUO_JWT_SECRET` (mínimo 32 caracteres; ex.: gere com
    `openssl rand -base64 48` e `setx DUO_JWT_SECRET "<valor>"`, depois abra um terminal novo); `cd backend &&
    ./gradlew bootRun`; `cd frontend && npm run dev`; abrir `http://localhost:5173`. Se o navegador recusar o
    cookie `Secure` em `http://localhost`, definir `DUO_COOKIE_SECURE=false` só em desenvolvimento. No Linux,
    repetir também as variáveis do Google (seção abaixo).
  - **Testes:** backend 43 (inclui `SessionFlowTest`: refresh, rotação, reuso, token vencido, assinatura falsa,
    emissor errado, logout, logout-all); frontend 9. O "Estado do código" mais abaixo está defasado nesses números.
- **Épico 2 — plano de implementação aprovado em 2026-11-04**, em 18 etapas pequenas (4 blocos: A-esqueleto do
  casal, B-pausa/saída/HTTP/frontend, C está dentro de B, D-celebração em tempo real isolada por último). Cada
  etapa termina com changelog e pausa para aprovação, conforme a regra 4.
- **Etapa 1 (esqueleto do casal, RN-06/07/08) — concluída em 2026-11-04.** Módulo novo `com.duo.relationships`:
  entidades `Couple`/`CoupleMember`, repositórios, migration `V4__create_couples.sql`. A peça central é o
  índice único **parcial** `couple_members_active_person_unique` (`WHERE left_at IS NULL`) — RN-06 (um vínculo
  ACTIVE/PAUSED por pessoa) garantida pelo banco, no mesmo espírito de `users_email_unique`/
  `refresh_tokens_token_hash_unique`. 3 testes novos (`CoupleMemberRepositoryTest`) contra o `duo_test` real.
  Ainda não existe `CouplePairing` (o convite em si) nem nenhum endpoint.
- **Etapa 2 (`CouplePairing`, máquina de estados pura) — concluída em 2026-11-04.** API pública
  `CouplePairingService` (`initiate`/`cancel`/`find`) + entidade interna `CouplePairing`, migration `V5`.
  Expiração de 7 dias (RN-29) checada **lazy** (sem `@Scheduled`): `find` marca `CANCELLED` na hora se o
  convite já venceu. Ainda sem convite real por @handle/link (ids passados direto em teste) e sem HTTP.
  13 testes novos (8 de máquina de estados pura, sem banco; 5 de serviço contra o `duo_test` real).
  **Confirmado nesta sessão:** `bootRun` aplica V1-V5 também no banco de desenvolvimento (`duo`), não só no
  de teste — verificado por um subagente, que **expôs a senha do `duo_app`** num log de depuração por engano
  (ver aviso de segurança registrado na conversa). **Resolvido em 2026-11-05:** o fundador trocou a senha do
  `duo_app` (e a variável `DUO_APP_DB_PASSWORD` correspondente).
- **Etapa 3 (convite por link/código, alternativa — P1) — concluída em 2026-11-04.** `inviteByLink`/
  `redeemByLink` em `CouplePairingService`; código aleatório (`SecureRandom`, 32 bytes) com hash SHA-256
  guardado no banco (mesmo padrão do token de renovação, RA-09), nunca o código em texto puro; uso único;
  migration `V6` acrescenta `invite_code_hash` (índice único parcial). RA-08 (limite de tentativas) ainda não
  implementado — adiado para a Etapa 5 (@handle), onde o risco de força bruta é maior. 24 testes novos.
- **Etapa 4 (coluna `handle` em `users`) — concluída em 2026-11-05.** Decisões confirmadas pelo fundador:
  formato `^[A-Za-z0-9_]{3,20}$` (igual ao recomendado), comparação sem diferenciar maiúsculas (`lower(handle)`,
  mesmo padrão do e-mail); quem já tem conta vai ter uma **tela obrigatória no próximo login** para escolher
  o handle (implementação dessa tela adiada para a Etapa 14 — frontend). Migration `V7`; `UserService` ganhou
  `chooseHandle`/`findByHandle`; `HandleTakenException`/`InvalidHandleException`; `UserSummary` passou a
  carregar o `handle`. 4 testes novos em `UserServiceImplTest`. `./gradlew build` verde.
- **Etapa 5 (convite por @handle, caminho padrão) — concluída em 2026-11-05.** `inviteByHandle` em
  `CouplePairingService`/`Impl`; `HandleNotInvitableException` (mensagem genérica para handle inexistente,
  próprio ou já vinculado — RN-30/RA-07). RA-08 implementado: módulo novo `shared` ganhou `RateLimiter`
  (interface pública) + `RateLimiterImpl` (tabela `rate_limit_attempts`, migration `V8`, mesmo truque de
  travar a linha — `PESSIMISTIC_WRITE` — já usado no `RefreshToken`); por ora limita só por conta (o IP só
  existe quando houver um controller HTTP, Etapa 13; o `RateLimiter` já nasce genérico para isso). Limite
  escolhido (ajustável): 20 tentativas por hora. 10 testes novos (`CouplePairingServiceImplTest`,
  `RateLimiterImplTest`). `./gradlew build` verde.
- **Decisão de organização (2026-11-05):** o fundador propôs separar as pastas por camada técnica
  (`exception/`, `model/`, `repository/`, `service/`) e, depois, considerou traduzir os nomes do código para
  português temporariamente. As duas ficaram **sem aplicar por agora**: o fundador decidiu reforçar
  `docs/GLOSSARIO-TECNICO.md` (novo "dicionário rápido: classe → o que ela é") e `docs/MAPA.md` em vez disso,
  mantendo a estrutura de pastas e o código em inglês como estão (DEC-028 continua `DECIDIDO`, sem mudança).
  Se a necessidade de reorganizar por camada voltar, o desenho já discutido (nested dentro de `internal/`,
  sem precisar tocar no `ArchitectureTest`) fica registrado aqui para não refazer a análise.
- **Reorganização dos pacotes por camada técnica — EXECUTADA em 2026-11-06** (revoga o "sem aplicar" acima; o
  fundador voltou atrás e aprovou o plano, `docs/plano-reorganizacao-pacotes.md`). Quatro etapas, um commit cada
  (`3a8a8a0` shared, `eebbc96` users, `83aa5c8` auth, `42ca045` relationships), build verde com 151 testes em todas,
  `ArchitectureTest` sem alteração. Convenção: raiz do módulo = `controller/`, `exception/`, `dto/` + contratos e
  enums soltos; `internal/` = `model/`, `repository/`, `service/`. Só ajustes de visibilidade (`public` em
  `RefreshCookies`, `JwtConfig.ISSUER`, `CouplePairing.requireConfirmed()`) e posição de testes; nenhuma regra de
  negócio mudou. **Os caminhos citados nesta seção 19 e nos registros por etapa do `docs/MAPA.md` são os de antes
  da reorganização**; a árvore atual está no `docs/MAPA.md` (seção "Como os pacotes do backend estão organizados").
  DEC-028 (código em inglês) segue valendo. Segue valendo também o item de código em português: continua sem aplicar.
- **Etapa 6 (história em 4 passos, RN-25/26) — concluída em 2026-11-05.** Módulo `relationships` ganhou
  `StoryStep`/`StorySummary`/`StoryIncompleteException`; entidade `CouplePairingStory` (migration `V9`, uma
  linha por pessoa por pareamento, 4 colunas de texto); `CouplePairingService` ganhou
  `saveStoryDraft`/`isStoryComplete`/`requireStoryComplete`/`readPartnerStory` (RN-26: só libera a história do
  parceiro quando as duas estão completas); `CouplePairing` ganhou `partnerOf`. **Revisão de plano, decidida
  pelo fundador nesta etapa:** RN-25 não exige mais 20 caracteres mínimos — só texto não vazio (ver linha
  própria na tabela de decisões abaixo e `docs/epico-2-vinculo.md`); e a etapa ficou **só no backend**, sem
  tela nova (demonstração pelos testes; a tela real é a Etapa 14). 17 testes novos, `./gradlew build` verde.
  Detalhe em `docs/MAPA.md`.
- **Etapas 7, 9, 10 e 11 — concluídas em 2026-11-05, numa única sessão (o fundador decidiu os 3 pontos em
  aberto de antemão e pediu para eu codar as quatro em sequência, sem pausa a cada uma — só a Etapa 8 continua
  reservada por causa do teste de concorrência).**
  - **Etapa 7 (P3/RN-27):** `CouplePairing` ganhou `proposeDates`/`acceptDates` — um propõe, o outro aceita
    (nunca quem propôs); uma nova proposta sempre reabre o consenso. Migration `V10`.
  - **Etapa 9 (P4/RN-09):** `Scope` (`PERSONAL`/`ARCHIVED`/`MEMBER_SIDE`/`COUPLE_CENTER`) criado em `shared`,
    como decidido. Só a estrutura — sem tabela, sem endpoint.
  - **Etapa 10 (RN-13 a 18):** `Couple` ganhou `pause`/`resume`/`end`; `CoupleService`/`CoupleServiceImpl`
    novos (`find`/`pause`/`resume`/`leave`). `leave`, no MVP de 2 membros, termina o vínculo inteiro para os
    dois (bulk update de `left_at`). **Refatoração:** o enum de estado do `Couple` saiu de dentro da classe e
    virou `CoupleState` (mesmo padrão de `PairingState`), para a nova exceção pública poder citá-lo.
  - **Etapa 11 (RN-11/12/12a/13a):** `CouplePermissions` — a matriz de permissões por estado, como 4
    perguntas puras (`canViewCoupleCenter`, `canPostToCoupleCenter`, `canPostToOwnSide`, `canUsePersonal`).
    Ainda não é usada por ninguém — é a peça que fotos/memórias vão consultar depois.
  - Build verde, 128 testes no total (28 novos nesta sessão). Detalhe de cada etapa em `docs/MAPA.md`.
- **Etapa 12 — concluída em 2026-11-05 (RN-29, rodou em paralelo a uma conversa sobre a Etapa 8).** O que
  faltava de verdade: `CouplePairing.cancel()` só trocava o estado para `CANCELLED`, sem apagar nada. Agora
  `cancel()` também zera a proposta de datas (Etapa 7) com um `eraseDateProposal()` novo, e
  `CouplePairingServiceImpl` ganhou `cancelAndEraseDrafts(...)`, que além de cancelar também apaga as histórias
  (Etapa 6, tabela separada) com `CouplePairingStoryRepository.deleteAllByPairingId(...)` — usado nos 3 pontos
  que cancelavam um pareamento (`cancel` público e as duas expirações "lazy" em `find`/`redeemByLink`). As
  outras duas decisões da etapa (limpeza lazy; múltiplos `PENDING` por pessoa permitidos) não pediram código —
  só ganharam um teste de regressão. 4 testes novos, 132 no total, `./gradlew build` verde. Detalhe em
  `docs/MAPA.md`.
- **Etapa 8 — concluída em 2026-11-05, com o fundador presente (RN-06/RN-08).**
  - **Mudança de plano decidida pelo fundador nesta etapa:** o vínculo firma **primeiro** (logo depois do
    convite aceito, sem exigir história nem datas prontas); história e datas só existem **depois**, já dentro
    do Couple Universe nascido. Isso revisa a ordem antiga do fluxo (`docs/epico-2-vinculo.md`, atualizado) e
    simplificou o `confirm()` (não checa mais história/data como pré-requisito).
  - `CouplePairing` ganhou `recordConfirmation`/`isFullyConfirmed`; a trava de `proposeDates`/`acceptDates`/
    `saveStoryDraft` virou "só depois de `CONFIRMED`" (antes era "enquanto `PENDING`"). `Couple` ganhou
    `recordDates(...)` (nasce vazio; as datas chegam depois, copiadas do `CouplePairing` quando aceitas).
  - **Duas corridas resolvidas com peças diferentes:** (1) os dois membros do mesmo pareamento confirmando ao
    mesmo tempo — resolvida com `@Lock(PESSIMISTIC_WRITE)` em `findByIdForUpdate` (mesmo padrão do
    `RefreshToken`); (2) a mesma pessoa confirmando dois pareamentos diferentes ao mesmo tempo — resolvida
    pelo índice único do banco (RN-06, já existia desde a Etapa 1) mais `CoupleFactory` (novo `@Component`)
    rodando em transação própria (`REQUIRES_NEW`) com `noRollbackFor`, para que só o nascimento do segundo
    `Couple` desfaça — a confirmação legítima da pessoa continua gravada. Nova exceção:
    `AlreadyInRelationshipException`.
  - **Primeiro teste de concorrência real do projeto** (`CouplePairingConcurrencyTest`, threads de verdade via
    `ExecutorService`+`CountDownLatch`, `@SpringBootTest` em vez de `@DataJpaTest` — um objeto criado com
    `new` nunca passa pelo proxy do Spring que aplica `@Transactional`/`@Lock`). **Verificado nesta sessão:**
    tirando a trava de propósito, o teste da primeira corrida falha de forma consistente (5 de 5) — prova de
    que o teste realmente pega o bug, não só "não dá erro".
  - 18 testes novos, 146 no total. `./gradlew build` verde. Detalhe completo em `docs/MAPA.md`.
- **Etapa 13 — concluída em 2026-11-05 (exposição HTTP).** Decisão do fundador: `@RestControllerAdvice`
  central (não `try/catch` por controller). `CouplePairingController`/`CoupleController`/`UserController`
  novos; `ProblemDetail` (do próprio Spring) para o formato de erro. **Efeito colateral descoberto no
  caminho:** a primeira versão do `GlobalExceptionHandler` (importando cada exceção de `relationships`/`users`
  direto) criou um ciclo de módulos — resolvido com uma classe-base `DuoHttpException` em `shared` (os
  módulos dependem dela, nunca o contrário) e trocando `SecurityConfig`'s dependência de `GoogleLoginSuccessHandler`
  (tipo concreto) pela interface `AuthenticationSuccessHandler` do Spring Security. 7 testes novos
  (`RelationshipsHttpTest`, via `MockMvc`, incluindo acesso cruzado RN-12/RN-20), 151 no total, build verde.
  Detalhe completo em `docs/MAPA.md`.
- **Etapa 14 — CONCLUÍDA e confirmada em 2026-11-05, pelo fundador, no terminal real dele.**
  `react-router-dom` no `package.json`; telas novas (`HandleSetupScreen`, `InviteScreen`,
  `AcceptInviteScreen`, `ConfirmScreen`, `StoryScreen`, `DateConsensusScreen`, `CoupleHomeScreen`) e
  `Dashboard.tsx` (decide pra onde mandar a pessoa ao abrir o app); `src/api.ts` novo com todas as chamadas
  HTTP do vínculo; `PairingSummary` (backend) ganhou `coupleId`, as confirmações de cada lado e a proposta de
  datas em curso — o frontend precisava deles e não existiam ainda.
  - **Descoberta importante sobre o ambiente desta sessão:** a IA roda num sandbox isolado, sem acesso ao
    sistema real do fundador (sem `sudo`, sem ver o que ele instala) — só alcança serviços de rede expostos,
    como o PostgreSQL em `localhost:5432`. O `!` do Claude Code roda **nesse mesmo sandbox**, não no terminal
    real — por isso só o fundador, numa janela de terminal de verdade (fora do Claude Code/VS Code), conseguiu
    rodar `npm install`/`npm test`/`npm run build`. Revisa a suposição anterior ("mesma máquina"); o Node.js
    precisou ser instalado de novo no sistema real (`sudo dnf install nodejs npm`, v22.23.1).
  - **Resultado:** `npm install` (507 pacotes), **15 testes passando** (`session.test.ts`, `api.test.ts`,
    `App.test.tsx`), `npm run build` (tsc + vite) sem erro. Dois avisos do React Router sobre flags da v7
    (não são erro). `npm audit` com avisos só em ferramentas de build (mesma situação já conhecida e aceita,
    seção "Estado do código").
  - Detalhe completo, inclusive uma limitação conhecida (link de convite se perde se clicado por alguém
    deslogado) e o que ficou sem teste dedicado, em `docs/MAPA.md`.
- **Próximo passo:** Etapas 13 e 14 concluídas. Falta o acabamento visual — identidade visual / protótipo da
  tela inicial (o fundador trouxe um esboço, `~/Downloads/DUO Universe.png`, só como referência; ainda não
  virou protótipo nem código) e, depois, o Bloco D (celebração em tempo real, Etapas 15 a 18, isolado de
  propósito).

### Roteiro restante do Épico 2 (Etapas 4 a 18 — plano aprovado em 2026-11-04)

> Plano completo (com o raciocínio de cada escolha) também salvo em
> `/home/leevino/.claude/plans/jaunty-knitting-pike.md` — mas o resumo abaixo já basta para retomar o
> trabalho em uma sessão nova, mesmo sem acesso a esse arquivo. Módulo `com.duo.relationships` já existe
> (Etapas 1-3: `Couple`/`CoupleMember` com RN-06 garantida no banco; `CouplePairing` com `initiate`/`cancel`/
> `find` e expiração lazy; `inviteByLink`/`redeemByLink` com código hash SHA-256). Cada etapa a seguir: anunciar
> o que vai ser feito e por quê antes de codar, confirmar qualquer decisão técnica nova com o fundador (regras
> 1 e 8), terminar com `./gradlew build` verde, changelog (regra 6) e **pausa para aprovação** antes da próxima.

**Bloco A (continuação) — ainda sem HTTP:**
- **Etapa 4 — coluna `handle` em `users`.** `V7__add_handle_to_users.sql` (nullable, índice único parcial em
  `lower(handle)`, mesma técnica das Etapas 1/3); `UserService.chooseHandle`/`findByHandle`;
  `HandleTakenException`. Decisões a confirmar: formato do handle (regex, tamanho); quando/como quem já tem
  conta (Épico 1) escolhe o handle (bloquear navegação no próximo login vs. tela opcional vs. gerar
  automático).
- ~~Etapa 5~~ **concluída em 2026-11-05** — ver seção "Onde paramos" acima para o resumo da implementação.
- **Etapa 6 — história em 4 passos (RN-25/26).** `V8__create_pairing_stories.sql`; `saveStoryDraft` (sem
  validar tamanho, rascunho pode ficar incompleto); `isStoryComplete` (4 passos, ≥20 caracteres);
  `readPartnerStory` (só devolve quando **ambas** completas); `StoryTooShortException`.
- **Etapa 7 — datas do casal: propor e aceitar (P3/RN-27).** Acrescenta a `couple_pairings`:
  `date_proposed_by_user_id`, `proposed_known_since_date`, `proposed_dating_since_date`, `dates_accepted_at`.
  `proposeDates`/`acceptDates`; `DatesNotAcceptedException`.
- **Etapa 8 — firmar e nascimento do `Couple` (RN-06/08 — a mais delicada do épico).** Colunas
  `initiator_confirmed_at`/`invitee_confirmed_at`. `confirm(pairingId, userId)`: trava a linha
  (`@Lock(PESSIMISTIC_WRITE)`, igual a `findForUpdateByTokenHash`), valida PENDING/não expirado/datas
  aceitas/história completa, grava a confirmação; quando ambos confirmaram, `CoupleFactory.createFrom(pairing)`
  com `@Transactional(propagation = REQUIRES_NEW)` (conceito novo — explicar antes de usar) cria `Couple`+
  `CoupleMember`s; se o índice único da Etapa 1 rejeitar, só essa transação nova desfaz, a confirmação
  permanece, `AlreadyInRelationshipException`. **Teste de concorrência real** (`ExecutorService`+
  `CountDownLatch`) — primeira vez nesse estilo no repositório, pedir autorização explícita antes de escrever.
- **Etapa 9 — "o que levar" (P4/RN-09), sem tela ainda.** Enum `Scope{PERSONAL,ARCHIVED,MEMBER_SIDE,
  COUPLE_CENTER}` (confirmar se `shared` é o lugar certo); interface de teste `Scopeable`, só em testes
  unitários com objetos fictícios. Sem tabela nova, sem endpoint.

**Bloco B — pausa, saída, permissões, HTTP e frontend:**
- **Etapa 10 — pausar/retomar/sair (RN-13 a 18).** `CoupleService.pause/resume/leave`; `leave` com 2 membros
  termina o casal inteiro (bulk update de `left_at`); `IllegalCoupleTransitionException`.
- **Etapa 11 — matriz de permissões por estado (RN-11/12/12a/13a).** Guard `canView`/`canPostToCouple`/
  `canPostToOwnSide`/`canPostToPersonal`. RN-13a já está totalmente decidida (2026-11-05): durante `PAUSED`,
  a pessoa também pode postar/editar no Personal, por livre escolha — modelar os dois (leitura e edição).
- **Etapa 12 — limpeza de rascunhos (RN-29) e casos de borda restantes.** Decisão a confirmar: `@Scheduled`
  vs. lazy (recomendado) para limpeza proativa; decidir se dois pareamentos `PENDING` simultâneos entre as
  mesmas duas pessoas são permitidos ou bloqueados (a spec lista o caso, não dá a regra).
- **Etapa 13 — exposição HTTP.** `CouplePairingController`/`CoupleController` (nunca tocam repositório
  direto). Decisão a confirmar: `try/catch` manual por controller (padrão atual) vs.
  `@RestControllerAdvice` central (primeira vez no projeto). Testes de acesso cruzado (RN-12/RN-20).
- **Etapa 14 — frontend: telas do vínculo.** Decisão a confirmar: estender o `useState` de `App.tsx` vs.
  `react-router` (tecnologia nova). Telas: `HandleSetupScreen`, `InviteScreen`, `AcceptInviteScreen`,
  `StoryScreen` (autosave, cuidado com StrictMode), `DateConsensusScreen`, `ConfirmScreen`,
  `CoupleHomeScreen`. Via `session.ts`/`authedFetch`; sem CSS novo além de `index.css`.

**Bloco D — P6, celebração em tempo real (isolado, por último; nada do A/B depende disto):**
- **Etapa 15 — decisão técnica: WebSocket (STOMP) vs. SSE vs. polling.** Nenhuma tecnologia de tempo real
  existe hoje; não decidir sem apresentar ao fundador (regra 8).
- **Etapa 16 — presença online.** `PresenceTracker` em memória (`ConcurrentHashMap`); só funciona numa
  única instância — registrar como nota para ADR futura, não resolver agora.
- **Etapa 17 — canal de celebração.** Ao nascer o `Couple` (fim da Etapa 8), publica evento; se ambos
  presentes, celebra; senão, marca pendente e dispara quando ambos estiverem presentes juntos depois.
- **Etapa 18 — frontend: recebendo a celebração.** Cliente do canal da Etapa 15 + animação simples em
  `index.css`.
- **Ambiente desta sessão:** a IA rodou em Windows (Git Bash); `DUO_GOOGLE_CLIENT_ID`, `DUO_GOOGLE_CLIENT_SECRET` e
  `DUO_APP_DB_PASSWORD` estão definidas lá (via `setx`; só valem para processos abertos depois). No Linux
  (Fedora, `~/.zshrc`) o fundador ainda precisa repetir as variáveis do Google. O mesmo OAuth Client serve nas
  duas máquinas. Para subir: `cd backend && ./gradlew bootRun`; se parar a tarefa do Gradle, conferir que o
  processo `java` da porta 8080 também morreu.
- **Pendência de segurança do primeiro login (resolvida em 2026-11-04):** login simultâneo com o mesmo `sub` agora
  devolve a conta criada pela outra requisição; e-mail já ligado a outro `sub` é recusado (`EmailAlreadyLinkedException`).
- **Etapa B (ADIADA para depois do MVP, decisão do fundador em 2026-11-04):** passkeys e/ou código por e-mail como
  segunda forma de entrar (RA-03, RA-06, H3, H5 dependem dela). Exige HTTPS real ou `localhost`.
- **Pendências do épico 1 ainda abertas:** RA-08 (limite de tentativas por IP/e-mail em `/api/auth/*`) — não
  implementado, precisa de decisão sobre a ferramenta (regra 8); desligar o Swagger UI em produção; tela de
  "meus aparelhos"; exclusão de conta (Q-17).

### Decisões do fundador
| Código | Decisão |
| --- | --- |
| Épico 2 (2026-11-04) | **Vínculo:** cada um conta a sua história, obrigatória, revelada ao mesmo tempo (DEC-012 revisada); perto ou longe é o mesmo fluxo; nome na interface "Couple Universe"; matriz de estados aprovada; contador de tempo juntos parte das datas do relacionamento, e o casal precisa de consenso sobre a data em que se conheceram e a em que começaram a namorar; sem prévia da história antes de os dois terminarem; tudo editável pelo dono; **sem Instagram** (sem cards nem compartilhamento externo); chat e "dar um tempo da rede" na Fase 2; universo de estrelas e planetas como ideia (`docs/IDEIAS.md`). Especificação **aprovada**: `docs/epico-2-vinculo.md` (**sem código ainda**). |
| P1-P6 / DEC-031 (2026-11-04) | **Perguntas do Épico 2 respondidas:** convite por **@handle exato** dentro do Duo como caminho padrão (reforça o Duo como rede social própria), link com código de 7 dias como alternativa — revisa o escopo do RA-07 só para este fluxo de convite (DEC-031, RN-30); história com mínimo de **20 caracteres** por passo (não só "não vazio"); consenso das datas do casal por **propor e aceitar**; "o que levar" (RN-09) modelado já na Semana 3, tela na Semana 4; Personal Universe volta em **leitura e edição/postagem** durante a `PAUSED` (revisa DEC-002, RN-13a — decidido por completo em 2026-11-05, por livre escolha da pessoa); gesto de celebração entra **já na Semana 3, em tempo real** (contra a recomendação — o fundador assumiu a peça técnica extra de presença online/WebSocket dentro das 3h/semana). Detalhe de cada pergunta: `docs/epico-2-vinculo.md`. |
| Q-22 | **Gradle (Kotlin DSL).** |
| Q-15 | **Cloudflare R2** para as mídias (exige meio de pagamento na ativação; ver Q-14). |
| Q-16 | **Confirmada:** passkeys + código de 6 dígitos por e-mail, sem senha (DEC-026 agora `DECIDIDO`). |
| Q-14 | **Fundador não tem cartão de crédito.** Oracle Always Free e R2 ficam bloqueados até resolver (cartão virtual de banco digital, pré-pago ou outra hospedagem). O deploy foi **adiado**; não criar contas nem VM por enquanto. |
| Q-13 | **3 horas por semana**, ajustável conforme o ritmo real. |
| Q-23 / DEC-030 | **Entrar só com Google no MVP** (2026-11-04). Revisa a DEC-026 e o Épico 1: passkeys e código por e-mail passam para a etapa B. Ganhos: sem serviço de e-mail, sem códigos, e-mail já verificado pelo Google (RA-01). Custo: quem não tem conta Google não entra; o Google vira ponto único de falha. RA-02 (sem senha) segue valendo; RA-03, RA-06, H3 e H5 ficam suspensos até a etapa B. |
| Sessão (passo 5) | **Decidido em 2026-11-04:** token de acesso JWT de 15 min (validado pela biblioteca `spring-boot-starter-security-oauth2-resource-server`, sem escrever criptografia na mão) + token de renovação **aleatório, não JWT**, guardado só como hash, em cookie `HttpOnly`/`Secure`/`SameSite=Strict`, rotacionado a cada uso; `family_id` é nosso (reuso de token antigo derruba toda a família). **Opção A:** após o login com Google o backend só define o cookie e redireciona ao front; o front chama `/api/auth/refresh` e recebe o token de acesso no JSON. Nenhum token vai em URL (RA-09). O cookie é definido em todo login com Google e a cada rotação. |
| Docker | **Adiado para outra fase.** Ferramentas instaladas direto no Windows. Consequência: Testcontainers (DEC-018) fica fora por ora e os testes usam um Postgres local (`duo_test`). |
| Git | Repositório privado `https://github.com/AbnerLevino/Duo-Universe.git`, branch `main`. **A cada requisição atendida: commit e push** (autorização durável só para este repositório). |
| Etapa 4 (2026-11-05) | **Handle:** formato `3 a 20` caracteres, letras/números/`_` (`^[A-Za-z0-9_]{3,20}$`), comparado sem diferenciar maiúsculas. Quem já tem conta escolhe numa **tela obrigatória no próximo login** (navegação bloqueada até escolher) — essa tela entra na Etapa 14. |
| Q-17 (2026-11-05) | **Centro do casal no término:** nunca é apagado (RN-17/18 inalteradas); cada ex-membro escolhe individualmente esconder da própria visão, podendo desfazer depois; a escolha de um não afeta o outro (RN-17a). |
| Q-11/Q-12 (2026-11-05) | **Pergunta do dia:** o app escolhe as perguntas (curadoria própria); a resposta do parceiro aparece assim que ele responder, **sem** esperar os dois (ao contrário do formato da história do vínculo, DEC-012); a pergunta **nunca expira** — sem marca de "atrasada", só um contador de dias (mecânica exata a detalhar na Semana 5). Perguntas entre o casal com jogos interativos ficam para a Fase 2 (`docs/IDEIAS.md`). |
| RA-08 (2026-11-05) | **Limite de tentativas:** tabela própria no Postgres (padrão de `email_login_codes.attempts`), a implementar na Etapa 5. Esclarecido: diferente da RN-30 (busca só por @handle exato — nunca há lista de resultados a paginar); RA-08 limita quantas tentativas por período, não o que a busca devolve. |
| Organização/idioma (2026-11-05) | Reorganizar pastas por camada técnica (`exception/`/`model/`/`repository/`/`service/`) e traduzir o código para português foram as duas propostas do fundador; as duas ficaram **sem aplicar** — optou por reforçar `docs/GLOSSARIO-TECNICO.md`/`docs/MAPA.md` em vez disso. DEC-028 (código em inglês) continua valendo. |
| RN-25 (2026-11-05) | **Mínimo de 20 caracteres revertido.** Os 4 passos da história exigem só texto não vazio (depois de remover espaços), sem tamanho mínimo — revisa P2/DEC-031 (2026-11-04), ver `docs/epico-2-vinculo.md`. Motivo do fundador: o mínimo de caracteres não garante resposta de verdade e pune quem responde curto e sincero; a obrigatoriedade sozinha já garante o ritual. |
| Etapa 6 (2026-11-05) | **Demonstração simples, sem tela nova.** A Etapa 6 (história em 4 passos) fica só no backend — lógica funcionando, testes provando; apresentação em texto corrido (sem UI), já que a tela real do vínculo é a Etapa 14. Telas elaboradas ficam para depois de o fluxo todo avançar. |
| Etapa 9 (2026-11-05) | **`Scope` (`PERSONAL`/`ARCHIVED`/`MEMBER_SIDE`/`COUPLE_CENTER`, P4/RN-09) mora em `shared` desde já** — não é regra do vínculo, é algo que fotos (Semana 4) e memórias (Semana 5) também vão usar. |
| Etapa 12 (2026-11-05) | **Limpeza de rascunho continua lazy** (sem `@Scheduled`). **Mais de um `CouplePairing` `PENDING` por pessoa, ao mesmo tempo: permitido**, sem índice único bloqueando — o fundador quer deixar essa porta aberta para um fluxo futuro ("pedido de namoro" entre pessoas Personal que se conheceram organicamente dentro do Duo, registrado em `docs/IDEIAS.md`; fora do MVP). RN-06 continua valendo — só é verificada na hora de **firmar** (Etapa 8), não na hora de convidar; ver "casos de borda" em `docs/epico-2-vinculo.md`. |
| Etapa 8 (2026-11-05) | **Esclarecimento, sem mudar o desenho da etapa:** o vínculo de hoje (DEC-012) não é um "pedido de namoro" — pressupõe que o casal já namora. Um fluxo de pedido de namoro de verdade (gesto fofo, para perto ou longe) é ideia para depois, registrada em `docs/IDEIAS.md`, não faz parte do MVP. |
| Etapa 8 — ordem do fluxo (2026-11-05) | **O vínculo firma primeiro; história e datas vêm depois.** Revisa a ordem original do fluxo (`docs/epico-2-vinculo.md`): antes a história/datas eram pré-requisito para firmar; agora firmar é o primeiro passo depois do convite aceito, e o `Couple` nasce vazio — história e datas são atividades de dentro do Couple Universe já nascido. Motivo do fundador: a história é "sobre nós", só faz sentido perguntar depois que o vínculo existe de verdade. |

### Princípios reafirmados pelo fundador
- **Pressão e conflito** são assunto do casal: o app dá os meios (privar, excluir, editar) e o casal conversa.
- **Exclusão:** existe a do casal e a do Personal; o app **sempre avisa antes** que dados não salvos/tratados serão apagados.
- O Duo é, ao mesmo tempo, **galeria na nuvem** (liberar espaço do celular), **rede social de vínculo** e **Personal Universe**.
- A segurança do CPF segue a RN-05; o desenho do segredo e da rotação será proposto quando chegarmos ao módulo `verification`.

### Verificação de idade por CPF (pesquisa de 2026-11-03)
Existem fontes, **todas pagas por consulta** (Serpro Consulta CPF, Didit ~US$ 0,20, BigData Corp a partir de R$ 0,05,
Infosimples, Hub do Desenvolvedor). Nenhum plano gratuito confirmado; verificar preços nas páginas oficiais antes de
contratar. Só é obrigatória antes de abrir ao público; o módulo fica atrás de uma interface. Exceção consciente ao
custo zero, a decidir perto do lançamento.

### Ambiente de desenvolvimento (Linux, sem Docker por ora)

> **2026-11-03:** o fundador trocou de Windows para Linux (Fedora) **no mesmo notebook**. Este bloco substitui o
> antigo ("Ambiente de desenvolvimento (Windows, sem Docker)"); nenhuma decisão de produto ou arquitetura mudou,
> só o sistema operacional de desenvolvimento.

- Notebook: i5-1035G1, mesma máquina de antes. RAM visível ao SO confirmada em ~7 GB — e já apertada no dia a
  dia (`free -h` mostrou só ~150 Mi livres e ~2,8 Gi de swap em uso em uso normal, sem nada do projeto rodando
  ainda). RAM continua o fator limitante, agora com um número concreto. Fedora Linux (`fc44`); disco com bastante
  espaço livre (~454 GB).
- Instalados: **OpenJDK 25** (não é o 21 que a DEC-018 pede — falta instalar o 21 à parte; múltiplos JDKs convivem
  bem no Linux), **Node.js 22.23.1** (a seção anterior tinha Node 26 LTS no Windows; por ora mantido, sem
  problema conhecido com o `package.json` atual), **Git 2.56.0**. Gradle continua só pelo wrapper (9.8.2), sem
  mudança.
- **JDK 21: resolvido sem instalar nada no sistema.** O Fedora 44 não empacota mais nenhum `java-21-openjdk`
  (só 25 e um 27 em early access via `dnf`), então adicionamos o plugin oficial do Gradle
  `org.gradle.toolchains.foojay-resolver-convention` (versão 1.0.0, confirmada no Gradle Plugin Portal em
  2026-11-03) em `backend/settings.gradle.kts`. Ele deixa o próprio Gradle baixar o JDK que falta, sem `sudo`:
  `./gradlew compileJava` já baixou um **Eclipse Temurin 21.0.13** (mesma build do Windows) para
  `~/.gradle/jdks/`, isolado do JDK 25 do sistema.
- **Docker/containers:** não instalado; o Fedora traz **Podman** em vez do Docker Engine, sem plugin de compose
  configurado. A decisão de não usar containers por ora (seção 9) continua valendo, mas o motivo mudou: antes era
  a fricção do Docker no Windows, agora é a RAM apertada (acima) e evitar empilhar containers como conceito novo
  enquanto o fundador aprende JPA/Hibernate.
- **JDK 21 e PostgreSQL: os dois já resolvidos**, sem `sudo` para o JDK (toolchain do Gradle, acima) e com
  `sudo dnf install postgresql-server postgresql-contrib` (18.6) para o PostgreSQL — serviço instalado,
  inicializado e confirmado funcionando de ponta a ponta (ver bullet abaixo). Não há mais pendência de ambiente
  bloqueando o Épico 1.
- Bancos previstos (sem mudança): `duo` (desenvolvimento) e `duo_test` (testes), dono `duo_app`.
- Segredos em variáveis de ambiente do usuário Linux (substitui o `setx` do Windows): `DUO_DB_PASSWORD`
  (superusuário `postgres`) e `DUO_APP_DB_PASSWORD` (aplicação) — por exemplo em `~/.bashrc` ou `~/.profile`.
  Nunca no repositório.
- Rodar o build: `./gradlew build` em `backend/` (sem `.bat`); só precisa definir `JAVA_HOME` manualmente se o
  Gradle não achar um JDK 21 por conta própria.
- Se a RAM apertar: fechar abas do navegador antes de builds pesados; IDE leve (VS Code).
- **Correção importante (descoberta tentando configurar o Postgres):** o shell interativo do terminal do
  fundador é **`zsh`**, não `bash` — mesmo o `/etc/passwd` listando `/bin/bash` como shell da conta (algum
  app de terminal está sobrepondo isso). Por isso: `~/.bashrc` **nunca** é lido automaticamente; as variáveis
  `DUO_DB_PASSWORD`/`DUO_APP_DB_PASSWORD` do fundador devem ir no **`~/.zshrc`**. O Gradle, quando chamado
  pela IA (que roda em `bash` puro), ainda precisa que as variáveis sejam carregadas manualmente nesse `bash`
  a cada sessão nova (shell state não persiste entre chamadas de ferramenta).
- **PostgreSQL local, confirmado funcionando de ponta a ponta em 2026-11-03:** serviço ativo, `pg_hba.conf`
  corrigido (`ident` → `scram-sha-256` nas linhas `host`), role `duo_app` autenticando por senha via rede,
  bancos `duo`/`duo_test` criados, `UserRepositoryTest` rodando contra `duo_test` de verdade.

### Estado do código
- `backend/`: Spring Boot **4.1.2**, Java 21, Gradle Kotlin DSL, com Web (`webmvc`), Security, Data JPA, Validation,
  Flyway, PostgreSQL, Actuator. Pacote raiz `com.duo`.
- `V1__create_users.sql`: tabela `users` com índice único em `lower(email)` (RA-01).
- `shared/SecurityConfig`: tudo autenticado (RN-20), exceto `/actuator/health`.
- Testes (8, passando): contexto, saúde pública, rota protegida, 3 regras ArchUnit, 2 da documentação da API.
  Perfil `test` aponta para `duo_test`.
- Módulos criados (só `package-info`): `auth`, `users`; `shared` tem `SecurityConfig`. Convenção: API pública do módulo na
  raiz do pacote, implementação em `com.duo.<modulo>.internal` (decisão aguardando confirmação do fundador).
- `ArchitectureTest` (ArchUnit 1.5.1): sem ciclos entre módulos, controller sem acesso a `Repository`, `internal` privado
  ao módulo. Spotless 8.11.0 (só imports, espaços finais e newline; preserva tabs). Springdoc 3.2.0 (POM sobre Boot 4.1.2),
  protegido por autenticação (RN-20); desligar o Swagger UI em produção quando houver deploy.
- `frontend/`: Vite 8.3.5, React 19.3.1, **TypeScript 6.0.3** (o `typescript-eslint` exige `<6.1.0`; não usar o 7),
  ESLint 10, Prettier, Vitest 5. PWA com `vite-plugin-pwa` (manifest + service worker **sem cache da API**), proxy
  `/api` → `localhost:8080` no dev, ícones **provisórios**. Node 26 local e no CI. `npm audit` acusa falhas só em
  ferramentas de build (workbox/ESLint); não aplicar `--force` (faz downgrade). Reavaliar depois.
- CI (`.github/workflows/ci.yml`): jobs `backend` (Postgres como service) e `frontend`. `gh` instalado e
  autenticado pelo fundador em 2026-11-05; ainda falta um deste lado conferir a aba Actions para o verde
  (meu shell não alcança o `gh` do sistema real — ver "Pendências abertas").
- `gradlew` versionado com permissão de execução (runner Linux).
- Notas técnicas do Boot 4: starters renomeados (`spring-boot-starter-webmvc`, `-flyway`); em testes, um
  `application.properties` em `src/test/resources` **substitui** o principal, por isso usamos `application-test.properties`
  com `@ActiveProfiles("test")`. As anotações de test slice também mudaram de pacote (ver
  `docs/GLOSSARIO-TECNICO.md`, entrada `@DataJpaTest`) — confira o pacote real pelo `.jar` antes de copiar de
  tutorial antigo.
- `users/internal/User.java` + `UserRepository.java`: primeira entidade/repositório do projeto (Épico 1,
  passo 1 — CRUD básico, implementado direto sem modo acompanhamento, acordo de 2026-11-03). `UserRepositoryTest`
  (`@DataJpaTest`) roda contra o `duo_test` real. Build completo (`./gradlew build`) passando: 10/10 testes,
  Spotless e ArchUnit ok.
- **Este bloco está defasado desde a Semana 1** (contagem de testes e lista de módulos); para o estado atual,
  ver "Onde paramos / próximo passo" no topo desta seção 19. **Atualização rápida de 2026-11-05:** build verde
  com 83 testes; migrations até `V8`; módulo novo `shared` (além de `ClockConfig`/`SecurityConfig`, agora tem
  `RateLimiter`); convenção `internal` (linha acima, "decisão aguardando confirmação") está **confirmada** e
  em uso em todo módulo desde o Épico 2.

### Plano ajustado
1. **Semana 1: CONCLUÍDA** (etapas 1 e 2: backend, módulos, ArchUnit, Spotless, Springdoc, frontend PWA, CI). Falta só
   confirmar o CI verde no GitHub. **Sem Docker.**
2. **Modo de trabalho:** formalizado em 2026-11-03 como "Modo acompanhamento" (regra 5 e seção dedicada após a
   regra 13) — o fundador é tratado como estagiário em formação, não como par técnico. Detalhes: passos pequenos,
   um conceito por vez, explicação antes e depois de cada passo, perguntas de verificação, `docs/MAPA.md` e
   `docs/GLOSSARIO-TECNICO.md` mantidos sempre atualizados. Esta nota permanece como registro histórico do pedido.
3. **Épico 1, etapa A (em andamento): passo 1 concluído** — entidade `User` + `UserRepository` (2026-11-03).
   **Passo 2 (próximo):** entrada por **código de e-mail** (RA-01 a RA-05, RA-07 a RA-09) — aqui já é lógica
   concreta do Duo, não CRUD genérico, então volta o Modo acompanhamento (versão leve, seção 2). Para ver o
   e-mail em desenvolvimento sem logar o código, propor o **Mailpit** (precisa de ok). **Etapa B:** passkeys,
   só quando houver HTTPS real ou `localhost` (WebAuthn exige contexto seguro; verificar o suporte do Spring
   Security na versão usada).
4. **Marco de infraestrutura (adiado):** deploy, Caddy/HTTPS, backups e passkeys em dispositivo real, depois de
   resolver o cartão (Q-14) ou escolher outra hospedagem.
5. Semanas 3 a 6 como na seção 17, ajustadas pela disponibilidade.

### Pendências abertas
- ~~Q-23~~ resolvida: DEC-030 (só Google).
- Ok para **ArchUnit** e **Spotless**; ok para o **Mailpit** (desnecessário com a DEC-030).
- **Q-18:** serviço de e-mail transacional gratuito (pesquisar quando o envio real for necessário). "Transacional"
  aqui é termo do mercado para e-mail automático disparado por uma ação do sistema (código de login, confirmação),
  diferente de "transação" no sentido de banco de dados/concorrência (DEC-005, Etapa 8) — os dois usos convivem
  no projeto, sem relação entre si.
- ~~Q-17~~ resolvida em 2026-11-05: ver RN-17a (seção 8). Cada ex-membro esconde o centro do casal só da
  própria visão; o dado nunca é apagado.
- **Q-06:** matriz de permissões por estado do vínculo (o que cada pessoa pode fazer em `ACTIVE`, `PAUSED`, `ENDED`);
  confirmar se a pausa é do vínculo do casal, do Personal Universe ou de ambos. **Atualização 2026-11-05:** a parte
  do Personal já foi respondida (RN-13a, acima); falta só o resto da matriz, que a Etapa 11 do Épico 2 cobre.
- ~~RA-08~~ resolvida em 2026-11-05: tabela própria no Postgres (mesmo padrão de `email_login_codes.attempts`),
  a implementar quando chegarmos na Etapa 5. Esclarecido ao fundador: RA-08 (limite de tentativas) e RN-30
  (busca só por @handle exato, sem listagem) são regras diferentes — RN-30 decide o que a busca pode devolver
  (nunca uma lista), RA-08 decide quantas vezes alguém pode tentar por período.
- ~~Q-11/Q-12~~ decididas em 2026-11-05 (ver seção 18): pergunta do dia sem ocultar a resposta do parceiro
  (aparece assim que ele responder) e sem expiração (contador de dias, não "atrasada"). O desenho exato do
  contador de dias fica para o detalhamento da Semana 5.
- **Q-20** (cotas de armazenamento): o fundador quer decidir junto quando chegarmos ao módulo de storage
  (Semana 4), por causa do impacto na monetização futura (DEC-022) — não é uma pendência esquecida, é intencional.
- **CI do GitHub:** o fundador se autenticou no `gh` em 2026-11-05. Ainda não confirmado **verde** na aba Actions
  — meu shell roda num sandbox isolado sem acesso ao `gh` instalado no sistema real; preciso que o fundador rode
  o `gh` pelo prefixo `!` no terminal do Claude Code, ou confirme manualmente pela aba Actions do repositório.
