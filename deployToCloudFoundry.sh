#! /bin/bash

#############################################
# Deploy the app to cloudfoundry
# and set any environment variables required
#############################################

APP_MODE='debug'

for arg in $@
do
  case $arg in
    --production)
      APP_MODE='production'
      ;;
  esac
done

# login

vmc login

APP_PATH="`pwd`/lift/target/webapp/"

APP_NAME="am2app"

vmc update ${APP_NAME} --path ${APP_PATH}

UPDATE_STATUS=$?

if [ "${APP_MODE}" == "production" ] ; then
  vmc env-add ${APP_NAME} "JAVA_OPTS=-Drun.mode=production"
else
  vmc env-del ${APP_NAME} "JAVA_OPTS"
fi

ENV_STATUS=$?

# display env
vmc ${APP_NAME} env

echo "Update (${UPDATE_STATUS}) Env (${ENV_STATUS})"

RETVAL=$(($UPDATE_STATUS + $ENV_STATUS))

exit $RETVAL

