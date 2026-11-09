# Especificação de requisitos — Duo Universe, foco no casal (v5)

> **Estado:** proposta enviada ao fundador em 2026-11-09. O fundador autorizou começar os requisitos que **não
> dependem de decisão dele**; o restante aguarda aprovação. O `CLAUDE.md` ainda **não** foi reescrito para a v5.

## 1. Visão e escopo

**Visão (rascunho):** o Duo transforma duas vidas separadas em uma história compartilhada, para casais.

**Decisões do fundador incorporadas (2026-11-09):**
1. A camada social fica **congelada e fora de cena** (não apagada).
2. O perfil do solteiro é o que já existe no frontend, **melhorado e sem ser expandido**.
3. O **"o que levar" (RN-09) está fora do MVP**.
4. Abrir o app a pessoas de fora vem **bem depois** (depois da Fatia 5).

| Dentro do MVP | Fora do MVP |
| --- | --- |
| Conta e sessão (Google) | Busca por nome, Duollowers/Duollowing e perfil público de terceiros (congelados) |
| Perfil pessoal (o atual, melhorado) | "O que levar" (RN-09) |
| Convite e vínculo | Vídeo, álbuns, datas especiais com lembrete |
| Fotos do casal (centro e lado de cada um) | Celebração em tempo real e notificações |
| Linha do tempo e contador de tempo juntos | R2 e URLs assinadas (continua em disco local) |
| Pergunta do dia | Verificação de idade por CPF (só antes de abrir ao público) |
| Pausa, saída, lixeira, exclusão de conta e exportação | Passkeys e planos pagos |
| Bloqueio, denúncia e moderação mínima | |

**Atores:** Visitante (sem login), Solteiro (conta sem vínculo), Membro (vínculo ativo ou pausado), Ex-membro, Admin.

## 2. Requisitos funcionais

Prioridade: **M** = deve ter no MVP, **S** = deveria ter. Estado: `feito`, `em andamento`, `aguarda decisão`, `—`.

### Épico A — Fundação segura (Fatia 0)

Nasce da auditoria do código de 2026-11-09.

| ID | Requisito | Pri. | Estado |
| --- | --- | --- | --- |
| RF-A01 | O convite por DuoName respeita bloqueio, perfil oculto e conta excluída, com resposta indistinguível de "não existe" (RN-30) | M | feito |
| RF-A02 | Só um convite pendente por par de pessoas | M | feito |
| RF-A03 | Refresh recusa conta excluída ou oculta | M | próximo |
| RF-A04 | Limites de tamanho em todo texto livre e no corpo JSON | M | aguarda decisão (P4) |
| RF-A05 | Rate limit nas rotas de auth, no convite por link e no resgate | M | aguarda decisão (valores) |
| RF-A06 | O limitador de taxa não gera 500 em corrida | M | feito |
| RF-A07 | Duas abas ou uma resposta perdida não derrubam todas as sessões | M | aguarda decisão (desenho) |
| RF-A08 | Tratamento global de erro: nenhuma exceção prevista vira 500 | M | feito |
| RF-A09 | Limpeza agendada de tokens, tentativas, convites vencidos e rascunhos | M | aguarda decisão (ver nota) |
| RF-A10 | Convite expirado aparece e some corretamente (RN-29) | M | feito |
| RF-A11 | **Congelamento do social**: chave de configuração desliga os endpoints (404) e a interface não mostra busca, Duollowers, contadores nem perfis de terceiros | M | feito |
| RF-A12 | Refatorações pequenas: remover `initiate` da API pública; unificar `userId(Jwt)` e o hash SHA-256 | S | não iniciado |

### Épico B — Conta e perfil pessoal (existente, melhorar)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-B01 | Entrar com Google; sair de um aparelho ou de todos (existe) | M |
| RF-B02 | DuoName, com 2 trocas por dia (existe) | M |
| RF-B03 | Perfil: nome, foto, bio até 100, profissão até 20, estado civil (existe) | M |
| RF-B04 | O servidor decodifica a imagem enviada, limita as dimensões e remove o EXIF | M |
| RF-B05 | O Personal é privado: fotos só do dono | M |
| RF-B06 | Tela do solteiro sem vínculo, com chamada para convidar ou aceitar convite | M |
| RF-B07 | Tratamento global de sessão expirada; estados de carregamento e erro unificados | M |
| RF-B08 | Melhorias do perfil a definir pelo fundador (P1) | S |

### Épico C — Vínculo (Fatia 1)

O backend e as telas já existem; falta validar de verdade.

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-C01 | Convidar por DuoName exato ou por link com código de 7 dias | M |
| RF-C02 | Aceitar, recusar e cancelar convite | M |
| RF-C03 | O `Couple` só nasce com a confirmação dos dois (RN-08) | M |
| RF-C04 | Um vínculo ativo por pessoa, mesmo sob concorrência (RN-06) | M |
| RF-C05 | História em 4 passos, revelada quando as duas estão completas (RN-25/26) | M |
| RF-C06 | Datas do casal por propor e aceitar (RN-27) | M |

**Aceitação da fatia:** o fundador e outra pessoa se vinculam em dois aparelhos reais.

### Épico D — Fotos do casal (Fatia 2)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-D01 | Enviar fotos para o centro do casal e para o lado de cada um | M |
| RF-D02 | Só membros atuais acessam (RN-12); no lado de cada um, só o dono posta e apaga (RN-12a) | M |
| RF-D03 | Miniatura gerada no servidor e carregamento sob demanda na grade | M |
| RF-D04 | Cota de fotos por casal (valor provisório, P3) | M |
| RF-D05 | Qualquer membro edita o centro; apagar manda para a lixeira (RN-11) | M |
| RF-D06 | Sem corrida na cota e sem arquivo órfão se a transação falhar | M |
| RF-D07 | Paginação por cursor na galeria | M |

### Épico E — Linha do tempo e contador (Fatia 3)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-E01 | O vínculo é o primeiro evento da linha do tempo (RN-10) | M |
| RF-E02 | Fotos e memórias em ordem cronológica, paginadas | M |
| RF-E03 | Contador de tempo juntos a partir da data de namoro acordada; sem consenso, convida a definir | M |
| RF-E04 | Memória com texto (registro com significado) | S |

### Épico F — Pergunta do dia (Fatia 4)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-F01 | Uma pergunta por dia, de um banco curado pelo Duo | M |
| RF-F02 | Cada membro responde; a resposta do parceiro aparece assim que ele responder | M |
| RF-F03 | A pergunta nunca expira; há um contador de dias, sem marca de "atrasada" | M |
| RF-F04 | As respostas viram registros na linha do tempo (DEC-023) | S |

### Épico G — Liberdade e piso mínimo (Fatia 5)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-G01 | Qualquer membro pausa e retoma (RN-13); nada é apagado | M |
| RF-G02 | Qualquer membro sai sozinho (RN-14); no MVP de 2 membros, o vínculo termina para os dois | M |
| RF-G03 | Matriz de permissões por estado (RN-11/12/12a/13a), com o Personal editável durante a pausa | M |
| RF-G04 | O término nunca apaga memórias (RN-17); cada ex-membro pode esconder o centro da própria visão e desfazer (RN-17a) | M |
| RF-G05 | Lixeira de 30 dias com limpeza automática | M |
| RF-G06 | Exclusão em massa no centro exige o consentimento dos dois (RN-11a) | M |
| RF-G07 | Excluir a própria conta dentro do app, com aviso do que será apagado (P2) | M |
| RF-G08 | Exportar os próprios dados | M |
| RF-G09 | Sem notificação pública de pausa ou término (RN-16) | M |

### Épico H — Confiança (transversal; antes do piloto)

| ID | Requisito | Pri. |
| --- | --- | --- |
| RF-H01 | Bloquear e denunciar usuário ou foto (existe); o bloqueio passa a barrar convites | M |
| RF-H02 | Papel de admin, endpoint e fila para revisar denúncias e ocultar usuário ou foto | M |
| RF-H03 | Denúncia por suspeita de menor tem prioridade | M |
| RF-H04 | Termos de uso e política de privacidade, revisados por advogado antes de abrir | M |
| RF-H05 | Registros de acesso (data, hora, IP) por 6 meses, em sigilo | M |

## 3. Requisitos não funcionais

Valores com (\*) são metas iniciais, ajustáveis.

| Área | Requisito |
| --- | --- |
| Segurança | Autorização no backend em toda operação; nenhum id do cliente é confiado; todo endpoint que toca dado de duas pessoas tem teste de acesso cruzado; sem segredo em log, repositório ou frontend; mídia só pela API autorizada |
| Privacidade (LGPD) | Dado mínimo; exportação e exclusão; EXIF removido; privado por padrão |
| Desempenho (\*) | p95 abaixo de 500 ms; miniaturas até 60 KB; tela inicial abaixo de 3 s em 4G; acesso a imagem sem N+1 |
| Confiabilidade | Migrations só para frente; backup diário com restauração testada (depende do deploy, P6); sem corrida em vínculo, cota e avatar |
| Manutenibilidade | Módulos sem ciclos (ArchUnit); cobertura medida com JaCoCo (\*meta 70% nos serviços); CI verde obrigatório |
| Usabilidade | Mobile-first, PWA; diálogos com foco, Escape e teclado; interface em português |
| Custo | R$ 0; aviso antes de qualquer serviço que exija cartão |
| Observabilidade | Logs estruturados sem dados pessoais; métricas básicas via Actuator |
| Portabilidade | Armazenamento atrás de `MediaStorageProvider` |

## 4. Arquitetura-alvo

- Continua: monólito modular, Postgres, Spring Boot, React + Vite.
- Núcleo do produto: `relationships` e `media` (este muda de Personal-first para casal-first).
- **Congelado** por chave de configuração e remoção da interface: busca por nome, Duollowers/Duollowing, perfil
  público de terceiros e fotos com visibilidade diferente de privada. **Nenhuma tabela é apagada.**
- Mantido da camada social: bloqueio e denúncia (protegem o convite).
- Nada novo de infraestrutura neste escopo (sem filas, tempo real ou R2).

## 5. Plano por fatias

| Fatia | Conteúdo | Pronto quando |
| --- | --- | --- |
| 0 | Épico A | Testes novos de acesso cruzado passam; interface sem social |
| 1 | Épico C + RF-B06/B07 | Vínculo feito em dois aparelhos reais |
| 2 | Épico D + RF-B04 | O parceiro vê a foto e um terceiro não |
| 3 | Épico E | A linha do tempo mostra o vínculo e as fotos |
| 4 | Épico F | Os dois respondem e se veem |
| 5 | Épico G | Sair não apaga nada de ninguém |
| Antes do piloto | Épico H, deploy e backups | Denúncia revisada sem tocar no banco |
| Piloto | Fundador + 4 casais | 3 casais voltam sozinhos por semana |

**Definição de pronto (toda fatia):** build verde, testes passando, teste de acesso cruzado quando houver dado de
duas pessoas, changelog, `docs/MAPA.md` e glossário atualizados, commit citando RF/RN.

## 6. Rastreabilidade com a auditoria

| Achado | Tratado em |
| --- | --- |
| Convite ignora bloqueio, oculto e excluído | RF-A01, A02 |
| Texto sem limite | RF-A04 |
| Conta nunca revalidada | RF-A03 |
| Upload só confere assinatura | RF-B04, D03 |
| Sem rate limit em auth e convites | RF-A05 |
| Tabelas crescem sem limpeza; convite expira só ao abrir | RF-A09, A10 |
| Corridas (limitador, cota, avatar) | RF-A06, D06 |
| Sessão derrubada com duas abas | RF-A07 |
| Grade com fotos originais | RF-D03 |
| Sem moderação | RF-H02, H03 |
| Sem exclusão de conta | RF-G07 |
| Erros 500 genéricos | RF-A08 |
| Erro e carga duplicados no frontend | RF-B07 |

## 7. Pendências que dependem do fundador

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

Também aguardam decisão: valores do rate limit (RF-A05) e o desenho da correção da corrida de abas (RF-A07).

## 8. Andamento (2026-11-09)

Feitos na Fatia 0, com testes e `./gradlew build` verde (backend) e `npm test`, lint e prettier verdes (frontend):
RF-A01, A02, A06, A08, A10, A11.

- **RF-A09 (limpeza agendada):** não foi feito. A decisão do fundador na Etapa 12 era limpeza *lazy* para convites e
  rascunhos, e isso conflita com o requisito; falta decidir. A limpeza de `refresh_tokens` e `rate_limit_attempts`
  não tem esse conflito e fica como próximo passo.
- **RF-A03, A04, A05, A07, A12:** não iniciados (A04, A05 e A07 aguardam decisão; A03 e A12 não exigem).
- **Consequência do congelamento (RF-A11):** com a camada social desligada, denunciar um perfil
  (`POST /api/profiles/{handle}/report`) passa a responder 404, porque a denúncia exige que o perfil seja
  "visível" pela regra central de acesso. Para o convite de vínculo isso precisa de uma decisão (RF-H01).
