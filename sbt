#! /bin/bash

SBT_VER="0.11.2"
JAVA_OPTS="-Dfile.encoding=UTF8 -Xmx512M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m"

SBT_ARGS=""

while [ $# -gt 0 ]
do
  case $1 in
    --debug)
      echo "Running in debug mode"
      JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
      shift
      ;;
    *)
      SBT_ARGS="${SBT_ARGS} ${$1}"
      shift
      ;;
  esac
done

java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar 
#"$SBT_ARGS"

