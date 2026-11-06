# Ideias (material, fora do escopo do MVP)

> Aqui ficam ideias do fundador para guardar e pensar. **Nada daqui é implementado sem decisão registrada**
> (regra 3 do CLAUDE.md). Cada ideia diz do que ela depende.

## Universo visual: estrelas e planetas (2026-11-04)

Em vez de uma linha do tempo, o casal veria o seu universo: momentos comuns como **estrelas** e marcos
importantes (ex.: o início do namoro) como **planetas**. Ao entrar num planeta, a pessoa vê as fotos e o que
aconteceu naquele dia ou ano.

- **Depende de:** acontecimentos com data e indicador de marco no modelo de dados (já previsto no Épico 2), e
  de fotos (Semana 4).
- **Princípio:** dado e apresentação são coisas separadas. O banco guarda acontecimentos com data; a tela é uma
  forma de olhá-los.
- **Custo:** desenhar algo animado e leve no celular; decidir mais perto da Semana 5 (que hoje diz "linha do
  tempo"; a primeira versão pode ser uma lista simples).

## Juntar as duas histórias numa linha do tempo ou universo (2026-11-04)

Combinar a história de cada um do casal numa visão única, com fotos. Hoje as duas histórias são lidas
separadamente. Só ideia, para ter material.

## Gesto de celebração (2026-11-04)

Quando o Couple Universe nasce, os dois fazem um gesto ao mesmo tempo (ex.: segurar um botão). Exige os dois
online em tempo real. Toque final opcional, depois do vínculo funcionando.

## Chat do casal (2026-11-04)

Um chat bonito, com várias coisas para entreter. **Fase 2**: exige tempo real, armazenamento, moderação, denúncia
e bloqueio (seção 16 do CLAUDE.md) e retenção sob a LGPD.

## "Dar um tempo da rede": perfil desativado temporariamente (2026-11-04)

Um interruptor da conta, diferente da pausa do vínculo (que é do casal). **Fase 2**: se mistura com a exclusão
de conta (Q-17).

## Rede social (visão de longo prazo, 2026-11-04)

O Duo quer ser uma rede social, com o Personal e o Couple Universe como espaços de postar. O casal é **privado
por padrão** e escolhe o que mostrar (DEC-027). Horizonte (seção 10). Traz as obrigações da seção 16 (denúncia,
bloqueio, remoção de conteúdo ilícito).

**Atualização de 2026-11-06:** o fundador decidiu que o Duo terá **busca por nome (além do handle), conexões e
seguidores (pedir para seguir e ser seguido)** e que o casal escolherá se o perfil do Couple é público, privado
ou visível só a pessoas selecionadas. Isso tira a rede social do "Horizonte" e **precisa ser registrado como
decisão de escopo** (seção 11 do `CLAUDE.md`), com denúncia e bloqueio (seção 16) como pré-requisito.

## Jogos e perguntas entre o casal (2026-11-05)

Resposta do fundador às Q-11/Q-12 (pergunta do dia): além da pergunta do dia (curada pelo app, no escopo do
MVP mínimo — seção 10), o casal poderia criar perguntas um para o outro em momentos determinados, e o app
ofereceria jogos interativos com perguntas "vergonha alheia", picantes etc.

- **Depende de:** chat do casal (já listado acima, Fase 2 — exige tempo real, moderação e retenção sob LGPD).
  "Jogos de conhecimento mútuo" já está na Fase 2 (seção 10); isso detalha o conteúdo desses jogos.
- **Atenção:** perguntas "picantes" tocam no Cofre do casal (seção 10 do CLAUDE.md — 18+, criptografia de
  ponta a ponta) e nos princípios de maturidade/liberdade (seção 3) — o app oferece, não arbitra conteúdo.
  Detalhar moderação e faixa de conteúdo quando este item for priorizado.
- **Fora do MVP mínimo e completo** (seção 11): não implementar agora.

## "Pedido de namoro" para quem está solteiro no Duo (2026-11-05)

Hoje o vínculo (DEC-012) assume que o casal **já namora na vida real** — é um ritual de formalizar a relação
no Duo, não um convite para começar a namorar. O fundador trouxe uma ideia para o futuro, ao decidir a Etapa
12/RN-29 (seção 19 do `CLAUDE.md`): alguém no Personal Universe que está conversando organicamente com outra
pessoa dentro do próprio Duo — já tem um convívio natural, "se conheceu ali" — poderia enviar um pedido de
namoro de verdade, como um gesto fofo (para gente perto ou longe). Isso seria um fluxo **diferente** do
momento do vínculo de hoje.

- **Depende de:** alguma forma de pessoas Personal se conhecerem e conversarem dentro do Duo — hoje isso não
  existe (Épicos 1/2 não têm isso); conecta com a ideia de "rede social" de longo prazo, acima.
- **Só vale para quem está Personal:** alguém que já é Couple não precisa disso — só pode ter um vínculo
  ativo por pessoa (RN-06).
- **Relaciona-se com a Etapa 12:** é parte do motivo de permitirmos, desde já, mais de um `CouplePairing`
  `PENDING` por pessoa ao mesmo tempo — não custa nada deixar aberto, e evita bloquear esse caso de uso futuro
  sem necessidade real agora.
- **Fora do MVP mínimo e completo** (seção 11): não implementar agora.

## Home como "o seu universo", não como feed (2026-11-06)

Ao pensar a tela inicial do Personal, a proposta é que a home mostre **o universo da própria pessoa**: o cartão
de perfil, os momentos recentes dela e, quando houver casal, o banner "N meses juntos" (esboço do fundador,
`DUO Universe.png`). Um feed com conteúdo de **outras pessoas** é um passo à parte.

- **Por quê:** a home é útil desde o primeiro dia, sem depender de outras pessoas; combina com "lugar quentinho"
  e "formatos próprios, sem feed infinito" (seção 3 do `CLAUDE.md`); e evita trazer moderação (denúncia, bloqueio,
  remoção de conteúdo, seção 16) antes da hora.
- **Depende de:** perfil e fotos do Personal (Épico 3); o feed de outras pessoas depende de conexões/seguidores
  e do que a seção 16 exige.
- **Fora do MVP:** feed com conteúdo de outras pessoas é "Universo social" (Horizonte, seção 10).

## Formatos próprios no lugar do "reels" (2026-11-06)

O fundador não quer um reels. Vídeo em si é **Fase 2** (custo de armazenamento e de conversão, planos pagos), mas
os formatos abaixo ficam guardados para quando chegar a hora. Princípio comum: **nada de rolagem vertical
infinita com reprodução automática**.

- **Recados que se abrem depois ("abra quando…")** — uma foto, um áudio ou um vídeo curto **selado** até uma data
  ou uma situação ("abra quando estiver com saudade", "abra no nosso aniversário"). Privado entre duas pessoas,
  o que evita o peso de moderação, e muito ligado ao casal. Candidato a **primeiro formato de vídeo** (duração
  curta, ex.: até 30 s).
  - **Depende de:** Duo Cloud (mídia), vídeo (Fase 2), cotas (Q-20) e algum jeito de lembrar a pessoa quando o
    recado puder ser aberto.
- **Feed com fim** — se um dia houver feed, ele **acaba**: "você viu tudo de hoje". Sem rolagem infinita.
  - **Depende de:** conexões/seguidores e do feed em si (Horizonte).
- **Resumo automático ("nosso ano em 1 minuto")** — o app monta sozinho um vídeo curto a partir dos momentos e
  das datas do casal. Existe prior art (apps de "1 segundo por dia"); o diferencial seria a história do casal
  (datas do vínculo, a pergunta do dia, os marcos).
  - **Depende de:** fotos/momentos com data (Épico 3 e Semana 4), vídeo (Fase 2). Gerar vídeo é pesado: decidir
    se roda no aparelho ou no servidor, e o custo disso, antes de prometer.
- **Universo de estrelas e planetas** — já registrado acima; é também o formato próprio de **navegar** por vídeos
  e fotos (um planeta pode abrir um álbum), em vez de uma lista rolável.

## Fora de questão

- **Cards e compartilhamento para o Instagram:** o fundador quer se distanciar dele. Não fazer.
