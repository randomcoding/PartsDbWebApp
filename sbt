#! /bin/bash

SBT_VER="0.11.3-2"
JAVA_OPTS="-Dfile.encoding=UTF8 -Xmx256M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m"

SBT_ARGS=""
#if [ "$1" == "--no-format" ] ; then
#  JAVA_OPTS="-Dsbt.log.noformat=true ${JAVA_OPTS}"
#  shift
#fi

#if [ "$1" == "--debug" ] ; then 
#  echo "Running in debug mode"
#  JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
#  shift
#fi

for ARG in "$@"
do
  case "$ARG" in
    --debug)
      echo "Running in debug mode"
      JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
      ;;
    --no-format)
      echo "Disabling SBT Log Formatting"
      JAVA_OPTS="-Dsbt.log.noformat=true ${JAVA_OPTS}"
      ;;
    *)
      SBT_ARGS="${SBT_ARGS} ${ARG}"
      ;;
  esac
done

#while [ $# -gt 0 ]
#do
#  case $1 in
#    --debug)
#      echo "Running in debug mode"
#      JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
#      shift
#      ;;
#    --no-format)
#      echo "Disabling SBT Log Formatting"
#      JAVA_OPTS="-Dsbt.log.noformat=true ${JAVA_OPTS}"
#      shift
#      ;;
#    *)
#      SBT_ARGS="${SBT_ARGS} ${1}"
#      shift
#      ;;
#  esac
#done

#java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar "$@"
if [ -z "${SBT_ARGS}" ] ; then
  java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar
else
  java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar "$SBT_ARGS"
fi

