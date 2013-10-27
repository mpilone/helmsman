#!/bin/bash

function do_status {
  ssh someuser@$REMOTE_HOST /home/someuser/bin/cmdStatus.sh 2>&1 | grep "Active" &> /dev/null
}

function do_start {
  do_status
  if [[ "$?" -eq "0" ]]
  then
    return 0
  fi
  
  ssh someuser@$REMOTE_HOST /home/someuser/bin/cmdStart.sh 2>&1 | grep "Active" &> /dev/null
}

function do_stop {
  do_status
  if [[ "$?" -ne "0" ]]
  then
    return 0
  fi
  
  ssh someuser@$REMOTE_HOST /home/someuser/bin/cmdStop.sh 2>&1 | grep "Standby" &> /dev/null
}

result=0
case $1 in
  "status")
	  echo -n "Checking status of cmd..."
	  do_status
	   result=$? 
	  [ $result -eq 0 ] && echo "UP" || echo "DOWN"
	  ;;
	"start")
	  echo -n "Starting cmd..."
	  do_start
	  result=$?
	  [ $result -eq 0 ] && echo "OK" || echo "FAILED"
	  ;;
	"stop")
	  echo -n "Stopping cmd..."
	  do_stop
	  result=$?
	  [ $result -eq 0 ] && echo "OK" || echo "FAILED"
	  ;;
esac

exit $result