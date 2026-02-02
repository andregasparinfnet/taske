# Estágio de Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom.xml da pasta backend para dentro do container
COPY pom.xml .
# Copia o código fonte
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# LIMITADOR DE MEMÓRIA (Essencial para o Render Free):
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]
