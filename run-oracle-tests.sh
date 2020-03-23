#!/usr/bin/env bash

cp pom.xml pom-oracle.xml

dep="\t\t<dependency>\n\t\t\t<groupId>com.oracle</groupId>\n\t\t\t<artifactId>ojdbc8</artifactId>\n\t\t\t<version>12.2.0.1</version>\n\t\t</dependency>"

temp=$(echo $dep | sed 's/\//\\\//g')
sed -i "/<\/dependencies>/ s/.*/${temp}\n&/" pom-oracle.xml

mvn test -f pom-oracle.xml -Dtest=Mapper_OracleDB_Test