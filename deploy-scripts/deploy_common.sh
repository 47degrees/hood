#!/usr/bin/env bash
set -e

function getProperty {
    PROP_KEY=$1
    PROP_VALUE=`cat gradle.properties | grep -e "^$PROP_KEY=" | cut -d'=' -f2`
    echo $PROP_VALUE
}

function fail {
    echo "$1"
    exit -1
}

SLUG="47degrees/hood"
BRANCH="master"
RELEASE_VERSION=$(getProperty "release_version")
LATEST_PUBLISHED_VERSION=$(curl https://dl.bintray.com/47deg/hood/com/47deg/hood/maven-metadata.xml | grep latest | cut -d'>' -f2 | cut -d'<' -f1)
if [ "$RELEASE_VERSION" != "$LATEST_PUBLISHED_VERSION" ]; then
    sed -i "s/version.*/version=$RELEASE_VERSION/g" gradle.properties
fi
VERSION_NAME=$(getProperty "version")
