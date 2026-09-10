# Contribuer

GuildKeeper est le dépôt souche du cours **Tests Unitaires et Logiciels**. Ces notes s'adressent aux intervenants qui font évoluer le support.

## Mise en route

```bash
# Backend Java (JDK 21+)
cd backend-java && ./mvnw test

# Client TypeScript (Node ≥ 22.18, voir quests-ts/.nvmrc)
cd quests-ts && npm install && npm test
```

## Développement local (serveur + CLI)

Deux terminaux :

```bash
# Terminal 1 — le serveur
cd backend-java
GUILDKEEPER_ENV=dev ./mvnw -q compile exec:java     # http://localhost:7070, jeu de démo
```

```bash
# Terminal 2 — le CLI
cd quests-ts
npm run cli -- report Dragan
npm run cli                    # mode interactif
```

`GUILDKEEPER_API_URL` surcharge l'URL cible du CLI.
[`docs/api-examples.http`](docs/api-examples.http) sert de **référence de l'API**
(routes, corps, codes d'erreur) et de requêtes exécutables, écritures comprises.

## Règles à respecter

- **Règle d'or** : aucun test sur le module `finance`, aucune implémentation de
  la distribution de dividendes. Ce sont les livrables des étudiants.
- Les tests à compléter portent `@Tag("todo")` (Java) / `todo(...)` (Vitest) :
  ils sont **exclus du run par défaut** (qui doit rester vert). Pour les voir :
  `./mvnw test -Ptodo` et `npm run test:todo`. Ne pas les compléter.
- Pattern **AAA**, nommage `should_..._when_...` (Java) / descriptif (TS).
- **Prénoms des membres** : dans les exemples et les tests, préférer des prénoms
  distinctifs (Albéric, Attila, Dragan, Dorian, Dante, Ektor…) plutôt que des
  noms de fantasy génériques. Convention de rédaction, pas une règle : `recruit()`
  accepte tout nom non vide.

## Avant de committer

```bash
cd backend-java && ./mvnw verify        # tests + JaCoCo + jar + spotless:check
cd quests-ts && npm run check           # typecheck + tests
cd quests-ts && npm run lint && npm run format:check
```

L'architecture du dépôt est décrite dans
[`docs/architecture.md`](docs/architecture.md).
