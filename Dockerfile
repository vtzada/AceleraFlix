# ---- Etapa de build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Etapa de execução ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 acelera
USER acelera

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Usa a PORT injetada pelo Render; cai em 8080 fora dele
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
