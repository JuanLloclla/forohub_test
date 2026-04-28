# Etapa 1: Construcción (Build)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Dar permisos de ejecución al script mvnw
RUN chmod +x mvnw

# Construir el jar
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen de ejecución (Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar solo el jar generado de la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto que usa Spring Boot
EXPOSE 8080

# Agregamos parámetros para optimizar el uso de memoria en contenedores pequeños
ENTRYPOINT ["java", "-Xmx500m" ,"-jar", "app.jar"]