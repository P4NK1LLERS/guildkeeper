# GuildKeeper - Projet final

**Nom : Falc'hun Victor**
**Date : 10/09/2026 **
**Dépôt Git : https://github.com/P4NK1LLERS/guildkeepe **

> Ce fichier a deux rôles : la checklist ci-dessous sert de suivi pendant les 3 heures, la synthèse en fin de fichier est le livrable 5. Garder la synthèse sur une page maximum.

---

## Suivi des tâches

Le détail de chaque livrable est dans les slides du projet. Cette checklist ne reprend que la progression TDD des dividendes, où l'oubli d'un cas coûte des points, et les contrôles à passer avant le rendu.

### Livrables (cocher quand terminé)

- [x] Livrable 1 : suite de tests complète de `GuildFinanceService`
- [x] Livrable 2 : `finance.feature` et ses step definitions Cucumber
- [x] Livrable 3 : `distributeDividends` développé en TDD (détail ci-dessous)
- [x] Livrable 4 : rapport de couverture généré
- [x] Livrable 5 : synthèse écrite ci-dessous

### Livrable 3 - Progression TDD des dividendes

Un cycle rouge -> vert -> refactor à chaque palier, chaque test écrit avant le code de production.

- [x] palier 1 : guilde vide -> répartition retournée vide, compte inchangé (test rouge imposé, à écrire en premier)
- [x] palier 2 : un seul membre -> il reçoit toute l'enveloppe, le compte est débité d'autant
- [x] palier 3 : deux membres de rangs différents -> parts au prorata des poids, reliquat laissé sur le compte
- [x] palier 4 : `@ParameterizedTest` sur `p` invalide (`0`, `-5`) -> `InvalidAmountException`, compte inchangé
- [x] palier 5 : `p > 100`, un seul membre, solde `100`, `p = 200` -> `checkSolvency` renvoie `false` -> `InsufficientFundsException`, compte inchangé
- [x] palier 6 : le solde ne devient jamais négatif

### Contrôles avant rendu

- [ ] `./mvnw test` et `npm test` verts
- [x] `./mvnw test -Ptodo` vert : plus aucun message « Test à compléter »
- [ ] `npm run test:todo` vert
- [x] couverture du module `finance` supérieure ou égale à 80 %
- [ ] aucun test flaky : la suite passe aussi quand l'ordre des tests change
- [x] méthodes existantes de `GuildFinanceService` non modifiées (hors `distributeDividends`)

---

## Synthèse écrite (livrable 5, une page maximum)

### Niveau de couverture retenu

Couverture obtenue sur le module `finance` : 100 % (instructions et branches, rapport JaCoCo via `mvn verify`).

Pourquoi ce niveau : les 5 classes du module (`GuildAccount`, `GuildAccountRepository`/`InMemoryGuildAccountRepository`, `GuildFinanceService`, `InvalidAmountException`, `InsufficientFundsException`) sont entièrement exercées par les tests unitaires (`GuildAccountTest`, `GuildFinanceServiceTest`) et par le scénario Cucumber (`finance.feature`). Aucune ligne ni branche non couverte : le module est petit et sans effet de bord caché (pas d'I/O, pas d'aléatoire, pas d'horloge), donc chaque chemin - nominal, montant/pourcentage invalide, solde insuffisant, guilde vide, reliquat, arrondi - a pu être testé explicitement sans complexité combinatoire excessive.

### Choix de stratégie de test

- Unitaire contre bout-en-bout : les règles métier (validation, calcul des parts, solvabilité) sont testées en isolation dans `GuildFinanceServiceTest`, avec `GuildAccountRepository` mocké - aucune dépendance réelle sollicitée. Un seul scénario Cucumber (`finance.feature`) vérifie le parcours de bout en bout (distribution nominale + rejet pour solde insuffisant) au niveau domaine, sans passer par l'API HTTP, sur le modèle de `recruitment.feature`.
- Usage de Mockito : uniquement sur `GuildAccountRepository`, seule dépendance du service. `verify` / `verifyNoInteractions` / `verifyNoMoreInteractions` pour contrôler les interactions (le repository n'est sollicité qu'en cas de succès, jamais si une exception est levée avant), `doAnswer` / `doThrow` pour observer l'état au moment précis de l'appel à `save(...)` ou simuler une panne - `save` étant `void`, un simple `thenReturn` n'aurait pas suffi.
- Paramétrage : `@ParameterizedTest` + `@ValueSource` pour les cas invalides qui partagent une seule intention - montants/pourcentages non positifs (`deposit`, `distributeDividends`) et bornes de solvabilité (`checkSolvency`) - évite de dupliquer le même corps de test pour chaque valeur testée.
- Données de test : comptes créés via un helper `accountWith(balance)` (id fixe `"g-1"`), membres via `Member.novice(id, nom, luck)` ou le constructeur complet avec un rang explicite, avec des ids/noms fixes. Aucune donnée aléatoire ni dépendance au temps système dans le module : le déterminisme est garanti par construction.

### Problèmes rencontrés et solutions

- Problème : `JAVA_HOME` pointait vers un JRE 8 (pas de compilateur disponible) au lieu du JDK 21 requis par le projet, et se réinitialisait à chaque nouveau terminal.
  Solution : repéré un JDK Temurin 21 déjà installé localement par IntelliJ, et fixé `JAVA_HOME` dessus pour la session de build.
- Problème : le rapport texte de Surefire affichait « Tests run: 0 » pour `GuildFinanceServiceTest`, qui regroupe une partie de ses tests dans des classes `@Nested`.
  Solution : limitation connue de Surefire avec les classes `@Nested` en JUnit 5 (mauvais compte dans le résumé `.txt` par classe) - le compte réel est vérifié via le rapport XML et le résumé global `Results:` en fin de build.
