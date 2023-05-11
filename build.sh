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


current=`pwd`
echo $current
export CI_DEPS_TO_BE_FETCHED=true

rm -fr dependencies/*

./gradlew clean --refresh-dependencies mavenDependencyExport

mkdir tmp_qmobile_modules
cd tmp_qmobile_modules

projects="QMobileAPI QMobileDataStore QMobileDataSync QMobileUI"

for project in $projects; do
    echo "📦 Cloning $project"
    rm -fr android-$project
    git clone https://github.com/4d/android-$project.git
    lowercase_project=`echo $project | tr "[:upper:]" "[:lower:]"`
    cd android-$project
    echo `pwd`
    ./gradlew assemble
    ./gradlew generatePomFileForAarPublication
    mkdir -p $current/dependencies/com/qmobile/$lowercase_project/$lowercase_project/0.0.1-main
    cp $lowercase_project/build/outputs/aar/$lowercase_project-debug.aar $current/dependencies/com/qmobile/$lowercase_project/$lowercase_project/0.0.1-main/$lowercase_project-0.0.1-main.aar
    cp $lowercase_project/build/publications/aar/pom-default.xml $current/dependencies/com/qmobile/$lowercase_project/$lowercase_project/0.0.1-main/$lowercase_project-0.0.1-main.pom
    cd ..
done

cd ..
rm -fr tmp_qmobile_modules