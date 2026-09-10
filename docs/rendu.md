# GuildKeeper - Projet final

**Nom :**
**Date :**
**Dépôt Git :**

> Ce fichier a deux rôles : la checklist ci-dessous sert de suivi pendant les 3 heures, la synthèse en fin de fichier est le livrable 5. Garder la synthèse sur une page maximum.

---

## Suivi des tâches

Le détail de chaque livrable est dans les slides du projet. Cette checklist ne reprend que la progression TDD des dividendes, où l'oubli d'un cas coûte des points, et les contrôles à passer avant le rendu.

### Livrables (cocher quand terminé)

- [ ] Livrable 1 : suite de tests complète de `GuildFinanceService`
- [ ] Livrable 2 : `finance.feature` et ses step definitions Cucumber
- [ ] Livrable 3 : `distributeDividends` développé en TDD (détail ci-dessous)
- [ ] Livrable 4 : rapport de couverture généré
- [ ] Livrable 5 : synthèse écrite ci-dessous

### Livrable 3 - Progression TDD des dividendes

Un cycle rouge -> vert -> refactor à chaque palier, chaque test écrit avant le code de production.

- [ ] palier 1 : guilde vide -> répartition retournée vide, compte inchangé (test rouge imposé, à écrire en premier)
- [ ] palier 2 : un seul membre -> il reçoit toute l'enveloppe, le compte est débité d'autant
- [ ] palier 3 : deux membres de rangs différents -> parts au prorata des poids, reliquat laissé sur le compte
- [ ] palier 4 : `@ParameterizedTest` sur `p` invalide (`0`, `-5`) -> `InvalidAmountException`, compte inchangé
- [ ] palier 5 : `p > 100`, un seul membre, solde `100`, `p = 200` -> `checkSolvency` renvoie `false` -> `InsufficientFundsException`, compte inchangé
- [ ] palier 6 : le solde ne devient jamais négatif

### Contrôles avant rendu

- [ ] `./mvnw test` et `npm test` verts
- [ ] `./mvnw test -Ptodo` vert : plus aucun message « Test à compléter »
- [ ] `npm run test:todo` vert
- [ ] couverture du module `finance` supérieure ou égale à 80 %
- [ ] aucun test flaky : la suite passe aussi quand l'ordre des tests change
- [ ] méthodes existantes de `GuildFinanceService` non modifiées (hors `distributeDividends`)

---

## Synthèse écrite (livrable 5, une page maximum)

### Niveau de couverture retenu

Couverture obtenue sur le module `finance` : ... %

Pourquoi ce niveau : quelles lignes ou branches restent non couvertes, et pourquoi c'est acceptable ou non.

### Choix de stratégie de test

- Unitaire contre bout-en-bout : ce qui est testé en isolation, ce qui passe par Cucumber, et pourquoi.
- Usage de Mockito : sur quelles dépendances, stub (`thenReturn`) ou mock (`verify`), et la raison.
- Paramétrage : quels cas regroupés en `@ParameterizedTest`, quelle source de données.
- Données de test : comment les comptes et les membres sont construits, comment le déterminisme est garanti.

### Problèmes rencontrés et solutions

- Problème : ...
  Solution : ...
- Problème : ...
  Solution : ...
