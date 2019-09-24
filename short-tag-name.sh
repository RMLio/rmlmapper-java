#!/bin/bash
set -e

# Drop the 'v' char in the tag names
OUT=$(echo "$1" | cut -d 'v' -f 2)

# Return the output for Gitlab CI
echo $OUT
