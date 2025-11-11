# --- Fase 1: Construcción (El "Taller") ---
# Usa una imagen de Maven con Java 17 para construir el .jar
FROM maven:3.9-eclipse-temurin-17 AS builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia solo el pom.xml primero (para la caché de Docker)
COPY pom.xml .
# Copia el resto del código fuente
COPY src ./src

# Corre el comando de Maven para construir el .jar
RUN mvn clean package -DskipTests

# --- Fase 2: Producción (El "Contenedor Final") ---
# Usa una imagen de JRE "limpia" y pequeña para correr la app
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia el .jar que construimos en la Fase 1
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto 8080 (el que usa Spring)
EXPOSE 8080

# El comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
