#!/bin/sh

CONTFND=0
SVCFND=0
CONFFND=0
CONFILE=config.yaml

case $# in
  0)
   echo "usage parseManifest <arg1> <arg2>"
   echo "<arg1> is Service Directory Name"
   echo "<arg2> is Service name"
   echo "<arg3> is Context "
   echo "<arg4> is configuration file name if different than config.yaml"
   exit;;
  4)
   CONFILE=$4
esac

SERVICEDIR=$1
SERVICE=$2
CONTEXT=$3

for i in `cat $OSCARS_HOME/$SERVICEDIR/conf/manifest.yaml`
do
  Line=$(echo $i)
  if  [ $CONFFND -eq 1 ];then
         echo $Line
         break
  elif [ $SVCFND -eq 0 ]; then
         CON1=$(echo $Line | awk '$1~/'$SERVICE'/{print "FOUND"}')
         if [ "$CON1" == "FOUND" ];then
              SVCFND=1              
         fi
  elif [ $SVCFND -eq 1 ] && [ $CONTFND -eq 0 ]; then
         CON2=$(echo $Line | awk -F: '$1~/'$CONTEXT'/{print "FOUND"}')
         if [ "$CON2" == "FOUND" ]; then
              CONTFND=1
         fi
  elif [ $SVCFND -eq 1 ] && [ $CONTFND -eq 1 ] && [ $CONFFND -eq 0 ]; then
         CON3=$(echo $Line | awk -F: '$1~/'$CONFILE'/{print "FOUND"}')
         if [ "$CON3" == "FOUND" ]; then
               CONFFND=1
         fi
  fi
done
