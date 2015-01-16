@echo off
setlocal
pushd %~d0%~p0

fly-ticket-notifier.exe //SS//FlyTicketNotifier
echo OK!

popd
@pause