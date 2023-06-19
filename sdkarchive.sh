#!/bin/bash

name=$1
if [[ -z "$name" ]]; then
  name="android"
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"

rm "$name.zip"
(cd dependencies && zip -r ../$name.zip . )
ls -l "$name.zip"
