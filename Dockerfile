FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copiar pom.xml
COPY backend/pom.xml .

# Baixar dependências
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Copiar código fonte
COPY backend/src ./src

# Compilar
RUN mvn clean package -DskipTests

# Extrair layers
RUN java -Djarmode=layertools -jar target/backend-0.0.1-SNAPSHOT.jar extract

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S -G app app
USER app

WORKDIR /app

COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]