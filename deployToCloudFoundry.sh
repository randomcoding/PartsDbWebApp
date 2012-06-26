#! /bin/bash

#############################################
# Deploy the app to cloudfoundry
# and set any environment variables required
#############################################

APP_MODE='debug'

APP_PATH="`pwd`/lift/target/webapp/"

APP_NAME="am2app"

while $? -gt 0
do
  case $1 in
    --production)
      APP_MODE='production'
      shift
      ;;
    --app-name)
      APP_NAME=$2
      shift
      shift
      ;;
    --app-path)
      APP_PATH=$2
      shift
      shift
      ;;
    *)
      echo "Unknown option $1"
      shift
      ;;
  esac
done

# login

vmc login


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

