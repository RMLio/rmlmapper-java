#!/bin/sh

apk add java-jre-headless maven java-jdk 
mvn install -DskipTests=true
