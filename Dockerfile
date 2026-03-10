# Estágio de Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# Copia o wrapper e o pom.xml primeiro para cachear dependências
COPY backend/.mvn .mvn
COPY backend/mvnw .
COPY backend/pom.xml .

# Baixa as dependências (cacheável se o pom.xml não mudar)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copia o código fonte e compila
COPY backend/src ./src
RUN ./mvnw clean package -DskipTests

# Estágio Final (Execução)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Pega o .jar gerado no estágio anterior
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Configurações de Memória para instâncias menores (OCI Free Tier)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
