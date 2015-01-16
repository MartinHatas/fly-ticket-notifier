@echo off
setlocal
pushd %~d0%~p0

fly-ticket-notifier.exe //IS//FlyTicketNotifier --DisplayName="Fly Ticket Notifier" --Startup auto --Install=%cd%\fly-ticket-notifier.exe --Jvm=auto --Classpath fly-ticket-notifier.jar --StartMode=jvm --StartClass=cz.hatoff.ftn.FtnApplication --StartMethod start --StopMode=jvm --StopClass=cz.hatoff.ftn.FtnApplication --StopMethod stop
echo OK!

popd
@pause