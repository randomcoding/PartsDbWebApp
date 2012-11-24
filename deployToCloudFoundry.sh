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
CLEAN_PACKAGE='yes'
NO_LOGIN=''

function showUsage 
{
  cat << END
$0 --app-name <Name> --app-path <Path> [--app-mode production] [--no-build] [--vmc-login user@email.com] [--no-clean] [--no-login]

--app-name     The name of the application to deploy on cloudfoundry
--app-path     The path to deploy the app from
--app-mode     The mode to deploy the app into. Currently only supports production
--no-build     Do not perform a build before deploying
--vmc-login    The login name to use for vmc
--no-clean     Do not clean the project before building. Otherwise perform an incremental build.
               Has no effect if --no-build if specified
--no-login     Assume that a currently active vmc login exists. Otherwise the script will 
               login and logout at the end of the deployment.

END
}

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
      CLEAN_PACKAGE='no'
      shift
      ;;
    --no-login)
      NO_LOGIN='yes'
      shift
      ;;
    -h|--help)
      showUsage
      exit 1
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
  BUILD_OK=$?
else
  echo "No SBT Build performed at user request"
  BUILD_OK=-1
fi

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

if [ "${NO_LOGIN}" != "yes" ] ; then
  # login via VMC
  $VMC login ${VMC_LOGIN}
fi

# update the app
echo "Updating deployment of ${APP_NAME}"
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

if [ "${NO_LOGIN}" != "yes" ] ; then
  # Logout of VMC
  echo "Logging out of VMC"
  $VMC logout
fi

exit $RETVAL

