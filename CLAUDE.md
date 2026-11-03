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
- **No término:** o que cada um postou no próprio lado e o que estava guardado volta para a pessoa.
  O destino do centro do casal está em aberto (Q-17).

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
| Momento do vínculo | `CouplePairing` | Momento especial em que o vínculo nasce. **Não é "convite"**. Nome final em aberto (Q-10). |
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
| C-05 Término e memórias | **Parcial:** lado de cada um e arquivados voltam à pessoa; centro do casal em aberto (Q-17) |
| C-06 Custo do armazenamento | **Encaminhado:** fotos comprimidas sem vídeo no gratuito; cotas em aberto (Q-20) |
| C-07 Hospedagem sem custo | **Resolvido:** Oracle Cloud Always Free (DEC-025) — **revisto na seção 19: o fundador não tem cartão** |
| C-08 Escopo do MVP | **Encaminhado:** seção 11 |

## 7. Registro de decisões (ADRs)

Status: `DECIDIDO`, `PROPOSTA`, `EM ABERTO`, `REVISADA` (vale, com mudança), `SUBSTITUÍDA` (histórico).

### DEC-001 — Foco inicial · `REVISADA`
MVP com o universo do solteiro (mínimo) e o Couple Universe. A parte social dos solteiros (descobrir pessoas,
comunidades) fica no horizonte.

### DEC-002 — Do "eu" ao "nós" · `DECIDIDO` (revisada em 2026-11-03)
Solteiro tem Personal Universe; ao se vincular, deixa de existir um "eu" separado e tudo vive no Couple Universe
(centro do casal + lado de cada um). No vínculo, a pessoa escolhe o que leva; o resto fica guardado e volta no
término. Login sempre individual; autoria sempre rastreável. (A v3 propunha manter o Personal Universe permanente;
o fundador decidiu pela visão original.)

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

### DEC-012 — O vínculo nasce por um momento especial, não por convite · `DECIDIDO` (formato `EM ABERTO`)
Opções: pedido; gesto simultâneo; **pergunta secreta** (cada um responde uma pergunta sobre o outro sem ver a
resposta, e o universo nasce quando os dois acertam); "nascimento do universo" como celebração.
Backend genérico: proposta + confirmação de todos.

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
- **RN-03:** bloqueio temporário após 2 tentativas que não batem (duração: Q-07).
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
- **RN-11:** qualquer membro edita o conteúdo do casal; exclusões vão para a lixeira (Q-04).
- **RN-12:** só membros atuais acessam o Couple Universe.
- **RN-12a:** o lado de cada um é visível ao parceiro; só o dono posta e apaga no próprio lado.

**Pausa, saída e retorno**
- **RN-13:** qualquer membro pausa sozinho; nada é apagado (quem retoma e o que se pode fazer na pausa: Q-02, Q-06).
- **RN-14:** qualquer membro sai sozinho, a qualquer momento.
- **RN-15:** ao sair, a pessoa volta ao Personal Universe com o que postou no próprio lado e o que estava guardado.
- **RN-16:** sem alardes ou notificações públicas sobre pausa ou término.
- **RN-17:** o término nunca apaga as memórias de ninguém.
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
(*Revisto na seção 19: por ora tudo instalado direto no Windows, sem Docker.*)

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

**Antes da semana 3:** Q-01 formato do momento do vínculo; Q-05 presencial ou à distância; Q-10 nome oficial;
Q-02 quem retoma a pausa; Q-06 o que se pode fazer na pausa (matriz de permissões por estado);
identidade visual e design system mínimo.

**Antes da semana 4:** Q-04 prazo da lixeira; Q-17 destino do centro do casal no término; Q-19 exclusão em massa;
Q-20 cotas, tamanho máximo e compressão das fotos.

**Antes da semana 5:** Q-11 quem escreve as perguntas do dia, se a resposta do par aparece antes de responder e se dá
para responder atrasado; Q-12 temas sensíveis nas perguntas; datas especiais no MVP mínimo ou completo.

**Antes de abrir ao público:** Q-07 duração do bloqueio da verificação; Q-21 verificação de idade antes do piloto;
domínio próprio; termos de uso e política de privacidade revisados.

---

## 19. Progresso e decisões posteriores à v4 (2026-11-03)

> Esta seção prevalece sobre o texto acima onde houver conflito. Atualize-a a cada etapa concluída.

### Decisões do fundador
| Código | Decisão |
| --- | --- |
| Q-22 | **Gradle (Kotlin DSL).** |
| Q-15 | **Cloudflare R2** para as mídias (exige meio de pagamento na ativação; ver Q-14). |
| Q-16 | **Confirmada:** passkeys + código de 6 dígitos por e-mail, sem senha (DEC-026 agora `DECIDIDO`). |
| Q-14 | **Fundador não tem cartão de crédito.** Oracle Always Free e R2 ficam bloqueados até resolver (cartão virtual de banco digital, pré-pago ou outra hospedagem). O deploy foi **adiado**; não criar contas nem VM por enquanto. |
| Q-13 | "Bastante tempo" (número exato não informado). |
| Docker | **Adiado para outra fase.** Ferramentas instaladas direto no Windows. Consequência: Testcontainers (DEC-018) fica fora por ora e os testes usam um Postgres local (`duo_test`). |
| Git | Repositório privado `https://github.com/AbnerLevino/Duo-Universe.git`, branch `main`. **A cada requisição atendida: commit e push** (autorização durável só para este repositório). |

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

### Ambiente de desenvolvimento (Windows, sem Docker)
- Notebook: i5-1035G1, **7,8 GB de RAM** (limitante), disco C: com bastante espaço.
- Instalados: **JDK Temurin 21.0.13**, **PostgreSQL 16.15** (serviço Windows, porta 5432), **Node.js 26.13.0 (LTS)**,
  Git. Gradle vem pelo wrapper (9.8.2).
- Bancos: `duo` (desenvolvimento) e `duo_test` (testes), dono `duo_app`.
- Segredos só em variáveis de ambiente do usuário Windows: `DUO_DB_PASSWORD` (superusuário `postgres`) e
  `DUO_APP_DB_PASSWORD` (aplicação). Nunca no repositório.
- Rodar o build: definir `JAVA_HOME` e `DUO_APP_DB_PASSWORD` no processo e executar `.\gradlew.bat build` em `backend/`.
- Se a RAM apertar: Postgres pode passar para início manual; fechar abas do navegador; IDE leve (VS Code).

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
- CI (`.github/workflows/ci.yml`): jobs `backend` (Postgres como service) e `frontend`. **Ainda não confirmado verde no
  GitHub** (o `gh` não está instalado; conferir na aba Actions).
- `gradlew` versionado com permissão de execução (runner Linux).
- Notas técnicas do Boot 4: starters renomeados (`spring-boot-starter-webmvc`, `-flyway`); em testes, um
  `application.properties` em `src/test/resources` **substitui** o principal, por isso usamos `application-test.properties`
  com `@ActiveProfiles("test")`.

### Plano ajustado
1. **Semana 1: CONCLUÍDA** (etapas 1 e 2: backend, módulos, ArchUnit, Spotless, Springdoc, frontend PWA, CI). Falta só
   confirmar o CI verde no GitHub. **Sem Docker.**
2. **Modo de trabalho:** formalizado em 2026-11-03 como "Modo acompanhamento" (regra 5 e seção dedicada após a
   regra 13) — o fundador é tratado como estagiário em formação, não como par técnico. Detalhes: passos pequenos,
   um conceito por vez, explicação antes e depois de cada passo, perguntas de verificação, `docs/MAPA.md` e
   `docs/GLOSSARIO-TECNICO.md` mantidos sempre atualizados. Esta nota permanece como registro histórico do pedido.
3. **Épico 1, etapa A (em andamento; passo 1 = entidade `User` em `users/internal`, a cargo do fundador):** entrada por **código de e-mail** (RA-01 a RA-05, RA-07 a RA-09), localmente. Para ver o e-mail
   em desenvolvimento sem logar o código, propor o **Mailpit** (precisa de ok). **Etapa B:** passkeys, só quando houver
   HTTPS real ou `localhost` (WebAuthn exige contexto seguro; verificar o suporte do Spring Security na versão usada).
4. **Marco de infraestrutura (adiado):** deploy, Caddy/HTTPS, backups e passkeys em dispositivo real, depois de
   resolver o cartão (Q-14) ou escolher outra hospedagem.
5. Semanas 3 a 6 como na seção 17, ajustadas pela disponibilidade.

### Pendências abertas
- **Q-23:** login com Google no MVP? (antes da etapa A do épico 1)
- Ok para **ArchUnit** e **Spotless**; ok para o **Mailpit**.
- **Q-18:** serviço de e-mail transacional gratuito (pesquisar quando o envio real for necessário).
- **Q-17 e exclusão de conta:** o que acontece com o centro do casal e com fotos que mostram o parceiro quando uma
  pessoa exclui a conta (conflito entre LGPD e RN-17/RN-18).
- **Q-06:** matriz de permissões por estado do vínculo (o que cada pessoa pode fazer em `ACTIVE`, `PAUSED`, `ENDED`);
  confirmar se a pausa é do vínculo do casal, do Personal Universe ou de ambos.
- Preencher a **Q-13** com um número de horas por semana.
