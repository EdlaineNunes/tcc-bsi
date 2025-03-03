# Use a imagem base do OpenJDK 17
FROM openjdk:17-jdk-slim

# Defina o diretório de trabalho no container
WORKDIR /app

# Copie o arquivo JAR para o diretório /app do container
COPY build/libs/edlaine-0.0.1-SNAPSHOT.jar /build/libs/edlaine-0.0.1-SNAPSHOT.jar

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/build/libs/edlaine-0.0.1-SNAPSHOT.jar"]
