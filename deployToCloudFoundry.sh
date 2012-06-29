#! /bin/bash

#############################################
# Deploy the app to cloudfoundry
# and set any environment variables required
#############################################

APP_MODE='debug'
APP_PATH="`pwd`/lift/target/webapp/"
APP_NAME="cat9-test"
SBT_BUILD='yes'
VMC_LOGIN=''
CLEAN_PAKCAGE='yes'

while [ $# -gt 0 ] 
do
  case $1 in
    --app-mode)
      APP_MODE=$2
      shift
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
    --no-build)
      SBT_BUILD='no'
      shift
      ;;
    --vmc-login)
      VMC_LOGIN=$2
      shift
      shift
      ;;
    --no-clean)
      CLEAN_PACKAHE='no'
      shift
      ;;
    *)
      echo "Unknown option $1"
      shift
      ;;
  esac
done

echo "Deploying ${APP_NAME} in ${APP_MODE} mode from path ${APP_PATH}"
read -p "Is this ok? (y/n) " CONT

if [ ! "${CONT}" == "y" ] ; then
  echo "User aborted deployment. Exiting..."
  exit 2
fi

if [ "${SBT_BUILD}" == "yes" ] ; then
  if [ "${CLEAN_PACKAGE}" == "yes" ] ; then
    echo "Performing a clean build and package of the app with SBT"
    SBT_CMD='clean-package'
  else
    echo "Performing an incremental build and package of the app with SBT"
    SBT_CMD='package'
  fi
  # run the package with sbt
  ./sbt ${SBT_CMD}
else
  echo "No SBT Build performed at user request"
fi

BUILD_OK=$?

if [ ! $BUILD_OK -eq 0 ] ; then
  echo "SBT Build Failed. Exiting..."
  exit $BUILD_OK
fi

# Do any post build setup for production mode
if [ "${APP_MODE}" == "production" ] ; then
  echo "Performing post build updates for production mode"
  # Move default properties to default production properties
  PROPS_PATH="${APP_PATH}/WEB-INF/classes/props"
  mv "${PROPS_PATH}/default.props" "${PROPS_PATH}/production.default.props"
fi

echo "Deploying Application via VMC"
VMC="/var/lib/gems/1.8/bin/vmc"

# login via VMC
$VMC login ${VMC_LOGIN}

# update the app
$VMC update ${APP_NAME} --path ${APP_PATH}

UPDATE_STATUS=$?

# Set other features based on mode
echo "Setting VMC environment for ${APP_MODE} mode"
if [ "${APP_MODE}" == "production" ] ; then
  PROD_MODE=`${VMC} env ${APP_NAME} | grep 'run.mode=production'`
  if [ -z "${PROD_MODE}" ] ; then
    echo "Setting run mode to production"
    $VMC env-add ${APP_NAME} "JAVA_OPTS=-Drun.mode=production"
  else
    echo "Already set to use production mode."
  fi
else
  $VMC env-del ${APP_NAME} "JAVA_OPTS"
fi

ENV_STATUS=$?

# display env
$VMC 'env' ${APP_NAME}

RETVAL=$(($UPDATE_STATUS + $ENV_STATUS))

if [ $RETVAL -eq 0 ] ; then
  echo "Deployment of app ${APP_NAME} Successful."
else
  echo "Deployment of app ${APP_NAME} Failed."
  echo "Update/Deployment status (${UPDATE_STATUS})"
  echo "Environment update status (${ENV_STATUS})"
fi

# Logout of VMC
echo "Logging out of VMC"
$VMC logout

exit $RETVAL

