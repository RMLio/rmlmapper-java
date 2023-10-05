FROM eclipse-temurin:19-jdk

ADD . /rmlmapper-java

RUN apt update && apt install maven git -y
WORKDIR rmlmapper-java
RUN git status
RUN mvn clean install -DskipTests=true
RUN mv `find target/ -iname rmlmapper-*-all.jar;` /rmlmapper.jar
WORKDIR /data

ENTRYPOINT ["java", "-jar", "/rmlmapper.jar"]
