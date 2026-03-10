FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Pega o .jar pré-compilado pelo CI
COPY backend/target/*.jar app.jar

EXPOSE 8080

# Configurações de Memória para instâncias menores (OCI Free Tier)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
