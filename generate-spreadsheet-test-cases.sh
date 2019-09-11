#!/usr/bin/env bash

# REQUIRES libreoffice!!

TEST_LOCATION="src/test"
TEST_FILE_LOCATION="java/be/ugent/rml"
TEST_RESOURCES_LOCATION="resources/test-cases"
NAME_CSV_TEST="Mapper_CSV_Test.java"

# Check for libreoffice
if [[ ! `libreoffice --help` ]]
then
  echo "Install libreoffice to convert CSV."
  return 1
fi

cd ${TEST_LOCATION}
TEST_DIR=$(pwd)

for i in "EXCEL xlsx" "ODS ods"
do
    set -- ${i}
    echo "Generating ${1} tests from CSV tests"

    ## Test files
    cd "${TEST_DIR}/${TEST_FILE_LOCATION}"
    NAME_NEW_TEST="Mapper_${1}_Test.java"
    cp ${NAME_CSV_TEST} ${NAME_NEW_TEST}
    sed -i "s/CSV/${1}/g" ${NAME_NEW_TEST}

    ## Test resources
    cd "${TEST_DIR}/${TEST_RESOURCES_LOCATION}"
    for csv_dir in *CSV*
    do
        # Copy CSV test directory
        NEW_DIR_NAME=$(echo ${csv_dir} | sed "s/CSV/${1}/")
        if [[ -d ${NEW_DIR_NAME} ]]
        then
            rm -Rf ${NEW_DIR_NAME}
        fi
        cp -r ${csv_dir} ${NEW_DIR_NAME}
        cd ${NEW_DIR_NAME}

        # Change files within directory

        echo "Test case: ${NEW_DIR_NAME}"
        # csv source file
        for csv_source in *.csv
        do
            if [[ ! -f ${csv_source} ]]; then break; fi
            # UTF-8 encoding issue
            # https://bugs.documentfoundation.org/show_bug.cgi?id=36313
            libreoffice --headless --convert-to ${2} --infilter=CSV:44,34,UTF8 ${csv_source}
            rm ${csv_source}
        done
        # mapping file
        sed -i "s/.csv/.${2}/g" "mapping.ttl"

        cd ..
    done
done

echo "Success!"

