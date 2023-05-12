#!/bin/bash

name=$1
if [[ -z "$name" ]]; then
  name="android"
fi
rm "$name.zip"
(cd dependencies && zip -r ../$name.zip . )
ls -l "$name.zip"
