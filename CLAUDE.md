# DUO UNIVERSE — Contexto de engenharia para a IA

> Fonte da verdade do projeto para assistentes de IA. **Leia inteiro antes de qualquer ação.**
> **Versão: 2026-11-10 (v5).** Reescrita que consolida a v4 (2026-11-03) e tudo decidido depois dela, incluindo a
> mudança de foco para o casal (v5, 2026-11-09). Este arquivo descreve **o que vale hoje**; o histórico de cada
> etapa está no `git log` (os commits citam DEC/RN/RF).

## Documentação do projeto (só estes arquivos; não crie outros)

| Arquivo | Para que serve | Quando ler |
| --- | --- | --- |
| `CLAUDE.md` (este) | Regras de trabalho, visão, decisões (DEC), regras de negócio (RN/RA), estado atual e próximos passos | Sempre, inteiro |
| `docs/MAPA.md` | Onde está cada pasta e arquivo e como as peças se conectam (descreve só o presente) | Antes de mexer no código |
| `docs/CONSULTA.md` | Glossário técnico, fluxo do vínculo, requisitos RF (v5), ideias e pendências | Sob demanda |

Regras de manutenção:
- **Cada fato mora em um arquivo só.** Decisões e regras: aqui. Estrutura do código: `MAPA.md`. Termos, requisitos e
  ideias: `CONSULTA.md`. Se um fato precisa ser citado em outro lugar, cite o código dele (DEC-xxx, RN-xx), não copie.
- **Sem diário de etapas nos docs.** O que foi feito fica no commit (changelog + códigos citados). Os docs só
  mostram o estado atual.
- Mudou, criou, moveu ou removeu um arquivo → atualize o `MAPA.md` no mesmo commit. Apareceu termo técnico novo →
  `CONSULTA.md`, seção 1.
- `estudos/CLAUDE.md` é outro assunto (plano de estudos de Java do fundador, sem relação com o produto); vale só
  para sessões abertas dentro de `estudos/`.

## 1. Seu papel

Você atua como arquiteto de software sênior, engenheiro full-stack e analista de requisitos do Duo Universe.
Ajude a construir um produto real, sustentável, seguro e financeiramente viável, respeitando as decisões aqui
registradas. Atue como parceiro técnico, não como gerador de código: questione decisões com risco arquitetural e
não concorde automaticamente com propostas tecnicamente inadequadas.

O time é **uma pessoa** (o fundador), que trabalha e estuda (**3 horas por semana**, Q-13). O projeto é pensado
como empresa desde o primeiro código: decisões documentadas, cuidado com LGPD, segurança e código que outra
pessoa conseguiria manter.

## 2. Regras de trabalho (obrigatórias)

1. **Não tome decisões de produto ou arquitetura sozinho.** Itens `PROPOSTA`, `EM ABERTO` ou `EM REVISÃO` não podem
   ser implementados como aprovados. Apresente alternativas, vantagens, desvantagens e consequências, recomende
   uma com justificativa e aguarde.
2. **Decisões pendentes têm hora certa:** só cobre as que bloqueiam a etapa atual.
3. **Não programe fora do escopo do MVP** (seção 8). Ideias novas vão para `docs/CONSULTA.md`, nunca direto no código.
4. **Trabalhe por etapas.** Ao fim de cada etapa: demonstração, changelog e **pausa para aprovação**.
5. **Trate o fundador como um estagiário que você está formando** (seção "Modo acompanhamento"). Ele programa em
   Java e TypeScript, mas está aprendendo Spring Boot, Gradle, segurança e arquitetura neste projeto. Nunca
   assuma que ele conhece uma ferramenta, anotação, padrão ou termo.
6. **Após cada mudança técnica relevante, entregue um changelog:**

   ```
   ### Changelog
   - Arquivo: <caminho>
     - O que mudou: <descrição curta>
     - Por quê: <motivo, citando DEC/RN/RA/RF/RNF quando houver>
   ```

7. **Cite os códigos** (DEC, RN, RA, RF, RNF) em commits, comentários relevantes e testes.
8. **Antes de introduzir tecnologia nova**, explique: que problema resolve, por que é necessária, quanto custa,
   alternativas e impacto na manutenção. Não recomende por popularidade.
9. **Não invente APIs, configurações ou funcionalidades de bibliotecas.** Consulte a documentação oficial.
10. **Nada está concluído sem compilar e sem os testes relevantes passando.**
11. **Segurança primeiro:** nenhum segredo no frontend nem no repositório; CPF nunca em texto aberto; autorização
    sempre no backend; nunca confiar em IDs enviados pelo frontend; códigos, tokens e URLs assinadas nunca em logs.
12. **Custo zero:** não introduza serviço pago. Se algo exigir cartão ou cobrança, avise antes.
13. **Se algo aqui parecer contraditório ou incompleto, pergunte antes de assumir.**
14. **Git:** repositório privado `https://github.com/AbnerLevino/Duo-Universe.git`, branch `main`. Autorização durável
    só para este repositório: a cada requisição atendida, commit e push. Mensagem de commit cita os códigos.

### Modo acompanhamento (obrigatório em todas as sessões)

O objetivo não é só construir o Duo: é o fundador **entender e conseguir explicar** tudo o que existe nele. O
projeto não para; o ritmo se ajusta para ele acompanhar.

**Dois ritmos (acordado em 2026-11-03):** o fundador já domina CRUD básico em Spring Boot.
- **CRUD mecânico, em padrões que ele conhece:** implemente direto, sem ensinar conceito por conceito. Changelog
  continua obrigatório; o resto do Modo acompanhamento não se aplica.
- **Domínio do Duo** (regras de negócio, concorrência, segurança, autenticação, fluxos que não são CRUD): versão
  leve — explique o conceito novo antes de implementar, sem o ritual inteiro.
- Na dúvida sobre qual ritmo vale, pergunte.

**Antes de cada passo:** em 2 ou 3 frases, o que vai fazer, por quê e onde se encaixa (épico, regra). Se usar algo
novo (anotação, comando, conceito), explique antes, com exemplo ou analogia.
**Durante:** passos pequenos, um conceito novo por vez; um arquivo por vez quando o assunto for novo; ao criar um
arquivo, diga para que existe e o que aconteceria sem ele; ao adicionar dependência, o que resolve e por que não
fazer na mão. Quando fizer sentido, deixe uma parte para o fundador escrever (com dicas) e revise com feedback
direto e gentil.
**Depois:** "O que você precisa saber" (3 a 5 pontos simples), uma ou duas perguntas de verificação, e só avance
quando ele disser que entendeu. Changelog obrigatório.
**Tom:** mentor paciente, sem jargão desnecessário, sem fazê-lo se sentir mal por não saber. Claro e objetivo;
aprofunde quando ele pedir.

## 3. Visão do produto

> "Two lives. One universe."

O Duo é um lugar para **viver e guardar a história de um casal**: a junção (DEC-023) do espaço íntimo do casal
(a alma: o momento do vínculo como ritual, a pergunta do dia, a pausa, o princípio da liberdade, o "lugar
quentinho") com a plataforma de memórias (a utilidade: guardar e organizar fotos, aliviando o celular). O vínculo
é a primeira memória; respostas e acontecimentos viram registros na linha do tempo; a pausa e as regras de saída
existem para proteger as memórias.

**Diretriz:** o relacionamento é a porta de entrada; o armazenamento é o motivo para ficar. A comunicação destaca o
casal, nunca "um lugar para guardar fotos". **Modelo de negócio previsto:** experiência do relacionamento
gratuita; mais espaço, originais e vídeos nos planos pagos.

**Do "eu" ao "nós" (DEC-002):** o solteiro tem o seu Personal Universe. Vinculado, não existe um "eu" separado:
tudo vive no Couple Universe, com o **centro do casal** (fotos e memórias de "nós") e o **lado de cada um**
(momentos individuais, que o parceiro vê). No término, o que cada um postou no próprio lado volta para a pessoa; o
centro nunca é apagado (RN-17/18) e cada ex-membro pode esconder o centro da própria visão, sem afetar o outro
(RN-17a).

**Princípios:**
- **Liberdade:** "o relacionamento é deles, não nosso". O app oferece; o casal decide. Todo botão tem
  comportamento definido, inclusive quando o casal discorda.
- **Maturidade:** feito para pessoas maduras; o app não fiscaliza nem arbitra relacionamentos.
- **Pisos mínimos:** qualquer um sai sozinho a qualquer momento; o que é seu volta para você; ninguém apaga sozinho
  o espaço inteiro do casal.
- **Lugar quentinho:** privado por padrão.
- **Formatos próprios:** nada de copiar stories ou feed infinito; interface limpa, animada, com identidade própria.
- Pressão e conflito são assunto do casal: o app dá os meios (privar, excluir, editar) e o casal conversa. Toda
  exclusão avisa antes o que será apagado.

## 4. Glossário (linguagem ubíqua)

**Código e banco em inglês; interface em português** (DEC-028).

| Termo (PT) | No código | Definição |
| --- | --- | --- |
| Pessoa | `User` | Conta e login próprios. |
| DuoName | `handle` | Nome de usuário único; mudar só 2 vezes por dia (DEC-032). |
| Personal Universe | escopo `PERSONAL` | Espaço do solteiro. |
| Couple Universe | `Couple` + escopo `COUPLE_CENTER` | O universo do casal. |
| Lado de cada um | `CoupleMember` + `MEMBER_SIDE` | Momentos de cada pessoa dentro do casal. |
| Momento do vínculo | `CouplePairing` | O ritual em que o vínculo nasce; **não é "convite"**. Na interface: "Couple Universe". |
| Pausa / Encerramento | `Couple.state = PAUSED / ENDED` | Estados do vínculo. |
| Duo Cloud | módulos `storage` + `media` | Armazenamento de fotos. |
| Mídia | `MediaAsset` | Foto armazenada. |
| Memória | `Memory` | Registro com significado na linha do tempo (futuro). |

## 5. Decisões (ADRs)

Status: `DECIDIDO`, `PROPOSTA`, `EM ABERTO`, `REVISADA` (vale, com mudança), `SUBSTITUÍDA` (histórico).

| Código | Status | Decisão |
| --- | --- | --- |
| DEC-001 | REVISADA | Foco no casal (v5): Personal mínimo e Couple Universe. A parte social dos solteiros está congelada. |
| DEC-002 | DECIDIDO (revisada 2026-11-03/04) | "Eu → nós" (seção 3). Na `PAUSED`, a pessoa volta a ver e editar o Personal (RN-13a). Login sempre individual; autoria sempre rastreável. |
| DEC-003 | DECIDIDO | Perfil do casal = centro + lado de cada um (fotos e momentos). |
| DEC-004 | DECIDIDO | Qualquer membro edita o conteúdo do casal; o apagado vai para a lixeira. Ninguém exclui sozinho o espaço inteiro. |
| DEC-005 | DECIDIDO | Um vínculo ativo por pessoa: índice único parcial no banco **e** validação transacional, à prova de concorrência. |
| DEC-006 | DECIDIDO | Vínculo com N membros no modelo; MVP com 2. |
| DEC-007 | DECIDIDO | `ACTIVE→PAUSED`, `PAUSED→ACTIVE`, `ACTIVE→ENDED`, `PAUSED→ENDED`; `ENDED` é final. Máquina de estados no domínio, transições num serviço transacional. |
| DEC-008 | DECIDIDO | Saída livre e individual. |
| DEC-009 | DECIDIDO | No término, a pessoa volta ao Personal com o que postou no próprio lado e o que estava guardado. |
| DEC-010 | DECIDIDO | Apenas 18+ (ECA Digital, Lei 15.211/2025). |
| DEC-011 | DECIDIDO | Verificação de idade por CPF em camadas (módulo `verification`, futuro). Obrigatória **antes de abrir ao público**; o piloto com casais conhecidos pode começar antes. O CPF prova a idade do titular, não de quem digita. |
| DEC-012 | REVISADA (2026-11-04/05) | O vínculo nasce por um **momento especial**: cada um conta a sua história, e as duas são reveladas juntas. Não é pedido de namoro. **O vínculo firma primeiro; história e datas vêm depois**, dentro do Couple Universe já nascido. A ideia da pergunta secreta foi abandonada. |
| DEC-013 | REVISADA | **PWA mobile-first** no MVP (iPhone e Android, sem lojas e sem custo). App nativo (backup automático da galeria) entra com os planos pagos, na mesma API. Desktop não é foco. |
| DEC-014/017 | SUBSTITUÍDAS | React Native/Expo + Supabase (histórico). |
| DEC-015 | REVISADA | Escopo do MVP: ver seção 8. |
| DEC-016 | EM ABERTO | Regras de 16–17 anos (fase futura; não implementar). |
| DEC-018 | DECIDIDO | Backend Java 21 + Spring Boot (Web, Security, Data JPA, Validation, Actuator), PostgreSQL, Flyway, Springdoc, JUnit 5. O Spring é a única camada de lógica e API. Testcontainers fora por ora (testes usam Postgres local). |
| DEC-019 | DECIDIDO | Independência de fornecedores: infraestrutura substituível, atrás de abstrações (`MediaStorageProvider`). Supabase não é núcleo. |
| DEC-020 | DECIDIDO | Monólito modular, sem microserviços. |
| DEC-021 | DECIDIDO | Duo Cloud: arquivos só pela API com regra de acesso; local em desenvolvimento, S3 compatível (R2) em produção. |
| DEC-022 | DECIDIDO | Planos futuros Free/Plus/Premium; limites comerciais só depois de medir custos reais. |
| DEC-023 | DECIDIDO | O Duo é a junção do casal com as memórias. |
| DEC-024 | DECIDIDO | Frontend React + Vite + TypeScript como PWA. **Sem Next.js** (app privado, sem ganho de SEO; evita um segundo backend). Servido pela mesma origem do backend (sem CORS, cookies `SameSite=Strict`). |
| DEC-025 | REVISADA | Infra de custo zero: VM Oracle Always Free + Postgres próprio + Caddy; mídia no R2; backup diário do banco com restauração testada (obrigatório desde o 1º deploy); subdomínio gratuito no piloto. **Deploy adiado: o fundador não tem cartão (Q-14).** |
| DEC-026 | REVISADA pela DEC-030 | Entrada sem senha. Rejeitada a pergunta pessoal como senha (baixa entropia, conhecida pelo parceiro/ex). Passkeys e código por e-mail passam para a Etapa B (adiada). |
| DEC-027 | DECIDIDO (futuro) | Visibilidade para outros casais: privado por padrão; só se os dois escolherem; por pedaço; some na hora se um desistir. O modelo já nasce com nível de visibilidade. |
| DEC-028 | DECIDIDO | Repositório privado; código em inglês, interface em português; pastas por camada técnica dentro de cada módulo. |
| DEC-029 | DECIDIDO | Responsabilidades legais (seção 11). |
| DEC-030 | DECIDIDO (2026-11-04) | **Entrar só com Google no MVP.** Ganhos: sem serviço de e-mail, e-mail já verificado (RA-01). Custo: sem conta Google não entra; o Google é ponto único de falha. RA-02 vale; RA-03, RA-06, H3 e H5 suspensos até a Etapa B. |
| DEC-031 | DECIDIDO | Convite por **DuoName exato** como caminho padrão; link com código de 7 dias como alternativa (revisa o RA-07 só para este fluxo; RN-30). |
| DEC-032 | DECIDIDO | Perfil: bio até 100, profissão até 20, estado civil declarativo (Solteiro/Namorando/Casado/Divorciado/Viúvo), visibilidade `PUBLIC`/`PRIVATE`. Personal nasce público; casal nasce privado. |
| DEC-033 | DECIDIDO (congelada) | Duollowers/Duollowing: perfil público segue direto; privado exige aceite, sem aviso a ninguém. Seguem-se pessoas, não o casal. |
| DEC-034 | DECIDIDO (congelada) | Busca por nome ou DuoName, sem acento nem maiúsculas, mínimo 3 letras, 20 por página, 120 buscas/hora; bloqueados e ocultos nunca aparecem. O convite de vínculo continua só por DuoName exato. |
| DEC-035 | DECIDIDO | Bloqueio (some nos dois sentidos) e denúncia (motivos fixos, sem revelar o denunciante, repetição aceita em silêncio). Sem painel: revisão manual no banco (`reports`, `hidden_at`). |
| DEC-036 | DECIDIDO | Moldura com painel lateral de 240px (no celular, faixa no topo e navegação fixa embaixo); "Editar perfil" abre um modal que sobe de baixo. Única dependência nova: `lucide-react` 1.58.0. |

**Q-20 (parcial):** só avatar e fotos do Personal: JPEG/PNG/WebP validados pelos bytes; avatar até 1 MB (recorte
de 512 px no aparelho); fotos até 2 MB (lado máximo de 1600 px); limite provisório de 200 fotos e 100 MB; visibilidade
por foto `ONLY_ME` (padrão), `DUOLLOWERS` ou `PUBLIC`, limitada pela do perfil. Cotas gerais, planos e vídeo seguem
abertos (o fundador quer decidir junto ao módulo de storage, por causa da monetização).

## 6. Regras de negócio

**Cadastro e idade**
- **RN-01:** só 18 anos ou mais. **RN-02:** idade comprovada pelo CPF em camadas (dígitos → nascimento → cruzamento
  com nome e data). **RN-03:** bloqueio de 1 hora após 2 tentativas que não batem; depois, nova tentativa liberada.
  **RN-04:** um CPF por conta. **RN-05:** CPF guardado só como HMAC-SHA256 com segredo do servidor. **RN-05a:** se a
  consulta falhar, a verificação fica pendente, nunca aprovada.

**Vínculo**
- **RN-06:** um vínculo `ACTIVE` ou `PAUSED` por pessoa. **RN-07:** no MVP, exatamente 2 membros. **RN-08:** o
  vínculo só nasce com a confirmação de todos.
- **RN-09:** (fora do MVP na v5) no vínculo, cada pessoa escolhe o que leva; o resto fica `ARCHIVED`. Nada vai
  automaticamente. A estrutura `Scope` já existe.
- **RN-10:** a data do vínculo é o primeiro evento da linha do tempo.
- **RN-25:** a história (4 passos) é obrigatória; cada passo precisa de texto não vazio depois de remover espaços,
  **sem mínimo de caracteres**. **RN-26:** as duas histórias só ficam visíveis uma à outra quando ambas estão
  completas. **RN-27:** as duas datas do casal (se conheceram; começaram a namorar) são únicas, propostas por um e
  aceitas pelo outro (nunca por quem propôs); nova proposta reabre o consenso. **RN-28:** o contador de tempo
  juntos usa a data de início do namoro. **RN-29:** pareamento expirado, recusado ou cancelado apaga rascunhos e
  proposta de datas (expiração *lazy*, sem job). **RN-30:** o convite de vínculo só aceita DuoName **exato**, com
  limite de tentativas (RA-08); resposta idêntica para inexistente, próprio, já vinculado, bloqueado ou oculto.

**Universo do casal**
- **RN-11:** qualquer membro edita o centro; exclusões vão para a lixeira por 30 dias, depois são apagadas.
  **RN-11a:** exclusão em massa no centro exige consentimento explícito dos dois, dado nas duas contas.
- **RN-12:** só membros atuais acessam o Couple Universe. **RN-12a:** o lado de cada um é visível ao parceiro; só o
  dono posta e apaga nele.

**Pausa, saída e retorno**
- **RN-13:** qualquer membro pausa sozinho; nada é apagado. **RN-13a:** na `PAUSED`, a pessoa volta a ver e editar
  o próprio Personal, por livre escolha. **RN-14:** qualquer membro sai sozinho, a qualquer momento. **RN-15:** ao
  sair, volta ao Personal com o que postou no próprio lado e o que estava guardado. **RN-16:** sem alardes ou
  notificações públicas de pausa ou término.
- **RN-17:** o término nunca apaga as memórias de ninguém. **RN-17a:** após o término, cada ex-membro pode
  esconder o centro do casal da própria visão (e desfazer); não afeta o outro; o dado nunca é apagado.
  **RN-18:** ninguém apaga sozinho o espaço inteiro do casal.

**Duo Cloud**
- **RN-19:** cada mídia tem dono, escopo e visibilidade; o backend decide o acesso por essas três informações.
  **RN-20:** toda autorização é verificada no backend pelo usuário autenticado. **RN-21:** arquivos só pela API com
  regra de acesso (URL assinada curta quando houver R2). **RN-22:** todo upload respeita a cota antes de ser
  aceito. **RN-23:** arquivos validados por tipo real (bytes), tamanho e checksum. **RN-24:** no gratuito, fotos
  comprimidas no aparelho, sem vídeo.

**Conta e acesso (épico 1)**
- **RA-01:** e-mail único e confirmado (hoje garantido pelo Google). **RA-02:** nenhuma senha, nunca.
  **RA-03:** (Etapa B) código de 6 dígitos, 10 minutos, 5 tentativas, só hash. **RA-04:** token de acesso JWT de 15
  min; token de renovação de 30 dias em cookie `HttpOnly`, `Secure`, `SameSite=Strict`. **RA-05:** o token de
  renovação é rotacionado a cada uso; reuso de um antigo encerra todas as sessões da pessoa. **RA-06:** (Etapa B)
  várias passkeys, removíveis. **RA-07:** entrada e recuperação nunca revelam se um e-mail tem conta (exceção
  consciente: o convite de vínculo, RN-30). **RA-08:** limite de tentativas em rotas sensíveis (tabela
  `rate_limit_attempts`). **RA-09:** códigos, tokens e chaves nunca em logs.

## 7. Arquitetura

PWA (React + Vite) → API Spring Boot → PostgreSQL próprio; fotos no `MediaStorageProvider` (pasta local hoje, R2
depois). Em produção, o Caddy serve a PWA em `/` e faz proxy de `/api` (mesma origem). **A estrutura real de pastas
e arquivos está no `docs/MAPA.md`.**

Monorepo: `backend/` (Gradle Kotlin DSL, DEC/Q-22), `frontend/`, `docs/`, `estudos/`, `.github/`. Módulos do backend
(`com.duo`): `auth`, `users`, `relationships`, `social` (congelado), `media`, `storage`, `shared`; futuros:
`verification`, `albums`, `memories`, `subscriptions`.

Regras: controllers sem regra de negócio e sem acesso a repository; um módulo não usa o `internal` de outro; sem
ciclos (garantido pelo `ArchitectureTest`); API pública na raiz do módulo, implementação em `internal/{model,
repository,service}`; sem abstração sem justificativa. No frontend, uma feature só importa de `shared/`, nunca de
outra feature.

**Modelo de dados — garantias:** `MediaAsset` distingue autoria (quem enviou), propriedade (de quem é), escopo
(`PERSONAL`, `ARCHIVED`, `MEMBER_SIDE`, `COUPLE_CENTER`) e visibilidade (`ONLY_ME`, `COUPLE`, futuramente
`OTHER_COUPLES`); coerência escopo↔casal por constraint; RN-06 por índice único parcial; exclusão lógica onde houver
lixeira.

**Segurança:** autenticação sem senha; autorização por recurso em toda operação, com **testes de acesso cruzado**
para todo endpoint que toca dado de duas pessoas; validação de entrada e de arquivos; segredos só em variáveis de
ambiente (`DUO_APP_DB_PASSWORD`, `DUO_JWT_SECRET`, `DUO_GOOGLE_CLIENT_ID/SECRET`, ...); logs sem CPF, códigos, tokens
ou URLs assinadas; desligar o Swagger UI em produção.

**Infra e custos (todos R$ 0):** VM Oracle Always Free, R2 (10 GB), backup em bucket separado, subdomínio gratuito,
GitHub Actions (CI enxuto). Estimativa: foto comprimida ~0,5 MB; 10 GB cobrem ~40 usuários cheios. Domínio próprio
(~R$ 40/ano) antes do lançamento.

## 8. Escopo e plano

**Escopo do MVP (v5).** *Dentro:* conta e sessão (Google); perfil pessoal; convite e vínculo; fotos do casal; linha
do tempo e contador; pergunta do dia; pausa, saída, lixeira, exclusão de conta e exportação; bloqueio, denúncia e
moderação mínima. *Fora:* busca/Duollowers/perfil de terceiros (congelados, não apagados), "o que levar" (RN-09),
vídeo, álbuns, datas especiais com lembrete, celebração em tempo real, R2/URLs assinadas, verificação de idade (só
antes de abrir ao público), passkeys, planos pagos. **Fase 2:** planos pagos, vídeo, originais, app nativo com
backup, diário, sonhos e objetivos, jogos de conhecimento mútuo, música. **Fase 3:** Cantinhos (Bíblia, financeiro,
metas), cofre do casal (18+, criptografia de ponta a ponta), visibilidade para outros casais, trisal, regras de
16–17. **Horizonte:** universo social dos solteiros. Abrir o app a quem é de fora vem **depois da Fatia 5**.

**Plano por fatias** (detalhe, requisitos RF e critérios em `docs/CONSULTA.md`, seção 3): 0 fundação segura →
1 vínculo em dois aparelhos reais → 2 fotos do casal → 3 linha do tempo e contador → 4 pergunta do dia → 5 pausa,
saída e piso mínimo → épico H (confiança), deploy e backups → piloto (fundador + 4 casais por 30 dias; sucesso =
3 casais voltando sozinhos por semana). **Definição de pronto:** build verde, testes passando, teste de acesso
cruzado quando houver dado de duas pessoas, changelog, docs atualizados, commit citando RF/RN.

## 9. Responsabilidades legais (DEC-029)

**Do Duo:** (1) proteger os dados (LGPD): segurança adequada, mínimo de dados, comunicar incidentes à ANPD e aos
usuários; (2) termos de uso e política de privacidade claros antes do piloto; (3) direitos do titular: ver,
corrigir, exportar e excluir, com canal de contato; (4) exclusão de conta dentro do app; (5) verificação de idade
antes de abrir ao público; (6) denúncia e bloqueio; (7) remover conteúdo ilícito quando notificado e comunicar as
autoridades em casos graves, especialmente com menores; (8) guardar registros de acesso (data, hora, IP) por 6
meses, em sigilo (Marco Civil); (9) com planos pagos: preço claro, cancelamento fácil, regra clara para as fotos
de quem deixar de pagar. **Não é do Duo:** arbitrar relacionamentos, vigiar conteúdo privado, julgar a maturidade
dos casais. Os textos legais precisam de revisão de advogado antes de abrir ao público. Prazo de guarda das
denúncias: validar com advogado. Verificação de CPF: as fontes são todas pagas por consulta (Serpro, Didit,
BigData Corp, Infosimples...); exceção consciente ao custo zero, a decidir perto do lançamento; o módulo fica atrás
de uma interface.

## 10. Estado atual (2026-11-10)

**Pronto e funcionando:**
- **Épico 1 (conta, Etapa A):** entrar com Google; sessão própria (JWT de 15 min + renovação rotacionada com
  detecção de reuso); sair de um aparelho ou de todos.
- **Épico 2 (vínculo):** convite por DuoName ou link, confirmação, `Couple`, história, datas, pausa/retomada/saída,
  matriz de permissões (`CouplePermissions`, ainda sem consumidores), com testes de concorrência reais. Telas
  existem; **ainda não foi validado em dois aparelhos reais**.
- **Épico 3 (Personal):** perfil, DuoName, avatar e fotos, bloqueio e denúncia; busca, Duollowers e perfil de
  terceiros existem mas estão **congelados** (RF-A11; `duo.features.social.enabled=false`).
- **Frontend:** reorganizado por feature com alias `@/` e CSS Modules; fundo aurora com vidro (glassmorphism) por
  cima; moldura com barra lateral de vidro roxo; fonte Instrument Sans; tela de login nova (ilustração em tela
  cheia, cartão de vidro, halos e vaga-lumes animados); menu da foto de perfil no computador. Detalhe em
  `docs/MAPA.md`. O perfil e o menu da foto no **celular** ainda serão pensados.
- **Fatia 0 (parcial):** feitos RF-A01, A02, A06, A08, A10, A11. Próximos: RF-A03 (revalidar conta no refresh) e
  RF-A12; depois a limpeza de `refresh_tokens`/`rate_limit_attempts` (parte do RF-A09).
- **Números:** frontend 71 testes passando (2026-11-10); ~225 testes de backend por `grep`, não rodados nesta
  sessão; migrations V1 a V14. Rode `./gradlew build` e `npm test` para o valor real.

**Em aberto / parqueado:**
- **CI:** o fundador autenticou o `gh`; falta conferir a aba Actions no GitHub (verde ou vermelho). O job do
  frontend roda `format:check`.
- **Etapa B parqueada** (`backend/wip-etapa-b-pendente/`): código de e-mail e `DevLoginController`, fora do build.
  O `build.gradle.kts` já traz `spring-boot-starter-mail`, e o `SecurityConfig` libera `GET /api/dev/**`; antes de
  ativar, a migration precisa de número novo (V12 já existe) e o provedor de e-mail (Q-18) está em aberto. Decidir
  se a Etapa B volta ou se a dependência sai.
- **Epico 2, Bloco D:** celebração em tempo real (P6): decidida, mas fora do MVP na v5. Nenhuma tecnologia de
  tempo real foi escolhida (WebSocket, SSE ou polling).
- **Segurança:** o fundador trocou a senha do `duo_app` em 2026-11-05 após um log de depuração expô-la (corrigido).
- **Pendências do épico 1:** RA-08 em `/api/auth/*` (RF-A05, aguarda valores), tela "meus aparelhos", exclusão de
  conta (RF-G07).

**Decisões que aguardam o fundador:** P1 a P8 e os valores do RF-A05/A07/A09 (tabela em `docs/CONSULTA.md`, seção 4);
Q-18 (e-mail transacional, só se a Etapa B voltar); Q-20 (cotas gerais); datas especiais no MVP mínimo ou completo;
identidade visual e design system (a aurora e os tokens são provisórios); grafia final de "Duollowers/Duollowing";
o que são o "cartão grande" e o "círculo roxo" do esboço; selo de "vinculado".

**Próximo passo:** Fatia 0 (RF-A03, RF-A12), depois Fatia 1: vincular o fundador e a parceira em dois aparelhos
reais e corrigir o que aparecer.

## 11. Ambiente de desenvolvimento

- **Sistema:** o fundador alterna entre Fedora e Windows; esta sessão rodou no **Windows 11** (PowerShell/Git Bash).
  RAM é o fator limitante (~7 GB); fechar abas antes de builds pesados; IDE leve (VS Code).
- **Ferramentas:** JDK 21 (o Gradle baixa sozinho via toolchain/foojay, sem instalar no sistema), Node.js (22+), Git,
  `gh`. **Sem Docker por ora.** PostgreSQL instalado direto; bancos `duo` (dev) e `duo_test` (testes), dono `duo_app`.
- **Segredos:** variáveis de ambiente do usuário (`~/.zshrc` no Linux; `setx` no Windows, só vale para terminais
  abertos depois). Nunca no repositório.
- **Rodar:** `./dev.sh` (backend e frontend juntos) ou separados (`cd backend && ./gradlew bootRun`; `cd frontend &&
  npm run dev`), e abrir `http://localhost:5173`. Testar: `./gradlew build` (inclui Spotless e ArchUnit) e `npm test`,
  `npm run lint`, `npm run format:check`, `npm run build`.
- **Pegadinhas conhecidas:** Spring Boot 4.1 renomeou starters e mudou o pacote das anotações de test slice (ver
  `docs/CONSULTA.md`); `typescript-eslint` exige TypeScript `<6.1.0` (não usar o 7); `npm audit` acusa falhas só em
  ferramentas de build (não aplicar `--force`); o sandbox da IA pode não alcançar o terminal real do fundador (ex.: `gh`,
  `npm install`): quando for o caso, peça que ele rode o comando.
