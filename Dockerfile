# Usa o runtime Java 21 necessário para executar o JAR.
FROM eclipse-temurin:21-jre

# Define o diretório de trabalho dentro da imagem.
WORKDIR /app

# Copia o JAR gerado pelo Maven para a imagem.
COPY target/stockplay-api-0.0.1-SNAPSHOT.jar app.jar

# Documenta a porta em que a API escuta.
EXPOSE 8080

# Inicia a API quando o container é executado.
ENTRYPOINT ["java", "-jar", "app.jar"]
