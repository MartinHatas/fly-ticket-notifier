@echo off
setlocal
pushd %~d0%~p0

fly-ticket-notifier.exe //ES//FlyTicketNotifier
echo OK!

popd
@pause