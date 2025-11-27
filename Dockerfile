# Estágio de Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copia o pom.xml e o código fonte
# Importante: Mantive a estrutura que você usa (pasta backend/)
COPY backend/pom.xml .
COPY backend/src ./src

# Instala Maven e compila o projeto
# O comando 'package' cria um JAR "gordo" com todas as dependências e arquivos dentro
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Estágio Final (Execução)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Pega o .jar gerado no estágio anterior. 
# O asterisco *.jar pega o nome do arquivo independente da versão.
COPY --from=builder /build/target/*.jar app.jar

# Expõe a porta (informativo, o Railway gerencia isso, mas é boa prática)
EXPOSE 8080

# Executa o JAR diretamente.
# Isso garante que o application.properties que está dentro da pasta /resources seja lido.
ENTRYPOINT ["java", "-jar", "app.jar"]