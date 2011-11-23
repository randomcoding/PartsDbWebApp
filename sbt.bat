
set SBT_VER=0.11.1
set JAVA_OPTS="-d32 -Dfile.encoding=UTF8 -Xmx512M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m"
set LAUNCH_DIR=%~dp0

java %JAVA_OPTS% -jar "%LAUNCH_DIR%sbt-%SBT_VER%-launch.jar " %*

