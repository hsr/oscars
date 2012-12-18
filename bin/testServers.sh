#!/bin/sh

printUsage () {
   echo "usage testservers <CONTEXT>"
   echo "<context> is one of: PRODUCTION|pro UNITTEST|test DEVELOPMENT|dev SDK|sdk"
   exit 1
}
case $# in
  0)
   printUsage;;
esac

CONTEXT=$1
case $1 in
    d|D|dev|DEV) CONTEXT="DEVELOPMENT";;
    p|P|pro|PRO) CONTEXT="PRODUCTION";;
    t|T|test|TEST) CONTEXT="UNITTEST";;
    s|S|sdk) CONTEXT="SDK";;
esac

if [ "$CONTEXT" == "PRODUCTION" ] || [ "$CONTEXT" == "UNITTEST" ] || [ "$CONTEXT" == "DEVELOPMENT" ] || [ "$CONTEXT" == "SDK" ]; then
    echo "testing for servers running in $CONTEXT context";
else
    echo "CONTEXT  $CONTEXT is not recognized"
    printUsage
fi

testService() {
    serviceDir=$1
    service=$2
    shortName=$3
    srcDir=$4
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh $serviceDir $service $CONTEXT)
    #echo "Config is $Config"
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    #echo "Service is $service Conf is $Conf Yaml is $Yaml"
    if [ "$Conf" == "conf" ]; then
          #echo "using configuration  $OSCARS_HOME/$serviceDir/$Conf/$Yaml"
          port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_HOME/$serviceDir/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
          #echo "using configuration  $OSCARS_DIST/$srcDir/$Conf/$Yaml"
          port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_DIST/$srcDir/$Conf/$Yaml)
    fi
    port=$(echo $port | sed "s/[^0-9]//g")
    echo "$service port is $port"
    porttest1=`netstat -na | grep tcp | grep LISTEN | grep "[:|\.]$port "`
    if [ -z "$porttest1" ]; then
         echo "$service is not running";
         echo "Please restart $service using startServers.sh $CONTEXT $shortName";
         echo "-----------------------------------------------------------";
    else
         echo "$service is running";
         echo "-----------------------------------------------------------";
    fi
}

testService "AuthNService" "AuthNService" "authN" "authN"

testService "AuthZService" "AuthZService" "authZ" "authZ"

testService "OSCARSService" "OSCARSService" "api" "api"

testService "CoordService" "CoordService" "coord" "coordinator"

testService "ResourceManagerService" "ResourceManagerService" "rm" "resourceManager"

testService "TopoBridgeService" "TopoBridgeService" "topoBridge" "topoBridge"

testService "PCEService" "NullAggregator" "nullAgg" "pce"

testService "PSSService" "PSSService" "PSS" "stubPSS"

testService "ConnectivityPCE" "ConnectivityPCE" "connPCE" "connectivityPCE"

testService "BandwidthPCE" "BandwidthPCE" "bwPCE" "bandwidthPCE"

testService "DijkstraPCE" "DijkstraPCE" "dijPCE" "dijkstraPCE"

testService "VlanPCE" "VlanPCE" "vlanPCE" "vlanPCE"

testService "WSNBrokerService" "WSNBrokerService" "wsnbroker" "wsnbroker"

testService "LookupService" "LookupService" "lookup" "lookup"

testService "NotificationBridgeService" "NotificationBridgeService" "notifyBridge" "notificationBridge"

#WBUI service gets its port from jetty.xml
Config=$(sh $OSCARS_DIST/bin/parseManifest.sh WBUIService WBUIService $CONTEXT jetty.xml)
conf=$(echo $Config | awk -F/ '$1~//{print $3}')
yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
echo Config:$Config conf:$conf yaml:$yaml
if [ "$conf" == "conf" ]; then
     port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_HOME/WBUIService/$conf/$yaml)
elif [ "$conf" == "config" ]; then
     port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_DIST/wbui/$conf/$yaml)
fi
port=$(echo $port | sed "s/[^0-9]//g")
echo "WBUI port is $port"
porttest11=`netstat -na | grep LISTEN | grep "[:|\.]$port "`
if [ -z "$porttest11" ]; then
     echo "WBUI is not running";
     echo "Please restart WBUI using startServers.sh $CONTEXT wbui";
     echo "-----------------------------------------------------------";
else
     echo "WBUI is running";
     echo "-----------------------------------------------------------";
fi
