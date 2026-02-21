# ====== STAGE 1: build ======
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia apenas os arquivos de dependência primeiro (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do projeto
COPY src ./src

# Gera o JAR
RUN mvn clean package -DskipTests

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia só o jar da etapa anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
