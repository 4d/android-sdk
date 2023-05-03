#!/bin/bash

has=$(which java)
if [ "$?" -ne "0" ]; then
  >&2 echo "❌ no java, install it"
  exit 1
fi

if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

java -version
echo "ℹ️ java 11 required"
# TODO: exit if not good version?

#export ARTIFACTORY_SCHEME="http"
if [ -z "$ARTIFACTORY_MACHINE_IP" ]; then
  >&2 echo "❌ You must defined ARTIFACTORY_MACHINE_IP"
  # exit 2
  export ARTIFACTORY_MACHINE_IP="localhost"
fi
#export ARTIFACTORY_MACHINE_IP="8081"
#export ARTIFACTORY_PATH="artifactory/libs-release-local"

if [ -z "$ARTIFACTORY_USERNAME"] || [ -z "$ARTIFACTORY_PASSWORD" ]; then
  >&2 echo "⚠️ You must defined ARTIFACTORY_USERNAME && ARTIFACTORY_PASSWORD. Default will be used"
  export ARTIFACTORY_USERNAME="admin"
  export ARTIFACTORY_PASSWORD="password"
fi

./gradlew clean --refresh-dependencies mavenDependencyExport
