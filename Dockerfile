# Build image
FROM eclipse-temurin:19-jdk as buildimage

ADD . /rmlmapper-java

RUN apt update && apt install maven -y
WORKDIR rmlmapper-java
RUN mvn -Pno-buildnumber clean install -DskipTests=true
RUN mv `find target/ -iname rmlmapper-*-all.jar;` /rmlmapper.jar

# Base image
FROM eclipse-temurin:19-jre
COPY --from=buildimage /rmlmapper.jar /rmlmapper.jar

WORKDIR /data

ENTRYPOINT ["java", "-jar", "/rmlmapper.jar"]
