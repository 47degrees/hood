#!/usr/bin/env bash
. $(dirname $0)/deploy_functions.sh
set -e

SLUG="47degrees/hood"
BRANCH="master"
VERSION_NAME=$(getProperty "version")