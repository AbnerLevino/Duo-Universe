# Épico 2 — O vínculo (Couple Universe nasce)

> Especificação para revisão do fundador. **Nenhum código foi escrito.** Itens `DECIDIDO` vêm das conversas de
> 2026-11-04; itens `PROPOSTA` são recomendações que aguardam aprovação (regra 1 do CLAUDE.md).

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

1. A inicia. O sistema cria um `CouplePairing` `PENDING` e um **código ou link que expira**.
2. B entra com o código (já logado com Google).
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
- **RN-25:** a história é obrigatória para o vínculo nascer; só texto não vazio é exigido (ver Q-aberta 4).
- **RN-26:** as duas histórias só ficam visíveis uma à outra depois que ambas foram concluídas.
- **RN-27:** a data de conhecimento e a data de início do namoro são únicas por casal e precisam do consenso dos
  dois.
- **RN-28:** o contador de tempo juntos usa a data de início do namoro.
- **RN-29:** pareamento expirado, recusado ou cancelado apaga os rascunhos.

## Requisitos não funcionais

- **Concorrência (RN-06):** índice único parcial no banco, mais validação transacional na aplicação, mais **teste
  que dispara duas confirmações simultâneas** e prova que só uma vence. A validação final ocorre **na
  confirmação**, não só na criação do pareamento.
- **Autorização (RN-12, RN-20):** o casal é sempre derivado do usuário autenticado; nunca de um `coupleId`
  enviado pelo frontend. Testes de acesso cruzado (a pessoa C tentando ver o casal A).
- **Privacidade:** textos das histórias, códigos de pareamento e rascunhos nunca em logs (RA-09).
- **Anti-enumeração (RA-07):** não há busca de pessoas por e-mail; o encontro é por código ou link.
- **Métricas:** funil iniciado, entrou, terminou, firmou, sem expor conteúdo.

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
`couple_pairings` (estado, código, expiração) e as histórias dos membros. O acontecimento guardado já nasce com
**data** e indicador de **marco**, para que o universo visual futuro (docs/IDEIAS.md) seja só uma forma de olhar
os mesmos dados.

## Perguntas ainda abertas

1. **Código ou link do pareamento:** aceita que A envie por fora (WhatsApp etc.) e que expire (proposta: 7 dias)?
2. **Obrigatoriedade:** basta o texto não estar vazio? (Proposta: sim. Um tamanho mínimo incentiva "asdfasdf".)
3. **Consenso das datas:** como o casal chega ao acordo na tela? (Proposta: um propõe, o outro aceita ou
   contrapropõe, até concordarem.)
4. **O que levar (RN-09):** modelamos a estrutura agora e a tela fica na Semana 4? (Proposta: sim.)
5. **DEC-002:** o Personal Universe volta durante a pausa, ou só no término?
6. **Gesto de celebração:** quando o universo nasce, o gesto simultâneo entra como toque final opcional (depois)?
