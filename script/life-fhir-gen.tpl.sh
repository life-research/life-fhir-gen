#!/usr/bin/env bash

BASEDIR=$(dirname "$0")

java -jar "${BASEDIR}/life-fhir-gen-<VERSION>-standalone.jar" $@
