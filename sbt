#! /bin/bash

SBT_VER="0.11.3-2"
JAVA_OPTS="-Dfile.encoding=UTF8 -Xmx256M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m"

SBT_ARGS=""

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
    test)
      JAVA_OPTS="${JAVA_OPTS} -Dtesting=yes"
      SBT_ARGS="${SBT_ARGS} ${ARG}"
      ;;
    --production)
      echo "Setting Production Mode"
      JAVA_OPTS="${JAVA_OPTS} -Drun.mode=production"
      ;;
    *)
      SBT_ARGS="${SBT_ARGS} ${ARG}"
      ;;
  esac
done

if [ -z "${SBT_ARGS}" ] ; then
  java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar
else
  java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-${SBT_VER}.jar "$SBT_ARGS"
fi

