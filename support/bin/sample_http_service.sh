#!/bin/bash

# --------------------
# Prints a progress '.' if the given value is a multiple 
# of 5 (i.e. every 5 seconds). Normally this method is 
# called in a loop that is sleeping and waiting for some 
# other process to end.
#
# param 1: the number to check if it is a multiple of 5
# --------------------
function print_progress {
  let "z=$1 % 5"
  if [[ "$z" -eq "0" ]]
  then
    echo -n "."
  fi
}

function do_status {
  #wget -O- -q --tries=1 --retry-connrefused --timeout=2 --wait=10 http://localhost:8011/ > /dev/null
  curl -s --retry 2 --connect-timeout 5 --max-time 10 --retry-delay 2 http://localhost:8011/ > /dev/null
}

function do_start {
  do_status
  if [[ "$?" -eq "0" ]]
  then
    return 0
  fi
  
  nohup /opt/bin/startHttpDaemon.sh &> /dev/null &
  
  count=0
  while [ "$count" -lt "300" ]
  do
    do_status
    if [[ "$?" -eq "0" ]]
    then
      return 0
    fi
    sleep 2
    let "count=count + 1" 
    print_progress "$count" 
  done
  
  return 1
}

function do_stop {
  do_status
  if [[ "$?" -ne "0" ]]
  then
    return 0
  fi
  
  /opt/bin/stopHttpDaemon.sh &> /dev/null &
  
  count=0
  while [ "$count" -lt "200" ]
  do
    do_status
    if [[ "$?" -ne "0" ]]
    then
      return 0
    fi
    sleep 2
    let "count=count + 1" 
    print_progress "$count" 
  done
  
  return 1
}


result=0
case $1 in

  "status")
    echo -n "Checking status of HttpDaemon..."
    do_status
    result=$?
    [ $result -eq 0 ] && echo "UP" || echo "DOWN"
    ;;
    
  "start")
    echo -n "Starting HttpDaemon..."
    do_start
    result=$?
    [ $result -eq 0 ] && echo "UP" || echo "FAILED"
    ;;
    
  "stop")
    echo -n "Stopping HttpDaemon..."
    do_stop
    result=$?
    [ $result -eq 0 ] && echo "DOWN" || echo "FAILED"
    ;;
  
esac

exit $result