#!/usr/bin/env bash
mvn validate
mvn compile
mvn test

# patch
# mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} versions:commit
# minor
# mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.\${parsedVersion.incrementalVersion} versions:commit
# major
# mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion} versions:commit

echo "Don't forget to change the CHANGELOG"
