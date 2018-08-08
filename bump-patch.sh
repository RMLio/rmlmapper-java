#!/usr/bin/env bash
mvn validate
mvn compile
mvn test
mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} versions:commit

echo "Don't forget to change the CHANGELOG"
