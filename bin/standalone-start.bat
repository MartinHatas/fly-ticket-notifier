@echo off
setlocal
pushd %~d0%~p0

set JAVA_OPTS=%JAVA_OPTS% -Xms16m -Xmx64m

java %JAVA_OPTS% -jar fly-ticket-notifier.jar

popd
endlocal
@pause