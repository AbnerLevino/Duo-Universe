# Épico 2 — O vínculo (Couple Universe nasce)

> **Especificação aprovada em 2026-11-04** (perguntas P1 a P6 respondidas pelo fundador). **Ainda nenhum código
> foi escrito** — a implementação (migration V4 e regra de concorrência) começa como próxima etapa, por fora
> desta sessão de decisões. Itens `DECIDIDO` vêm das conversas de 2026-11-04; itens `PROPOSTA` são recomendações
> que aguardam aprovação (regra 1 do CLAUDE.md).

## Objetivo

Duas pessoas já cadastradas criam juntas o seu **Couple Universe**. O momento é especial: cada um conta a sua
história, as duas aparecem ao mesmo tempo, e o universo nasce.

O vínculo **não é um pedido de namoro** (o casal já existe na vida real). É o momento em que os dois decidem
guardar a história no Duo. O conceito do ritual continua sendo especial e pode ganhar celebração visual.

## Decisões deste épico

| Código | Decisão | Status |
| --- | --- | --- |
| DEC-012 (revisada) | O formato do momento do vínculo é **cada um contar a sua história**, e não uma pergunta secreta. Substitui a ideia de resposta aproximada. | `DECIDIDO` |
| Q-05 | Perto ou longe é o mesmo fluxo. | `DECIDIDO` |
| Q-10 | Nome na interface: **Couple Universe**. No código continua `CouplePairing`. | `DECIDIDO` |
| Q-06 | Matriz de permissões por estado (seção "Estados"). | `DECIDIDO` |
| História | **Obrigatória.** Quem não quer responder, não se vincula. | `DECIDIDO` |
| Tempo juntos | O contador ("Juntos há 2 anos e 9 meses") parte das datas do relacionamento, **não** da data de entrada no Duo. | `DECIDIDO` |
| Datas do casal | **Consenso obrigatório** sobre a data em que se conheceram e a data em que começaram a namorar. Datas de acontecimentos dentro das histórias são de cada um. | `DECIDIDO` |
| Sem prévia | O parceiro não vê nada da história do outro antes de os dois terminarem. | `DECIDIDO` |
| Instagram | Fora do produto: sem cards e sem compartilhamento externo. O Duo caminha para ser a própria rede. | `DECIDIDO` |
| Conteúdo | Tudo editável (tirar, mover, apagar) pelo dono. | `DECIDIDO` |
| Chat e "dar um tempo da rede" | Fase 2. Fora do MVP. | `DECIDIDO` |
| Data de entrada no Duo | Gravada automaticamente (`created_at`) só para métricas. Não aparece para o casal. | `DECIDIDO` |
| P1 — Convite | Caminho **padrão**: buscar a pessoa dentro do próprio Duo por um **@handle exato** (identificador único escolhido no cadastro, nunca nome ou e-mail) — reforça o Duo como rede social, sem depender de apps de terceiros. Link com código que expira em 7 dias, de uso único, continua existindo como **alternativa** (ex.: antes de saber o @handle do outro). Revisa o escopo de anti-enumeração do RA-07 **só para este fluxo de convite** (DEC-031, seção 19 do CLAUDE.md). | `DECIDIDO` |
| P2 — Obrigatoriedade | Cada um dos 4 passos da história exige **no mínimo 20 caracteres** (depois de remover espaços) — não basta não estar vazio. | `DECIDIDO` |
| P3 — Consenso das datas | **Propor e aceitar:** um propõe as duas datas do casal, o outro aceita ou propõe outras, até concordarem; só depois disso o casal pode firmar (RN-27). | `DECIDIDO` |
| P4 — O que levar | Estrutura modelada **já na Semana 3** (escopos `PERSONAL`, `ARCHIVED`, `MEMBER_SIDE` e regras, com itens fictícios nos testes); a tela real para escolher o que levar vem na Semana 4, junto das fotos (RN-09). | `DECIDIDO` |
| P5 — Personal na pausa | O Personal Universe **volta durante a `PAUSED`** (leitura do que foi postado no próprio lado e do que está arquivado) — revisa a DEC-002 (nova regra RN-13a). **Ainda em aberto:** se dá para postar/editar no Personal durante a pausa, e o que acontece com isso ao retomar. | `DECIDIDO (parcial)` |
| P6 — Celebração | O gesto de celebração entra **já na Semana 3**, em **tempo real**: os dois precisam estar online juntos no momento em que o Couple Universe nasce. Acrescenta uma peça técnica nova ao MVP (presença online + notificação em tempo real) — ver nota técnica na seção de requisitos não funcionais. | `DECIDIDO` |

## Histórias

- **V1 Iniciar:** como pessoa solteira, quero criar o nosso Couple Universe e chamar a minha pessoa.
- **V2 Entrar:** como a pessoa chamada, quero entrar usando o código ou link.
- **V3 Contar a minha história:** como membro, quero escrever como nos conhecemos, como foi o tempo antes de
  namorarmos, o que eu pensava e o que penso agora.
- **V4 Combinar as datas:** como casal, queremos concordar numa data em que nos conhecemos e numa data em que
  começamos a namorar.
- **V5 Firmar:** como membro, quero confirmar que quero criar o Couple Universe.
- **V6 Ler a história do outro:** como membro, quero ler a história que a minha pessoa escreveu.
- **V7 Pausar, retomar e sair:** como membro, quero pausar, retomar ou sair (RN-13, RN-14).

## O fluxo

1. A inicia e busca B pelo **@handle exato** dentro do Duo (padrão) ou gera um **link com código** para enviar por
   fora (alternativa, ex.: B ainda não tem conta ou A não sabe o @handle). O sistema cria um `CouplePairing`
   `PENDING`; o convite por link também carrega um código que expira em 7 dias e vale uma única vez.
2. B aceita o convite recebido no próprio Duo, ou entra com o código/link (já logado com Google em ambos os casos).
3. Cada um escreve a sua história em **passos curtos**, tudo obrigatório, com rascunho salvo.
4. Os dois combinam as duas datas do casal (V4).
5. Cada um confirma ("firmar"). Só com os dois confirmados o `Couple` nasce como `ACTIVE` (RN-08).
6. As duas histórias são reveladas **ao mesmo tempo**.

Se o pareamento expira ou alguém desiste, os rascunhos são **apagados** (minimização de dados, LGPD).

### Como cada um conta a sua história (passos)

1. Como nos conhecemos.
2. Como foi o tempo conhecendo a pessoa, antes de namorarmos.
3. O que eu pensava dela naquela época.
4. O que eu penso agora.

Os textos são livres. Datas de acontecimentos citados nas histórias pertencem ao autor.

## Estados (matriz aprovada, Q-06)

```
Pairing:   PENDING ──(os dois firmam)──► CONFIRMED  → cria o Couple
              └──(recusa / expira / cancela)──► CANCELLED

Couple:    ACTIVE ⇄ PAUSED
              │         │
              └────►  ENDED   (final)
```

| Ação | ACTIVE | PAUSED | ENDED |
| --- | --- | --- | --- |
| Ver o centro do casal | sim | sim | não |
| Postar no centro / editar | sim | não | não |
| Postar no próprio lado | sim | não | não |
| Pausar | — | não | não |
| Retomar | — | sim | não |
| Sair | sim | sim | — |

## Regras

**Já decididas:** RN-06 (um vínculo `ACTIVE` ou `PAUSED` por pessoa), RN-07, RN-08, RN-09, RN-10, RN-12, RN-12a,
RN-13 a RN-18 (seção 8 do CLAUDE.md).

**Novas (a registrar na seção 8 após aprovação):**
- **RN-25:** a história é obrigatória para o vínculo nascer; cada passo exige no mínimo 20 caracteres (P2).
- **RN-26:** as duas histórias só ficam visíveis uma à outra depois que ambas foram concluídas.
- **RN-27:** a data de conhecimento e a data de início do namoro são únicas por casal, propostas por um e aceitas
  pelo outro até concordarem (P3).
- **RN-28:** o contador de tempo juntos usa a data de início do namoro.
- **RN-29:** pareamento expirado, recusado ou cancelado apaga os rascunhos.
- **RN-30:** a busca de pessoas para convite só aceita **@handle exato** (sem busca livre por nome/e-mail, sem
  listar resultados parciais); limite de tentativas por conta e por IP (RA-08). Link com código continua
  disponível como alternativa (P1).
- **RN-13a:** durante a `PAUSED`, a pessoa vê (leitura) o que postou no próprio lado e o que estava arquivado —
  revisa a DEC-002 (P5). Postar/editar no Personal durante a pausa fica em aberto.

## Requisitos não funcionais

- **Concorrência (RN-06):** índice único parcial no banco, mais validação transacional na aplicação, mais **teste
  que dispara duas confirmações simultâneas** e prova que só uma vence. A validação final ocorre **na
  confirmação**, não só na criação do pareamento.
- **Autorização (RN-12, RN-20):** o casal é sempre derivado do usuário autenticado; nunca de um `coupleId`
  enviado pelo frontend. Testes de acesso cruzado (a pessoa C tentando ver o casal A).
- **Privacidade:** textos das histórias, códigos de pareamento e rascunhos nunca em logs (RA-09).
- **Anti-enumeração (RN-30, revisa o escopo do RA-07 para este fluxo):** sem busca livre por nome ou e-mail, sem
  listagem; só @handle exato, com limite de tentativas (RA-08). Link com código continua como alternativa.
- **Métricas:** funil iniciado, entrou, terminou, firmou, sem expor conteúdo.
- **Celebração em tempo real (P6, nota técnica):** o nascimento do Couple Universe precisa avisar os dois membros
  "ao vivo" quando o segundo confirma — exige presença online (quem está conectado agora) e um canal de tempo
  real (WebSocket ou SSE; a decidir no detalhamento técnico da Semana 3). Se alguém não estiver online no
  momento, a celebração completa (com o parceiro) só ocorre quando ambos estiverem simultaneamente; o vínculo em
  si não fica bloqueado esperando isso.

## Casos de borda

- Os dois se chamam ao mesmo tempo.
- Alguém já vinculado recebe um código.
- O pareamento expira com um dos dois já tendo escrito a história.
- A pessoa tem dois pareamentos pendentes e firma os dois.
- Os dois discordam das datas (a tela de consenso precisa de uma saída definida).
- Alguém tenta uma transição ilegal (`ENDED` para `ACTIVE`).
- Alguém sai enquanto o outro edita.

## Critérios de aceitação (PROPOSTA)

- *Dado* duas pessoas solteiras, *quando* as duas concluem a história, combinam as datas e firmam, *então* o casal
  nasce `ACTIVE` e as duas histórias aparecem para ambos.
- *Dado* que só uma terminou, *então* a história dela **não** é visível ao parceiro.
- *Dado* uma pessoa já em vínculo `ACTIVE`, *quando* tenta firmar outro, *então* é recusada e nada muda.
- *Dado* duas firmas simultâneas da mesma pessoa em vínculos diferentes, *então* apenas uma tem sucesso.
- *Dado* um pareamento expirado, *então* os rascunhos são apagados.
- *Dado* um vínculo `ACTIVE`, *quando* um membro sai, *então* o vínculo vira `ENDED`, a pessoa volta a poder se
  vincular e nenhuma memória é apagada (RN-17).
- *Dado* um não-membro, *quando* acessa o casal, *então* é negado sem revelar que ele existe.

## Modelo de dados (visão geral, migration V4 a detalhar)

`couples` (estado, datas do casal), `couple_members` (com índice único parcial por pessoa ativa),
`couple_pairings` (estado, código, expiração, e se foi criado por @handle ou por link) e as histórias dos
membros. O acontecimento guardado já nasce com **data** e indicador de **marco**, para que o universo visual
futuro (docs/IDEIAS.md) seja só uma forma de olhar os mesmos dados.

**Nova necessidade (P1):** a tabela `users` (Épico 1) precisa de uma coluna `handle` única (ex.: índice único em
`lower(handle)`, como já existe para `email`), escolhida no cadastro ou num passo adicional antes do primeiro
convite — a decidir no detalhamento técnico de quando essa coluna é preenchida para quem já se cadastrou pelo
Épico 1.

## Perguntas respondidas em 2026-11-04 (histórico)

Todas as seis perguntas abaixo foram respondidas pelo fundador em 2026-11-04. As decisões finais já estão na
tabela "Decisões deste épico"; o conteúdo abaixo fica como registro das alternativas consideradas.

### P1 — Como a pessoa chamada encontra o casal? (código ou link)

> **Resolvida:** nem A nem B nem C como estavam propostas — o fundador quis o Duo como rede social, com busca
> **dentro do próprio app** por @handle exato, mantendo o link com código como alternativa. Ver linha P1 na
> tabela de decisões.

Não há busca de pessoas por e-mail (RA-07: não revelar quem tem conta).

| Opção | Como funciona | Prós | Contras |
| --- | --- | --- | --- |
| **A. Link com código que expira (recomendada)** | O sistema gera um link com código aleatório; A o envia por fora (WhatsApp etc.). Expira em 7 dias e só vale uma vez. | Funciona perto e longe; sem busca; simples. | Quem tiver o link entra: exige código longo e aleatório, uso único e cancelamento por A. |
| B. Código curto digitado | Ex.: 8 caracteres lidos em voz alta ou digitados. | Bom para quem está junto. | Mais fácil de adivinhar; exige limite de tentativas (RA-08, ainda pendente). |
| C. Procurar pelo e-mail | A digita o e-mail de B. | Direto. | Revela quem tem conta (viola RA-07). |

Consequência: o prazo (7 dias, a confirmar) também define quando os rascunhos são apagados (RN-29).

### P2 — O que significa "obrigatório"?

> **Resolvida:** opção B (mínimo de 20 caracteres).

| Opção | Consequência |
| --- | --- |
| **A. Texto não vazio, depois de remover espaços (recomendada)** | Simples; cada passo precisa de alguma resposta. Quem quiser burlar escreve qualquer coisa. |
| B. Tamanho mínimo (ex.: 20 caracteres) | Incentiva respostas de verdade, mas também "asdfasdf asdfasdf" e frustra quem responde curto e sincero. |

Observação: validar a **qualidade** da história é impossível e contrariaria o princípio da maturidade. A
obrigatoriedade garante o ritual, não o conteúdo.

### P3 — Como o casal chega ao consenso das datas (conheceram-se e começaram a namorar)?

> **Resolvida:** opção A (propor e aceitar), a recomendada.

| Opção | Como funciona | Prós | Contras |
| --- | --- | --- | --- |
| **A. Propor e aceitar (recomendada)** | Um propõe as duas datas, o outro aceita ou propõe outra, até concordarem. Só depois de aceitas o casal pode firmar. | Reflete "é um casal, precisam concordar"; sem desempate arbitrário. | Pode travar; precisa de prazo (o mesmo do pareamento) e de uma tela clara. |
| B. Cada um informa e o sistema compara | Se baterem, segue; se não, pede para conversarem. | Cada um informa sem influência do outro. | Mais passos; dois lugares para errar. |

Em qualquer opção: as datas são do **casal**, editáveis depois pelos dois (RN-27); o contador usa a data de
início do namoro (RN-28).

### P4 — "O que levar" (RN-09): quando entra?

> **Resolvida:** opção A (modelar a estrutura na Semana 3, tela na Semana 4), a recomendada.

Não há ainda conteúdo do Personal Universe para escolher (fotos só na Semana 4).

| Opção | Consequência |
| --- | --- |
| **A. Modelar a estrutura na Semana 3, tela na Semana 4 (recomendada)** | Os escopos `PERSONAL`, `ARCHIVED` e `MEMBER_SIDE` e as regras entram agora, com testes usando itens fictícios; a tela real vem com as fotos. Evita retrabalho no banco. |
| B. Adiar tudo para a Semana 4 | Semana 3 mais enxuta; risco de a estrutura nascer sem pensar na regra. |

### P5 — O Personal Universe volta durante a pausa?

> **Resolvida:** opção B (volta durante a pausa, em leitura), a recomendada. Revisa a DEC-002. Falta ainda
> decidir se dá para postar/editar durante a pausa.

A DEC-002 diz que, vinculado, **não existe um "eu" separado**; só no término a pessoa volta ao Personal (DEC-009).
Você disse que a pessoa pode "ter o personal" quando pausa.

| Opção | Consequência |
| --- | --- |
| A. Só no término (DEC-002 como está) | Pausa congela o espaço, sem mudar o modelo. Mais simples. |
| **B. Volta durante a pausa (recomendada, se for essa a intenção)** | A pessoa vê o que postou no próprio lado e o que estava arquivado, em modo leitura ou edição (a definir). **Revisa a DEC-002**; a matriz de estados precisa de uma linha nova ("ver o Personal"). |

Se escolher B, falta decidir: pode postar no Personal durante a pausa? E o que acontece com isso ao retomar?

### P6 — Gesto de celebração

> **Resolvida:** opção B (dentro da Semana 3, em tempo real) — **não** é a recomendada; o fundador assumiu
> conscientemente a peça técnica extra (presença online + canal em tempo real) dentro do cronograma de 3h/semana.

| Opção | Consequência |
| --- | --- |
| **A. Fora da Semana 3, toque final opcional depois (recomendada)** | O vínculo funciona de forma assíncrona; o gesto entra no polimento visual, sem atrasar. |
| B. Dentro da Semana 3 | Exige os dois online ao mesmo tempo (tempo real), uma peça técnica a mais; aumenta o risco do cronograma (3 h/semana). |

---

**Feito em 2026-11-04:** decisões registradas no `CLAUDE.md` (seção 19); RNs novas (RN-25 a RN-30, RN-13a)
prontas para promoção à seção 8 na próxima etapa de código; especificação marcada como **aprovada**. Próximo
passo: começar a Semana 3 pela migration V4 e pela regra de concorrência (RN-06), seguido do fluxo de convite
por @handle (precisa da coluna `handle` em `users`) e, por último, a peça de celebração em tempo real (P6).
