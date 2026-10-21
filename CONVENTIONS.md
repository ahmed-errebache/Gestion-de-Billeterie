# Conventions du projet

À respecter par les deux membres du binôme, pour que le code, les migrations et l'API restent cohérents.

## Langue et noms
- Noms métier **en français**, partout (code Java, migrations SQL, JSON de l'API).
- Entités : `Artiste`, `Salle`, `Concert`, `Client`, `CarteFidelite`, `AchatBillets`. Valeur imbriquée : `Adresse` (rue, ville).
- Tables SQL en snake_case : `artiste`, `salle`, `concert`, `client`, `carte_fidelite`, `achat_billets`.

## Packages (`fr.iut.billetterie`)
| Package | Rôle |
|---|---|
| `domain` | Entités JPA et valeurs imbriquées |
| `repository` | Interfaces Spring Data |
| `service` | Règles métier et transactions (jamais dans les contrôleurs) |
| `web.api` | `@RestController` : API JSON |
| `web.ui` | `@Controller` : pages Thymeleaf, appellent les services directement |
| `web.dto` | DTO d'entrée et de sortie (records immuables) |
| `web.error` | `@RestControllerAdvice` limité aux contrôleurs REST |
| `exception` | Exceptions métier spécifiques |
| `security` | Configuration Spring Security, comptes de démonstration |
| `config` | Beans de configuration (`Clock`, ...) |

## Codes d'erreur (champ `code` du JSON)
`VALIDATION_ECHOUEE` (400) · `CONCERT_INTROUVABLE` (404) · `CLIENT_INTROUVABLE` (404) · `JAUGE_INSUFFISANTE` (409) · `DOUBLON` (409) · `CARTE_DEJA_ACTIVE` (409).
Les erreurs 401 et 403 sont produites par Spring Security.

## Rôles
`CLIENT`, `PRODUCTEUR`, `ADMIN`.

## Règles techniques
- Java 25 déclaré **une seule fois** (`java.version` dans le pom).
- Hibernate ne crée jamais le schéma : `ddl-auto=validate`, tout vient de Flyway.
- Montants : `BigDecimal` côté Java, `DECIMAL(10,2)` côté SQL, arrondi à 2 décimales.
- Dates : toujours via le bean `Clock`, jamais `Instant.now()` direct, jamais `sleep` dans les tests.
- Les entités JPA ne sont jamais renvoyées par les contrôleurs.
- Aucun secret réel dans le dépôt ; `.env`, `target/` et fichiers d'IDE ignorés.

## Git
- Une branche et une pull request par bloc de travail, l'autre membre relit puis merge.
- `pom.xml` et migrations Flyway : un seul propriétaire (Ahmed). Les demandes se font par message.
- `git status` avant de dire « j'ai fini » : un fichier non commité ne compte pas.
