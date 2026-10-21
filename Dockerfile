# syntax=docker/dockerfile:1

# ---------- Étape 1 : construction du JAR avec le Maven Wrapper (Java 25) ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Le wrapper et le pom d'abord : la couche des dépendances est réutilisée tant que le pom ne change pas
COPY mvnw pom.xml ./
COPY .mvn .mvn
# Neutralise d'éventuelles fins de ligne Windows (CRLF) qui casseraient le script sous Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B -q dependency:go-offline

COPY src src
# Les tests Testcontainers ont besoin de Docker : ils s'exécutent avec « mvnw clean verify » sur le poste, pas dans l'image
RUN ./mvnw -B -q -DskipTests package

# ---------- Étape 2 : image d'exécution minimale, utilisateur non root ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system appli && useradd --system --gid appli appli
COPY --from=build /app/target/*.jar app.jar
USER appli
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
