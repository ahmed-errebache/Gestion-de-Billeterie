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
| `CalculateurPrixTest`, `AchatServiceTest`, `CarteFideliteServiceTest` (prix, remise, jauge, ordre du verrou) | Non |
| `SecurityConfigTest`, `AchatControllerTest`, `CarteFideliteControllerTest` (MVC et sécurité : 201, 400, 401, 403) | Non |
| `VenteConcurrenteIntegrationTest` (deux achats simultanés, fidélité sur MySQL) | Oui |

Les dates sont pilotées par un `Clock` injecté (`ClockConfig`), jamais par `sleep`.

## Comptes de démonstration
Authentification **HTTP Basic**. Ces comptes de démonstration sont séparés de l'inscription des clients :
`POST /api/clients` crée un client métier, pas un compte de connexion.

| Identifiant | Mot de passe | Rôle | Client métier rattaché |
|---|---|---|---|
| `client` | `client123` | CLIENT | client 1 (Camille Martin) |
| `producteur` | `producteur123` | PRODUCTEUR | aucun |
| `admin` | `admin123` | ADMIN | aucun |

Seuls les hachages **BCrypt** figurent dans le code (`ComptesDemonstration`) ; les mots de passe en clair ne sont écrits qu'ici.

```powershell
curl.exe -u client:client123 -H "Content-Type: application/json" -d '{\"concertId\":2,\"clientId\":1,\"quantite\":2}' http://localhost:8080/api/achats
curl.exe -u client:client123 -X POST http://localhost:8080/api/clients/1/carte-fidelite
```

## Sécurité
- API sans état : HTTP Basic, aucune session. Le CSRF est désactivé, il ne protège que les sessions par cookie.
- Routes publiques : lectures des concerts, inscription d'un client, OpenAPI, Swagger UI, `/actuator/health`.
  Les autres endpoints Actuator ne sont pas exposés.
- `/api/production/**` : PRODUCTEUR ou ADMIN. `/api/admin/**` : ADMIN. `POST /api/achats` : CLIENT.
- **Identité de sécurité et client métier sont reliés explicitement, jamais confondus** : le compte
  (`CompteBilletterie`) porte un `clientId`, l'entité `Client` ne connaît pas les comptes.
  `ControleAccesClient` applique la règle « un CLIENT n'agit que pour son propre client, un ADMIN pour tous » :
  - carte de fidélité : vérifiée sur le `{id}` du chemin dans `SecurityConfig` ;
  - achat : vérifiée sur le `clientId` du corps par `@PreAuthorize`.

  Un client qui agit pour un autre client reçoit 403.

## Règle de fidélité
- `POST /api/clients/{id}/carte-fidelite` vend une carte valable **exactement un an** : `expireLe = acheteeLe + 1 an`
  (une carte achetée le 29 février expire le 28 février suivant).
- **Une seule carte active par client** : une deuxième demande reçoit 409 `CARTE_DEJA_ACTIVE`.
- Une carte est active si `acheteeLe <= maintenant < expireLe`. Une carte active à la date de l'achat applique
  **10 %** de remise ; une carte expirée n'en applique aucune.
- Le serveur calcule seul le montant : `BigDecimal`, arrondi à deux décimales (`HALF_UP`). Un prix envoyé par le
  client est ignoré. Exemple : billet à 50,00 €, carte active → **45,00 €** ; carte expirée → **50,00 €**.
- « Maintenant » vient du `Clock` injecté : les tests figent l'horloge au lieu d'attendre.

## Décision de concurrence : verrou pessimiste
La vente (`AchatService.acheter`) est **une seule transaction**, dans cet ordre :

1. **verrou sur le concert** : `SELECT … FOR UPDATE` (`ConcertRepository.findByIdPourVente`) ;
2. contrôle du client (il doit être inscrit) ;
3. somme des billets déjà vendus, lue sous verrou ;
4. contrôle de la jauge : demande supérieure aux places restantes → 409 `JAUGE_INSUFFISANTE` ;
5. calcul de la remise ;
6. enregistrement de l'achat avec un code unique.

**Pourquoi un verrou pessimiste.** Deux ventes du même concert, sur deux threads ou deux instances de l'application,
sont sérialisées par MySQL : la seconde attend la fin de la première, puis lit la somme à jour. Une synchronisation
en mémoire ne protégerait pas deux instances. Un verrou optimiste aurait obligé à rejouer la vente en cas de conflit,
alors que les conflits sont attendus quand un concert se remplit.

**Pourquoi le verrou en premier.** En REPEATABLE READ (niveau par défaut de MySQL), une lecture ordinaire faite avant
le verrou fige un instantané : la somme lue ensuite pourrait être périmée et autoriser une survente.

**Aucun achat partiel** : toute exception annule la transaction. Le verrou ne bloque que les ventes du même concert.
La carte de fidélité suit le même principe avec un verrou sur le client (`ClientRepository.findByIdPourCarte`),
pour garantir une seule carte active même avec deux demandes simultanées.

**Preuve par le test** : `VenteConcurrenteIntegrationTest` rejoue cinq fois le scénario du sujet sur MySQL réel
(5 places restantes, deux achats simultanés de 4 billets) : un seul réussit, l'autre reçoit 409. Si l'on remplace
temporairement le verrou par une lecture simple, les cinq répétitions échouent par survente.
