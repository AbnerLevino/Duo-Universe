# Plano: reorganização dos pacotes do backend por camada técnica

> Status: **EXECUTADO em 2026-11-06** (4 etapas, um commit por etapa, build verde com 151 testes em todas).
> Decisão retomada em 2026-11-05 (a proposta original tinha ficado sem aplicar, ver seção 19 do `CLAUDE.md`,
> bloco "Decisão de organização"). Desta vez o fundador confirmou que quer aplicar.
>
> **Commits:** Etapa 1 `3a8a8a0` (shared), Etapa 2 `eebbc96` (users), Etapa 3 `83aa5c8` (auth),
> Etapa 4 `42ca045` (relationships).
>
> **O que o plano não previa e foi preciso ajustar** (só visibilidade e posição de testes; nenhuma regra de
> negócio mudou): como quase todas as classes de implementação são *package-private*, separar em pastas diferentes
> quebrou acessos que só funcionavam "por estar no mesmo pacote". Ficaram `public`: `RefreshCookies` (classe e
> `set`/`clear`/`read`), `JwtConfig` (classe e `ISSUER`) e `CouplePairing.requireConfirmed()`. Os testes de
> `internal/` foram para o mesmo pacote da classe que testam; `RelationshipsHttpTest` foi para
> `relationships/controller/` (usa tipos aninhados package-private de `CouplePairingController`).

## Motivo

A convenção atual (raiz do pacote do módulo = API pública; `internal/` = implementação) já existe e é guardada
pelo `ArchitectureTest`. Ela separa módulos de negócio (`auth`, `users`, `relationships`, `shared` — DEC-020),
mas dentro de cada módulo os arquivos estão todos soltos, sem separação por tipo técnico. O módulo
`relationships`, por exemplo, tem ~20 arquivos soltos na raiz (controllers, exceções, DTOs, enums, interfaces)
e mais 10 dentro de `internal/`. Este plano organiza cada módulo por camada técnica, **sem mudar nenhuma regra
do `ArchitectureTest`**: ele só olha o 3º segmento do pacote (nome do módulo) e se algo está em
`com.duo.*.internal..`; subpastas abaixo disso não afetam nenhuma das três regras (ciclos, controller não
toca repository, `internal` só é usado pelo próprio módulo).

## Convenção fixada

- Raiz pública do módulo: `controller/`, `exception/`, `dto/`.
- Interfaces de serviço e enums continuam **soltos na raiz** — são o contrato do módulo, não encaixam em
  nenhuma das três pastas acima.
- `internal/`: `model/` (entidades `@Entity`), `repository/`, `service/` (`*ServiceImpl` e orquestradores
  como `CoupleFactory`).
- **Sobras** (configs `@Configuration`, o handler `GoogleLoginSuccessHandler`, o utilitário `RefreshCookies`):
  ficam soltos onde estão por ora, sem pasta nova (`config/` não foi pedida nesta rodada). Se incomodar depois,
  vira uma etapa própria.

## Ordem de execução

Uma etapa por módulo, do menor para o maior. Cada etapa, isolada:

1. `git mv` de cada arquivo para a subpasta nova (preserva histórico no `git log`).
2. Atualizar a linha `package` em cada arquivo movido.
3. Atualizar os `import` que apontam para essas classes — nos outros módulos (ex.: `CoupleSummary` é usado
   fora de `relationships`) e **nos testes** (hoje vários testes importam `com.duo.<modulo>.internal.*`
   direto).
4. `./gradlew build` — compila, Spotless e `ArchitectureTest` continuam valendo sem alteração.
5. Atualizar `docs/MAPA.md` com a árvore nova do módulo.
6. Changelog (regra 6 do `CLAUDE.md`) e **pausa para aprovação** antes da próxima etapa.

---

### Etapa 1 — `shared`

```
shared/
  exception/   DuoHttpException.java, GlobalExceptionHandler.java, RateLimitExceededException.java
  RateLimiter.java, Scopeable.java, Scope.java        ← soltos (contratos)
  ClockConfig.java, SecurityConfig.java                ← soltos (configs)
  internal/
    model/        RateLimitAttempt.java
    repository/   RateLimitAttemptRepository.java
    service/      RateLimiterImpl.java
```

### Etapa 2 — `users`

```
users/
  controller/   UserController.java
  exception/    EmailAlreadyLinkedException.java, HandleTakenException.java, InvalidHandleException.java
  dto/          UserSummary.java
  UserService.java                                     ← solto (contrato)
  internal/
    model/        User.java
    repository/   UserRepository.java
    service/      UserServiceImpl.java
```

### Etapa 3 — `auth`

```
auth/
  controller/   AuthController.java, MeController.java
  dto/          AccessToken.java, IssuedRefreshToken.java
  AccessTokenService.java, RefreshTokenService.java    ← soltos (contratos)
  GoogleLoginSuccessHandler.java, RefreshCookies.java   ← soltos (sobras)
  internal/
    model/        RefreshToken.java
    repository/   RefreshTokenRepository.java
    service/      JwtAccessTokenService.java, RefreshTokenServiceImpl.java
    JwtConfig.java                                      ← solto (config)
```

### Etapa 4 — `relationships` (o maior, por último)

```
relationships/
  controller/   CoupleController.java, CouplePairingController.java
  exception/    AlreadyInRelationshipException.java, CoupleNotFoundException.java,
                DatesNotAcceptedException.java, HandleNotInvitableException.java,
                IllegalCoupleTransitionException.java, IllegalPairingOperationException.java,
                IllegalPairingTransitionException.java, InviteCodeInvalidException.java,
                PairingNotFoundException.java, StoryIncompleteException.java
  dto/          CoupleSummary.java, PairingSummary.java, StorySummary.java, IssuedPairingInvite.java
  Channel.java, CoupleState.java, PairingState.java, StoryStep.java   ← soltos (enums do contrato)
  CouplePairingService.java, CoupleService.java, CouplePermissions.java  ← soltos (contratos)
  internal/
    model/        Couple.java, CoupleMember.java, CouplePairing.java, CouplePairingStory.java
    repository/   CoupleRepository.java, CoupleMemberRepository.java,
                  CouplePairingRepository.java, CouplePairingStoryRepository.java
    service/      CoupleServiceImpl.java, CouplePairingServiceImpl.java, CoupleFactory.java
```
