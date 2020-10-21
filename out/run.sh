#!/bin/sh

HOME=`dirname $0`

echo "HOME = $HOME"

java -Dapp.home.dir=$HOME -Dapp.log.dir=$HOME/log/system.log -Dfile.encoding=UTF-8 -jar $HOME/mypass-launcher-1.0-SNAPSHOT.jar