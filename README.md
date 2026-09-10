# GuildKeeper

Système de gestion de guilde RPG, support du cours **Tests Unitaires et
Logiciels** (B3 Architecture du Logiciel, ESGI).

## À quoi sert ce dépôt

C'est le **projet souche** remis aux étudiants pour le projet final :

- un **backend Java** = serveur HTTP (Javalin) exposant les règles métier
  sous `/api/v1` ;
- un **client TypeScript** (`@guild-keeper/progression-client`) = client HTTP
  lecture seule + logique de progression + CLI ;
- une **suite de tests partiellement écrite** : des tests complets servent
  d'exemple, d'autres sont **à compléter** — marqués `@Tag("todo")` (Java) /
  `todo(...)` (Vitest), **exclus du run par défaut** pour que `./mvnw test` et
  `npm test` restent verts. Pour les voir : `./mvnw test -Ptodo` /
  `npm run test:todo` ;
- le module `finance`, **fonctionnel mais sans aucun test** : c'est le cœur du
  projet final. Les instructions à suivre seront données avec le projet final.

L'architecture du dépôt est décrite dans [`docs/architecture.md`](docs/architecture.md).

## Arborescence

```
guildkeeper/
├── backend-java/     Serveur Javalin /api/v1, règles métier, persistance SQLite, features Cucumber
├── quests-ts/        @guild-keeper/progression-client : client HTTP + planner + CLI
├── docs/             Architecture, exemples d'API
└── README.md
```

Modules Java (`fr.dev.sensei.guild.keeper.*`) : `experience`, `recruitment`,
`missions`, `rewards`, `promotion`, `finance`, `http` (serveur, couvert par les
tests) et `persistence.sqlite` (adaptateurs, hors couverture). Les tests du
domaine utilisent **toujours** les repositories in-memory ; les implémentations
SQLite ne servent qu'au serveur.

## Prérequis

- **JDK 21+** — Maven via le wrapper (`./mvnw`), rien à installer.
- **Node.js ≥ 22.18** (voir [`quests-ts/.nvmrc`](quests-ts/.nvmrc)) — le CLI est
  lancé directement en `.ts` grâce au *type stripping* natif de Node.

## Backend Java

Depuis `backend-java/` :

```bash
./mvnw test        # compile + tests (spotless:check inclus)
./mvnw verify      # + JaCoCo + jar exécutable target/guildkeeper.jar
./mvnw test -Ptodo # exécute UNIQUEMENT les tests à compléter (tous rouges, build non interrompu)
```

- Rapport JaCoCo : `backend-java/target/site/jacoco/index.html`.
- Le module `finance` apparaît à **0 % de couverture** : c'est volontaire, il
  n'est pas exclu.
- `persistence.sqlite` (adaptateurs, sans test unitaire attendu) est exclu du
  rapport ; `http` **n'est pas** exclu (couvert par `JavalinTest`).
- Formatage minimal via **Spotless** (imports inutilisés, espaces, newline
  finale) : `./mvnw spotless:apply` corrige, `verify` vérifie.

### Cucumber

Disposition standard Maven, scénarios **en français** (`# language: fr`) :

- `src/test/resources/features/recruitment.feature` ;
- `src/test/java/fr/dev/sensei/guild/keeper/cucumber/RecruitmentSteps.java`
  (annotations `io.cucumber.java.fr` : `@Soit`, `@Quand`, `@Alors`, `@Et`).

Exécutés par `./mvnw test` via `RunCucumberTest`. `surefire` affiche
`Tests run: 0` pour la suite (limitation connue) ; le détail par scénario est
dans `target/cucumber-reports/Cucumber.xml`, remonté par la CI.

## Client TypeScript

Depuis `quests-ts/` (`npm install` d'abord) :

| Script | Rôle |
|---|---|
| `npm run test:watch` | boucle TDD (Vitest en watch) |
| `npm test` | tests (les `todo(...)` sont ignorés → **vert**) |
| `npm run test:todo` | exécute les tests à compléter (rouges) |
| `npm run test:contract` | contrat contre un serveur réel (voir CI) |
| `npm run typecheck` / `lint` / `format` | qualité |
| `npm run check` | typecheck + lint + format:check + tests (gate CI) |
| `npm run cli -- <cmd>` | lance le CLI (voir plus bas) |
| `npm run coverage` / `build` / `clean` | couverture / dist / nettoyage |

`src/index.ts` ré-exporte tout. Couverture : `src/client/dto.ts` (interfaces) et
`src/cli/main.ts` (câblage I/O) sont exclus.

- **`src/client/`** — client HTTP **lecture seule** :
  `createGuildKeeperClient({ baseUrl })` → `quests.list/get/rewardPreview`,
  `members.list/get/assignments`, `guild()`. Transport avec délai maximum, réessais
  (5xx / 429) et erreurs typées (`NotFoundError`, `ValidationError`, `ApiError`,
  `NetworkError`). DTO écrits à la main, réponses réelles dans `fixtures/`.
- **`src/planner/`** — fonctions pures nourries par les DTO : `unlockTree`
  (`isQuestUnlocked`, `availableQuests`, lookup), `simulate`
  (`calculateExperienceReward`, `calculateLoot`, `simulateReward` — **indicatif**,
  le serveur fait foi), `recommend`, `progressionReport`.
- **`src/cli/`** — sous-commandes façon `git`, parseur maison (voir ci-dessous).

## Développement local

Deux terminaux :

```bash
# Terminal 1 — le serveur
cd backend-java
GUILDKEEPER_ENV=dev ./mvnw -q compile exec:java     # http://localhost:7070 + jeu de démo
```

```bash
# Terminal 2 — le CLI
cd quests-ts
npm run cli -- report Dragan
npm run cli                    # mode interactif (REPL, sans état)
```

- Serveur : port `GUILDKEEPER_PORT` (défaut 7070), base `GUILDKEEPER_DB` (défaut
  `guildkeeper.db`), schéma + catalogue créés au démarrage. `GUILDKEEPER_ENV=dev`
  seede quelques membres et attributions.
- CLI : `GUILDKEEPER_API_URL` (défaut `http://localhost:7070`).
- **Référence de l'API** (toutes les routes, corps, codes d'erreur) + requêtes
  exécutables : [`docs/api-examples.http`](docs/api-examples.http) (format REST
  Client / client HTTP IntelliJ). Les écritures (recruter, assigner, déposer…)
  passent par là.

### Commandes du CLI

```bash
npm run cli -- quests list [--json]
npm run cli -- quests show <id> [--json]
npm run cli -- members list [--json]
npm run cli -- unlock-status "<quête>" [--completed "<quête>"]… [--json]
npm run cli -- plan <membre> [--json]
npm run cli -- simulate reward "<quête>" --luck <1-10> [--json]
npm run cli -- report <membre> [--json]
npm run cli -- help [<commande>]
```

## Intégration continue

À chaque push (toutes branches). **Les deux suites sont vertes** ; une erreur
réelle (compilation, lint, format, test complété qui casse) fait échouer le
pipeline.

- **`.gitlab-ci.yml`** — CI de référence. `backend-java` (`./mvnw verify`),
  `quests-ts` (`npm run check`), puis **`contrat-cross-stack`** : démarre le jar
  et lance `npm run test:contract` (DTO ⇄ réponses réelles, `simulateReward` ⇄
  `reward-preview`). Rapports JUnit (surefire + Cucumber + Vitest) dans l'onglet
  *Tests*.
- **`.github/workflows/ci.yml`** — miroir GitHub Actions, **non testé** (voir le
  commentaire en tête de fichier).

## Conventions

- **Prénoms des membres** (convention de rédaction, *pas* une règle métier) :
  dans les exemples et les tests, préférer des prénoms distinctifs — Albéric,
  Attila, Dragan, Dorian, Dante, Ektor… — plutôt que des noms de fantasy
  génériques. `recruit()` accepte n'importe quel nom non vide.
- Nommage des tests : `should_..._when_...` (Java) / `"…"` descriptif (TS).
- Pattern **AAA** (Arrange / Act / Assert).
- Fins de ligne LF (`.gitattributes` + `.editorconfig`).

Voir [`CONTRIBUTING.md`](CONTRIBUTING.md) pour faire évoluer le support.
