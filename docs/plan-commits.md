# Plan de commits - un commit (et une branche) par étape

Principe : on ne pousse pas tout d'un coup. Pour chaque étape : **tu lances la vérification, tu prends la capture,
tu remplis la section du journal, puis tu commites**. C'est ce passage qui rend le commit réellement validé par toi.
Une branche et une PR par étape, `main` reçoit chaque PR mergée.

Rappel d'honnêteté : le code a été écrit par l'IA. Le journal le dit ; pour que l'historique Git soit cohérent avec lui,
tu peux ajouter à chaque message une ligne `Co-Authored-By: Claude <noreply@anthropic.com>` (recommandé).

Avant la première étape, dans le dossier du projet :

```powershell
git init -b main
git remote add origin https://github.com/ahmed-errebache/Gestion-de-Billeterie.git
mvn wrapper:wrapper -Dmaven=3.9.11
```
(Si le dépôt distant contient déjà un commit, supprime-le et recrée-le vide : ça évite un conflit d'historique.)

Modèle de séquence pour chaque étape :

```powershell
git switch main; git pull            # (inutile à la 1re étape)
git switch -c <branche>
# vérifier (voir colonne "Vérifier"), capturer dans docs/captures/, compléter docs/journal.md
git add <fichiers de l'étape> docs/
git commit -m "<message>"
git push -u origin <branche>
# ouvrir la PR sur GitHub, la faire relire par Lucas, merger
```

| # | Branche | Fichiers de l'étape | Vérifier avant de commiter | Capture à prendre | Message de commit |
|---|---|---|---|---|---|
| 1 | `chore/squelette` | `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `compose.yaml`, `Dockerfile`, `.dockerignore`, `.gitattributes`, `.gitignore`, `CONVENTIONS.md`, `src/main/resources/application.properties`, `BilletterieApplication.java`, `config/ClockConfig.java`, `docs/` | `.\mvnw.cmd -version` (Java 25) puis `.\mvnw.cmd clean verify` | `A0-01-mvn-version.png`, `A0-02-clean-verify.png` | `chore: squelette Maven Java 25, Spring Boot 4.1.1 et MySQL` |
| 2 | `feat/migrations` | `db/migration/V1__schema.sql`, `V2__donnees_demo.sql` | `docker compose up -d` puis `.\mvnw.cmd spring-boot:run` : Flyway applique V1 et V2 | `A1-01-flyway.png`, `A2-01-donnees.png` | `feat(db): schéma Flyway V1 et données de démonstration V2` |
| 3 | `feat/domaine` | `domain/*`, `repository/*`, `exception/*` | `spring-boot:run` : Hibernate valide le schéma sans erreur | `A3-01-validate.png` | `feat(domain): entités JPA, repositories et exceptions métier` |
| 4 | `feat/api-lecture` | `web/dto/ConcertResume`, `ConcertDetail`, `ConcertRequest`, `service/ConcertSpecifications`, `service/ConcertService`, `web/api/ConcertApiController` | GET `/api/concerts` et `/api/concerts/1` (le 401 vient de la sécurité tant que Lucas n'a pas ouvert les routes) | `A5-01-liste.png`, `A5-02-detail.png` | `feat(api): lecture publique des concerts avec filtres et places restantes` |
| 5 | `feat/api-creation` | `web/dto/` (les 7 autres), `service/ClientService`, `ArtisteService`, `SalleService`, `web/api/ClientApiController`, `ProductionApiController`, `AdminApiController` | POST client, artiste, concert, salle (Swagger ou Postman) : 201 puis 409 sur doublon | `A6-01-201.png`, `A6-02-409.png` | `feat(api): création des clients, artistes, concerts et salles` |
| 6 | `feat/erreurs-exploitation` | `web/error/*`, `config/OpenApiConfig` | Provoquer un 400 (quantité ou e-mail invalide) : corps JSON stable ; ouvrir `/swagger-ui.html` et `/actuator/health` | `A7-01-erreur-400.png`, `A8-01-swagger.png`, `A8-02-health.png` | `feat(api): erreurs JSON stables, OpenAPI et Actuator` |
| 7 | `test/jpa-integration` | `src/test/**` | `.\mvnw.cmd clean verify` avec Docker démarré | `A9-01-verify-vert.png` | `test: service, JPA et intégration MySQL (migrations, contraintes, transaction)` |
| 8 | `feat/ui-publique` | `web/ui/*`, `templates/*`, `static/*` | Ouvrir `/concerts`, `/concerts/1`, `/inscription` ; une inscription doublon affiche l'erreur | `A10-01-concerts.png`, `A10-02-inscription.png` | `feat(ui): pages Thymeleaf publiques (concerts, détail, inscription)` |
| 9 | `docs/readme` | `README.md`, `docs/` | Relire le README et suivre ses commandes sur un clone propre | `A11-01-readme.png` | `docs: README et journal de bord` |

## Après les 9 étapes (A12)
1. Cloner le dépôt dans un **dossier neuf**, Docker démarré.
2. `.\mvnw.cmd clean verify`, puis `java -jar target\billetterie-0.0.1-SNAPSHOT.jar` hors IntelliJ.
3. Contrôler A1, A2, A3, A10 de la recette. Capturer.

## Si une étape échoue
Ne contourne pas : note le symptôme, la capture, la cause et la correction dans le tableau « Erreurs rencontrées » du journal, puis commite la correction **dans la même branche**, avec son propre message (`fix: ...`). Un historique où l'on voit l'erreur puis sa correction vaut mieux dans le compte rendu qu'un historique parfait.
