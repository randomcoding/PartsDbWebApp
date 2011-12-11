#! /bin/bash

SBT_VER="0.11.1"
JAVA_OPTS="-Dfile.encoding=UTF8 -Xmx512M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m"

java ${JAVA_OPTS} -jar `dirname $0`/sbt-${SBT_VER}-launch.jar "$@"

