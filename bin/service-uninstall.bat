@echo off
setlocal
pushd %~d0%~p0

fly-ticket-notifier.exe //DS//FlyTicketNotifier
echo OK!

popd
@pause