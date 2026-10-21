# Billetterie de concerts

Backend d'une billetterie de concerts (BUT3 Informatique, R5.08 Qualité du développement).
Java 25 · Spring Boot 4.1.1 · Maven (Wrapper) · MySQL · Flyway · Spring Security · Thymeleaf.

## Prérequis
- **JDK 25** (le projet déclare Java 25 une seule fois, dans le `pom.xml`).
- **Docker** démarré (MySQL en conteneur, et tests Testcontainers).
- Aucun Maven à installer : le Maven Wrapper est versionné (`mvnw`, `mvnw.cmd`, `.mvn/`).

Vérifier l'environnement :

```powershell
java -version
.\mvnw.cmd -version      # doit indiquer Java 25
```

## Démarrer tout le projet sous Docker (application + MySQL)
```powershell
docker compose up -d --build
docker compose logs -f app      # attendre « Started BilletterieApplication »
```
- Le `Dockerfile` construit le JAR avec le Maven Wrapper (Java 25) puis l'exécute dans une image minimale, sans utilisateur root.
- `mysql` expose un contrôle de santé ; `app` n'est lancée qu'une fois MySQL prêt, et se connecte par le nom du service (`jdbc:mysql://mysql:3306/billetterie`).
- Application : http://localhost:8080 · Santé : http://localhost:8080/actuator/health
- Arrêter : `docker compose down` (ajouter `-v` pour effacer aussi les données et reconstruire la base uniquement par les migrations).

Pour développer dans l'IDE avec seulement la base : `docker compose up -d mysql`, puis lancer l'application normalement.

Base `billetterie`, utilisateur `billetterie`. Mots de passe de développement uniquement, surchargeables par les
variables d'environnement `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `DB_URL`, `DB_USER`.
Le schéma n'est jamais créé par Hibernate : il vient uniquement des migrations Flyway (`ddl-auto=validate`),
donc la base peut être supprimée puis reconstruite par un simple redémarrage.

## Vérifier et lancer
```powershell
.\mvnw.cmd clean verify                   # compile, teste (Docker requis), construit le JAR
java -jar target\billetterie-0.0.1-SNAPSHOT.jar
```
Application : http://localhost:8080

## Pages (interface Thymeleaf)
| Page | Rôle |
|---|---|
| `/concerts` | Liste des concerts avec filtres (dates, artiste) |
| `/concerts/{id}` | Détail d'un concert et places restantes |
| `/inscription` | Inscription d'un client |

## Endpoints de l'API
| Méthode et chemin | Accès | Statuts |
|---|---|---|
| `GET /api/concerts?du=&au=&artiste=` | Public | 200 |
| `GET /api/concerts/{id}` | Public | 200, 404 (avec places restantes) |
| `POST /api/clients` | Public | 201, 400, 409 |
| `POST /api/production/artistes` | PRODUCTEUR ou ADMIN | 201, 400, 409 |
| `POST /api/production/concerts` | PRODUCTEUR ou ADMIN | 201, 400, 404, 409 |
| `POST /api/admin/salles` | ADMIN | 201, 400, 409 |
| `POST /api/clients/{id}/carte-fidelite` | CLIENT ou ADMIN | 201, 404, 409 |
| `POST /api/achats` | CLIENT | 201, 400, 404, 409 |
| `GET /actuator/health` | Public | 200 |

Erreurs : corps JSON stable `{ timestamp, status, code, message, path }`, sans trace de pile ni détail SQL
(`VALIDATION_ECHOUEE`, `CONCERT_INTROUVABLE`, `JAUGE_INSUFFISANTE`, `DOUBLON`, ...). Les 401 et 403 sont produits par Spring Security.

## Documentation et exploitation
- OpenAPI : `/v3/api-docs` · Swagger UI : `/swagger-ui.html`
- Santé : `/actuator/health` (seul endpoint Actuator exposé, sans détails)

## Données de démonstration
Migration `V2__donnees_demo.sql` : 2 artistes, 2 salles (dont « Le Petit Théâtre », 50 places), 3 concerts, 2 clients.

## Tests
| Test | Docker |
|---|---|
| `ConcertServiceTest` (service, horloge figée) | Non |
| `ConcertRepositoryJpaTest` (recherche filtrée, somme des billets vendus) | Oui |
| `MySqlIntegrationTest` (migrations, contraintes, transaction) | Oui |

Les dates sont pilotées par un `Clock` injecté (`ClockConfig`), jamais par `sleep`.

## Comptes de démonstration, sécurité, fidélité et concurrence
*(à compléter par Lucas : comptes HTTP Basic et mots de passe de démonstration, règle de fidélité à 10 %,
décision de concurrence avec verrou pessimiste et ordre des opérations.)*
