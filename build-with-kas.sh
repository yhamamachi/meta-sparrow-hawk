#!/bin/bash

SCRIPT_DIR=$(cd `dirname $0` && pwd)
TARGETS=$(ls ${SCRIPT_DIR}/conf/kas/ | grep -v common.yaml)

Usage () {
    echo "Usage: $0 <target>"
    echo ""
    echo "target:"
    for item in ${TARGETS[@]}; do
    echo "  - ${item%.*}"
    done
}

if [[ $# -ne 1 ]]; then
    Usage;
    exit
fi

# Check Param.
TARGET=$1
#if ! `IFS=$'\n'; echo "${TARGETS[@]}" | grep -qx "${TARGET}"`; then
#    Usage
#    exit
#fi

mkdir -p kas-work
KAS_WORK_DIR=kas-work kas build conf/kas/$TARGET.yaml

