#!/usr/bin/env bash
set -e

function getProperty {
    PROP_KEY=$1
    PROP_VALUE=`cat gradle.properties | grep "$PROP_KEY" | cut -d'=' -f2`
    echo $PROP_VALUE
}

function fail {
    echo "$1"
    exit -1
}

SLUG="47degrees/hood"
JDK="oraclejdk8"
BRANCH="master"
VERSION_NAME=$(getProperty "version")