# Journal de bord - TP Billetterie de concerts (R5.08)

Binôme : Ahmed Errebache / Lucas Charpentier. Ce journal alimente le compte rendu.
Règle : on y note ce qui s'est réellement passé, y compris **qui a produit quoi** (nous ou l'IA).
Captures dans `docs/captures/`, nommées `<étape>-<numéro>-<sujet>.png`.

## Rôle de l'IA dans ce projet
- Outil utilisé : Claude (Anthropic), en conversation.
- Principe : l'IA propose et génère, nous relisons, testons et corrigeons. Chaque entrée ci-dessous indique ce qui vient de l'IA, ce que nous avons vérifié et ce que nous avons modifié.

---

## Étape A0 - Dépôt et squelette

### Ce qui a été fait
| Élément | Produit par | Vérifié / décidé par |
|---|---|---|
| Création du dépôt GitHub vide `Gestion-de-Billeterie` | Ahmed | - |
| Analyse du sujet, répartition des tâches Ahmed / Lucas (PDF) | IA, à la demande d'Ahmed | Ahmed (durées retirées, aucune tâche commune, dépôt à sa charge) |
| `pom.xml`, `compose.yaml`, `application.properties`, `.gitignore`, arborescence des packages, `BilletterieApplication`, `ClockConfig`, `CONVENTIONS.md` | **IA** (générés, puis poussés dans le dépôt) | **À faire par Ahmed** : relire chaque fichier, lancer le build, noter ses corrections ci-dessous |

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
- **Le Maven Wrapper n'a pas pu être généré** par l'IA : son environnement n'atteint pas Maven Central (`repo.maven.apache.org: Name or service not known`). Le build n'a donc **pas** été vérifié par l'IA.
  - À faire par Ahmed sur son poste : `mvn wrapper:wrapper -Dmaven=3.9.11` (ou le faire depuis IntelliJ), puis `.\mvnw.cmd -version` et `.\mvnw.cmd clean verify`.
  - 📸 Capture à prendre : sortie de `.\mvnw.cmd -version` montrant Java 25.

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
6. « fait cloner ce repo ... et ajout l'architecture » - Réponse : clone (dépôt vide constaté), puis génération du squelette décrit ci-dessus.

### Corrections apportées à ce que l'IA a produit
*(à compléter par Ahmed : ce qu'il a changé, pourquoi)*
