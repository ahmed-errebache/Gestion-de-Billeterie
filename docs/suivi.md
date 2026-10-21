# Suivi du TP Billetterie - tâches d'Ahmed

Début du TP : ____ h ____   |   Fin prévue (début + 4 h) : ____ h ____
Mise à jour : dis-moi « A3 fini » ou colle l'heure, je recalcule le temps restant.

Légende : [x] fait · [~] en cours · [ ] à faire

## Consigne ajoutée par le prof (en présentiel)
- [~] Consigne Docker : **le projet doit démarrer sous Docker**. Écrit (`Dockerfile`, service `app` dans `compose.yaml`), non testé. Preuve : `docker compose up -d --build` puis `/actuator/health` (capture `A12-03-docker-up.png`)

## Où on en est (mise à jour)
Code A1 à A11 **écrit par l'IA, non compilé ni exécuté** (le poste de travail de l'IA n'a ni Maven, ni Docker, ni accès à Maven Central).
`[~]` = écrit, à relire, compiler et tester par Ahmed. Passe à `[x]` quand `clean verify` est vert et que tu as pris la capture.
A12 reste à faire par toi (clone propre, `clean verify`, `java -jar`).

## Étapes
| État | # | Étape | Preuve à garder (capture / commit) |
|---|---|---|---|
| [~] | A0 | Squelette écrit, à relire et valider (build non vérifié) | Capture `mvnw -version` (Java 25), capture `clean verify` |
| [ ] | A0b | `git init`, premier commit, push, ajouter Lucas | Capture du dépôt sur GitHub |
| [~] | A1 | Migration V1 (schéma) | Capture Flyway qui applique V1 |
| [~] | A2 | Migration V2 (données de démo) | Capture des données en base |
| [~] | A3 | Entités JPA + repositories (à pousser tôt : Lucas en dépend) | Commit |
| [~] | A4 | Exceptions métier (à pousser tôt) | Commit |
| [~] | A5 | GET concerts (liste filtrée) et détail | Capture réponse JSON |
| [~] | A6 | Endpoints de création + DTO + validation | Captures 201, 400, 409 |
| [~] | A7 | ControllerAdvice, JSON d'erreur stable | Capture d'une erreur 400 stable |
| [~] | A8 | Actuator health, OpenAPI, Swagger UI | Capture Swagger UI |
| [~] | A9 | Tests JPA + 3 Testcontainers | Capture du `clean verify` vert |
| [~] | A10 | Pages Thymeleaf publiques | Captures des pages |
| [~] | A11 | README (tes sections) | - |
| [ ] | A12 | Vérification finale : clone propre, `clean verify`, `java -jar` | Captures finales |

## Compte rendu
- [ ] Journal à jour (erreurs + captures + corrections de l'IA)
- [ ] Prompts de l'IA consignés
- [ ] Export PDF du compte rendu
