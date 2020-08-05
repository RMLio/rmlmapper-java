#!/usr/bin/env bash

## Prints message when verbose is enabled.
log () {
  local MESSAGE=$1

  if [[ "$VERBOSE" == "true" ]]; then
    >&2 echo $MESSAGE
  fi
}

SKIP_ORACLE_TESTS=false
ONLY_ORACLE_TESTS=false

# Process command line arguments
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -n|--no-oracle)
    SKIP_ORACLE_TESTS=true
    shift # past argument
    ;;
    -o|--only-oracle)
    ONLY_ORACLE_TESTS=true
    shift # past argument
    ;;
    -v|--verbose)
    VERBOSE=true
    shift # past argument
    ;;
    -m|--hide-maven-output)
    HIDE_MAVEN_OUTPUT=true
    shift # past argument
    ;;
    -h|--help)
    echo "Usage information: "
    echo
    echo "  -n|--no-oracle         Skip Oracle tests."
    echo "  -o|--only-oracle       Only run Oracle tests."
    echo "  -v|--verbose           Output more information."
    echo "  -m|--hide-maven-output Hide maven output."
    echo "  -h|--help              The usage of information."
    exit 0;
    ;;
esac
done

if [[ "$SKIP_ORACLE_TESTS" == "true" && "$ONLY_ORACLE_TESTS" == "true" ]]; then
    >&2 echo "Error: Either use -n|--no-oracle or -o|--only-oracle, but not both."
    exit 1
fi

log "Skip Oracle tests: $SKIP_ORACLE_TESTS"
log "Only Oracle tests: $ONLY_ORACLE_TESTS"
log ""

if [[ "$ONLY_ORACLE_TESTS" != "true" ]]; then
  log "Running all tests except Oracle tests."

  if [[ "$HIDE_MAVEN_OUTPUT" == "true" ]]; then
    mvn test -Dtest=!Mapper_OracleDB_Test &> /dev/null
  else
    mvn test -Dtest=!Mapper_OracleDB_Test
  fi
fi

if [ $? -eq 0 ]; then
    if [[ "$SKIP_ORACLE_TESTS" != "true" ]]; then
      log "Running all Oracle tests."
      cp pom.xml pom-oracle.xml

      dep="\t\t<dependency>\n\t\t\t<groupId>com.oracle</groupId>\n\t\t\t<artifactId>ojdbc8</artifactId>\n\t\t\t<version>12.2.0.1</version>\n\t\t</dependency>"

      temp=$(echo $dep | sed 's/\//\\\//g')
      sed -i "/<\/dependencies>/ s/.*/${temp}\n&/" pom-oracle.xml

      if [[ "$HIDE_MAVEN_OUTPUT" == "true" ]]; then
        mvn test -f pom-oracle.xml -Dtest=Mapper_OracleDB_Test &> /dev/null
      else
        mvn test -f pom-oracle.xml -Dtest=Mapper_OracleDB_Test
      fi

      rm pom-oracle.xml

      if [ $? -eq 0 ]; then
        log "All tests are done."
      else
        >&2 echo "Some of the tests failed."
        exit 1
      fi
    else
      log "All tests succeeded."
    fi
else
    >&2 echo "Some of the tests failed."
    exit 1
fi
