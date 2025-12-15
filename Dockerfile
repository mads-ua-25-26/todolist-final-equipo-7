# Etapa de construcción
FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Añadida la opción java.security.egd para evitar que el servidor se cuelgue en Digitalocean
# al hacer una petición que usa el HttpSession.
# El problema está relacionado con el acceso al fichero /dev/random para inicializar el generador de números aleatorios
# https://programmer.help/blogs/page-opening-stuck-when-using-httpsession-in-springboot-under-openjdk.html
# Más información:
# https://www.digitalocean.com/community/tutorials/how-to-setup-additional-entropy-for-cloud-servers-using-haveged
ENTRYPOINT ["sh","-c","java -Djava.security.egd=file:/dev/urandom -jar /app/app.jar"]
