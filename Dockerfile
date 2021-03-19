FROM openjdk:8-alpine

RUN apk add --no-cache git maven
ADD . /rmlmapper-java

WORKDIR rmlmapper-java
RUN mvn clean install -DskipTests=true
RUN mv `find target/ -iname rmlmapper-*-all.jar;` /rmlmapper.jar
WORKDIR /data

ENTRYPOINT ["java", "-jar", "/rmlmapper.jar"]
