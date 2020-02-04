#!/bin/sh

HOME=`dirname $0`
LIB_HOME="$HOME/extLib"

echo "HOME = $HOME"

java -Dapp.home.dir=$HOME -Dapp.log.dir=$HOME/log/app.log -Dfile.encoding=UTF-8 -jar mypass-1.0-SNAPSHOT.jar