#!/bin/bash

clean=1
if [ "$#" -gt "0" ]; then
    clean=$1
fi

config="release" # alternative "debug"
qmobile_version="0.0.1"

echo ""
branch="main"
current_branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)
if [[ "$current_branch" == *\/* ]] || [[ "$current_branch" == *\\* ]]; then
  echo "Default branch $branch. Feature $current_branch" # currently if feature or fix branch keep main, maybe allow to select a branch etc...
elif [ -z "$current_branch" ]; then
  echo "Default branch $branch"
else
  branch="$current_branch"
  echo "Current branch $branch"
fi

echo ""
echo " ☕ Check Java"

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

echo ""
echo "🤖 Check Android SDK"
if [ -z "$ANDROID_HOME" ];then
  # for mac only
  if [ -d "$HOME/Library/Android/sdk" ]; then
    export ANDROID_HOME=$HOME/Library/Android/sdk
  elif [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_HOME=$HOME/Android/Sdk
  # else Windows: %LOCALAPPDATA%\Android\sdk
  else
    >&2 echo "❌ no ANDROID_HOME defined"
    exit 2
  fi

  export ANDROID_SDK_ROOT=$ANDROID_HOME
  export ANDROID_PREFS_ROOT=$HOME
  export ANDROID_SDK_HOME=$ANDROID_PREFS_ROOT
  export ANDROID_USER_HOME=$ANDROID_PREFS_ROOT/.android
  export PATH=$PATH:$ANDROID_HOME/platform-tools/
  export PATH=$PATH:$ANDROID_HOME/tools/
  export PATH=$PATH:$ANDROID_HOME/tools/bin/
  export PATH=$PATH:$ANDROID_AVD_HOME
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR" || exit 3

DEP_DIR=$SCRIPT_DIR/dependencies
if [ -z "$ARTIFACTORY_MACHINE_IP" ]; then
  export ARTIFACTORY_MACHINE_IP="localhost" # for less warnings on host
fi

if [ "$clean" -eq "1" ]; then
  echo ""
  echo "🧹 Clean"

  rm -fr dependencies/*
fi

echo ""
echo "🔎 Thirdparty Dependencies"

./gradlew clean --refresh-dependencies mavenDependencyExport

echo ""
echo "📱 QMobile Dependencies"

mkdir -p ".checkout"

projects="QMobileAPI QMobileDataStore QMobileDataSync QMobileUI"

# url="https://github.com/4d/android-"
url="git@github.com:4d/android-"

version_file="$SCRIPT_DIR/sdk/versions.txt"
echo -n "" > "$version_file" # create empty version file

export DEPS_PATH="../android-" # to build relatively to others
export CI_DEPS_TO_BE_FETCHED=true # do not use local repositories or pom file is invalid

for project in $projects; do

    echo "  📦 Cloning $project"
    project_lower=$(echo "$project" | tr "[:upper:]" "[:lower:]")

    cd "$SCRIPT_DIR" || exit 3
    if [ -d ".checkout/android-$project/$project_lower" ]; then
      cd ".checkout/android-$project" || exit 3
      # if issue add git reset --hard?
      qmobile_branch=$(git rev-parse --abbrev-ref HEAD)
      if [ "$branch" != "$qmobile_branch" ]; then
        git fetch
        git switch "$branch"
      fi
      git pull origin
    else
      cd ".checkout" || exit 3
      git clone -b "$branch" "$url$project.git"
    fi
    # TODO maybe checkout correct defined branch

    echo "   ⚙️ Build $project"
    cd "$SCRIPT_DIR/.checkout/android-$project" || exit 3
    hash=$(git rev-parse --short HEAD)
  
    if [ "$clean" -eq "1" ]; then
      echo "   🧹Clean $project"
      ./gradlew clean --console=rich
    fi
    ./gradlew assemble --console=rich --stacktrace
    ./gradlew generatePomFileForAarPublication

    echo "   ➡️ Copy $project"
    mkdir -p "$DEP_DIR/com/qmobile/$project_lower/$project_lower/$qmobile_version-$branch"

    if [ -f "$project_lower/build/outputs/aar/$project_lower-$config.aar" ]; then
      cp "$project_lower/build/outputs/aar/$project_lower-$config.aar" "$DEP_DIR/com/qmobile/$project_lower/$project_lower/$qmobile_version-$branch/$project_lower-$qmobile_version-$branch.aar"
      cp "$project_lower/build/publications/aar/pom-default.xml" "$DEP_DIR/com/qmobile/$project_lower/$project_lower/$qmobile_version-$branch/$project_lower-$qmobile_version-$branch.pom"

      if [ -s "${version_file}" ]; then
        echo -n "." >> "$version_file"
      fi
      echo -n "$hash" >> "$version_file"

    else
      echo "❌ Failed to generate aar $project"
      exit 2
    fi

done

echo "🎉 Ouput generated in $DEP_DIR"
cat "$version_file"
