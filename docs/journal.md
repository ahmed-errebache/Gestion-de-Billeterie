# Journal de bord - TP Billetterie de concerts (R5.08)

Binôme : Ahmed Errebache / Lucas Charpentier. Ce journal alimente le compte rendu.
Règle : on y note ce qui s'est réellement passé, y compris **qui a produit quoi** (nous ou l'IA).
Captures dans `docs/captures/`, nommées `<étape>-<numéro>-<sujet>.png`.

## Rôle de l'IA dans ce projet
- Outil utilisé : Claude (Anthropic), en conversation, avec accès au dossier du projet sur le poste d'Ahmed.
- Principe : l'IA propose et génère, nous relisons, testons et corrigeons. Chaque entrée indique ce qui vient de l'IA, ce que nous avons vérifié et ce que nous avons modifié.

---

## Étape A0 - Dépôt et squelette

### Ce qui a été fait
| Élément | Produit par | Vérifié / décidé par |
|---|---|---|
| Création du dépôt GitHub vide `Gestion-de-Billeterie` | Ahmed | - |
| Analyse du sujet, répartition des tâches Ahmed / Lucas (PDF) | IA, à la demande d'Ahmed | Ahmed (durées retirées, aucune tâche commune, dépôt à sa charge) |
| `pom.xml`, `compose.yaml`, `application.properties`, `.gitignore`, arborescence des packages, `BilletterieApplication`, `ClockConfig`, `CONVENTIONS.md` | **IA** (fichiers écrits dans le dossier du projet d'Ahmed, à sa demande) | **À faire par Ahmed** : relire chaque fichier, lancer le build, noter ses corrections ci-dessous |
| Commits et push vers GitHub | Ahmed | - |

### Décisions et choix
1. **Pas d'archive de départ** : l'enseignant demande de partir de zéro, le squelette est donc créé de toutes pièces.
2. **Un seul endroit pour Java 25** : propriété `java.version=25` du parent Spring Boot, aucun `source` / `target` / `release` explicite (risque de valeurs contradictoires cité dans le sujet).
3. **Scopes** : `mysql-connector-j` et `flyway-mysql` en `runtime`, tous les starters de test en `test`.
4. **`spring-boot-maven-plugin` dans `build`**, pas dans `dependencies` (erreur typique que le sujet signale).
5. **Pas de Lombok ni MapStruct** : facultatifs, et un ancien Lombok peut échouer avec Java 25.
6. **`Clock` en bean** (`ClockConfig`) pour piloter les dates dans les tests sans `sleep`.
7. **Packages par couche** (`domain`, `repository`, `service`, `web.api`, `web.ui`, ...) : séparation nette contrôleurs fins / services, et pages HTML distinctes de l'API.
8. **Actuator** : seul `health` exposé, sans détails.
9. **Thymeleaf ajouté au pom** car le prof a ajouté la partie interface ; la dépendance ne sera conservée que si les pages existent.

### Limites rencontrées (à documenter avec capture)
- **Le Maven Wrapper n'a pas pu être généré par l'IA** (son environnement n'atteignait pas Maven Central) : le build n'a donc **pas** été vérifié par l'IA.
  - À faire par Ahmed : `mvn wrapper:wrapper -Dmaven=3.9.11` (ou depuis IntelliJ), puis `.\mvnw.cmd -version` et `.\mvnw.cmd clean verify`.
  - 📸 Capture : sortie de `.\mvnw.cmd -version` montrant Java 25.

### Erreurs rencontrées et solutions
*(à compléter par Ahmed avec ses captures : symptôme, cause, correction)*

| # | Symptôme (capture) | Cause | Correction |
|---|---|---|---|
| | | | |

### Prompts de l'IA
1. « analyser ce tp et on discute ne rien faire » (pièce jointe : le sujet PDF) - Réponse : analyse des risques du sujet (concurrence sous MySQL, scénario des 5 places, sécurité, particularités Spring Boot 4).
2. « c'est un tp à deux je suis avec lucas charpentier je veux que tu répartisse les taches du sujet à deux » - Réponse : découpage par couches.
3. « le prof nous a ajouté la partie interface avec thymeleaf html » - Réponse : impacts (CSRF, deux chaînes de sécurité, cloisonnement du `ControllerAdvice`).
4. « faire un pdf avec les taches détaillé de chacun » puis « il y a pas de timing... je m'occupe de la création repo git ne fait pas tache ensemble » - Réponse : PDF de répartition, deux versions.
5. « la on va attaquer mes taches tu vas me guider de pas à pas jusqu'au bout... » - Réponse : mise en place de ce journal.
6. « fait cloner ce repo ... et ajout l'architecture » puis « tu dois les créer depuis mon ordinateur que t'as accès » - Réponse : squelette décrit ci-dessus, écrit dans le dossier du projet.

### Corrections apportées à ce que l'IA a produit
*(à compléter par Ahmed : ce qu'il a changé, pourquoi)*

---

## Étapes A1 à A11 - Données, API publique, erreurs, exploitation, pages, tests, README

### Ce qui a été fait
Tout le code de ces étapes a été **écrit par l'IA** (Claude), à la demande d'Ahmed (« fais toutes mes tâches »), dans le dossier du projet.
**Il n'a été ni compilé ni exécuté par l'IA** : son poste de travail n'avait ni Maven, ni Docker, ni accès à Maven Central.
Ahmed doit donc relire, compiler, tester et consigner ci-dessous ce qu'il constate.

| Étape | Fichiers principaux | Vérifié / corrigé par Ahmed |
|---|---|---|
| A1 Schéma | `V1__schema.sql` | à faire |
| A2 Données | `V2__donnees_demo.sql` | à faire |
| A3 Entités et repositories | `domain/*`, `repository/*` | à faire |
| A4 Exceptions | `exception/*` | à faire |
| A5 Lecture | `ConcertApiController`, `ConcertService`, `ConcertSpecifications` | à faire |
| A6 Création | `ClientApiController`, `ProductionApiController`, `AdminApiController`, services, DTO | à faire |
| A7 Erreurs | `ApiExceptionHandler`, `ErreurApi` | à faire |
| A8 Exploitation | `application.properties`, `OpenApiConfig` | à faire |
| A9 Tests | `ConcertServiceTest`, `ConcertRepositoryJpaTest`, `MySqlIntegrationTest` | à faire (Docker requis pour 2 des 3) |
| A10 Pages | `web/ui/*`, `templates/*`, `static/css/style.css` | à faire |
| A11 README | `README.md` (sections d'Ahmed ; Lucas complète les siennes) | à faire |

### Décisions et choix
1. **Verrou de vente prêt dans le repository** : `ConcertRepository.findByIdPourVente` (`PESSIMISTIC_WRITE`) pour que Lucas l'utilise en premier dans sa transaction (sur MySQL en REPEATABLE READ, une lecture non verrouillante avant le verrou peut figer un snapshot périmé).
2. **Places restantes** = capacité de la salle moins `totalVendu` (requête `coalesce(sum(quantite), 0)`).
3. **Filtres du/au/artiste** par `Specification` (critères facultatifs sans le piège `:p is null or ...` de MySQL/Hibernate), avec `fetch` de l'artiste et de la salle pour éviter le N+1.
4. **Date de concert future** contrôlée dans le service avec le `Clock` injecté (et non `@Future`, qui utilise l'horloge système), donc testable sans `sleep`.
5. **Erreurs** : hiérarchie `BilletterieException` (404 / 409 / 400) pour ne pas multiplier les handlers ; `@RestControllerAdvice` limité à `web.api` ; pas de handler générique sur `Exception` pour laisser Spring Security produire 401 et 403.
6. **Filet 409** sur `DataIntegrityViolationException` : si deux requêtes violent en même temps une unicité, la réponse reste un JSON stable sans détail SQL.
7. **Pages Thymeleaf** : contrôleurs séparés de l'API, qui appellent directement les services ; erreurs HTML gérées par un `@ControllerAdvice` limité à `web.ui`.
8. **Tests JPA sur MySQL réel** (Testcontainers, `replace = NONE`) plutôt que sur une base embarquée, comme le sujet l'impose.
9. **Contrainte « une seule carte active par client »** : non exprimable en SQL (dépend de la date), donc contrôlée par Lucas dans le service.

### Points à vérifier au premier `clean verify` (hypothèses de l'IA, non confirmées)
- Noms de packages des annotations de test avec Spring Boot 4 : `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` et `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`.
- Testcontainers 2 : classe `org.testcontainers.mysql.MySQLContainer`.
- Thymeleaf : `#temporals` disponible sans dépendance supplémentaire.
- Le cast `(Join<Object, Object>) root.fetch("artiste")` dans `ConcertSpecifications`.
- Nom exact du starter `spring-boot-starter-flyway` (tiré de l'annexe du sujet).
- Imports Swagger (`io.swagger.v3.oas.models.OpenAPI`) fournis par springdoc 3.1.1.

### Dépendances avec le travail de Lucas
- **Sans `SecurityConfig`**, Spring Security verrouille tout : aucune page ni aucun endpoint n'est testable. Lucas doit ouvrir au minimum `/`, `/concerts/**`, `/inscription/**`, `/css/**`, `/error`, `GET /api/concerts/**`, `POST /api/clients`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/actuator/health`.
- CSRF : à ignorer sur `/api/**` seulement (les formulaires Thymeleaf ajoutent le jeton automatiquement).
- Les exceptions `JaugeInsuffisanteException`, `CarteDejaActiveException`, `ClientIntrouvableException` sont prêtes, ainsi que les repositories `CarteFideliteRepository` et `AchatBilletsRepository`.

### Erreurs rencontrées et solutions
*(à compléter par Ahmed avec ses captures)*

| # | Symptôme (capture) | Cause | Correction |
|---|---|---|---|
| | | | |

### Prompts de l'IA
7. « aller fait tous mes taches et après on va discuter » - Réponse : écriture de A1 à A11 (voir ci-dessus), sans compilation possible.

### Corrections apportées à ce que l'IA a produit
*(à compléter par Ahmed)*

---

## Consigne Docker du prof : le projet doit démarrer sous Docker

### Ce qui a été fait (par l'IA, non testé)
- `Dockerfile` en deux étapes : construction du JAR avec le Maven Wrapper sur `eclipse-temurin:25-jdk`, puis exécution sur `eclipse-temurin:25-jre` avec un utilisateur non root.
- `compose.yaml` : service `app` ajouté à `mysql`, avec `depends_on` sur le contrôle de santé de MySQL et `DB_URL` pointant sur le nom du service (`mysql`).
- `.dockerignore` (target, .git, docs, .env) et `.gitattributes` (`mvnw` en LF, `*.cmd` en CRLF).

### Décisions et choix
1. **Application et base démarrent ensemble** par `docker compose up -d --build`.
2. **`depends_on: service_healthy`** : sans cela l'application peut démarrer avant MySQL et échouer au premier accès Flyway.
3. **Tests sautés dans l'image** (`-DskipTests`) : Testcontainers a besoin du démon Docker, absent pendant `docker build`. Les tests restent exécutés par `mvnw clean verify` sur le poste.
4. **Fins de ligne de `mvnw`** : un `mvnw` en CRLF échoue sous Linux (`/bin/sh^M: bad interpreter`). Double protection : `.gitattributes` et un `sed` dans le `Dockerfile`.
5. **Image d'exécution sans root** et sans outils de build (surface réduite).

### Points à vérifier (hypothèses de l'IA)
- Les images `eclipse-temurin:25-jdk` et `eclipse-temurin:25-jre` existent sous ces noms.
- La connexion JDBC vers MySQL 8.4 passe sans option supplémentaire (sinon : erreur « Public Key Retrieval is not allowed », à documenter).
- Le Maven Wrapper (`mvnw`, `.mvn/`) doit exister **avant** `docker compose up --build`.

### Erreurs rencontrées et solutions
*(à compléter par Ahmed avec ses captures)*

### Prompts de l'IA
8. « faut que le projet démarre sous docker » - Réponse : Dockerfile, service `app`, README et plan de commits mis à jour.

---

## Retours de Lucas (via son assistant IA) et ajustements

Lucas a transmis une liste de 8 demandes à Ahmed. Bilan :

| # | Demande | État |
|---|---|---|
| 1 | Maven Wrapper | Fait par Ahmed (`mvn wrapper:wrapper`, Maven 3.9.12) |
| 2 | Trois tests MySQL (migrations, contraintes, transaction) | Écrits (`MySqlIntegrationTest`), à exécuter. Le test de concurrence reste celui de Lucas (L7) |
| 3 | Clients de démo avec ids 1 et 2 | **Ajusté** : `V2` insère les clients avec `id` explicite 1 et 2 |
| 4 | `LocalDateTime` pour `acheteeLe` / `expireLe` | Déjà le cas (`CarteFidelite`, `DATETIME(6)` en SQL) |
| 5 | Verrou sur le client pour « une seule carte active » | **Ajouté** : `ClientRepository.findByIdPourCarte` (`PESSIMISTIC_WRITE`) |
| 6 | Date de concert future vérifiée avec le `Clock` | Déjà le cas (`ConcertService.programmer`) |
| 7 | Pousser vite les exceptions métier | Écrites ; à pousser dès l'étape 3 du plan de commits |
| 8 | `ControllerAdvice` sans interception d'`AccessDeniedException` | Déjà le cas : aucun handler générique, et l'advice est limité à `web.api` |

Décisions : le rattachement compte `client` -> client 1 est défini côté Lucas (`ComptesDemonstration`), la migration garantit seulement que le client 1 existe.
Prompt : 9. Transmission de la liste de Lucas - Réponse : tri fait / à faire, deux ajustements appliqués.

---

## Erreur E5 - Build refusé : Java 21 au lieu de Java 25 (A0)
- **Symptôme** : `.\mvnw.cmd clean verify` -> `BUILD FAILURE ... release version 25 not supported`. `.\mvnw.cmd -version` affiche `Java version: 21.0.12.1`.
- **Capture** : `docs/captures/A0-E5-java21.png`
- **Cause** : le poste n'avait que le JDK 21 ; le pom exige Java 25 (`java.version`). Le code n'est pas en cause.
- **Correction** : installation de Temurin 25 (`winget install EclipseAdoptium.Temurin.25.JDK`), `JAVA_HOME` pointé dessus, SDK du projet IntelliJ passé à 25.
- **Vérification** : `.\mvnw.cmd -version` affiche Java 25 (capture `A0-E5-java25.png`).
- **Retenu** : toujours contrôler la version de Java vue par Maven (`-version`) avant de chercher une erreur dans le code.

## Erreur E6 - Push refusé (A0)
- **Symptôme** : `! [rejected] main -> main (fetch first)`.
- **Cause** : le dépôt distant contenait déjà un commit de squelette généré par l'IA.
- **Correction** : (à compléter : dépôt recréé vide, ou `--force-with-lease`).
