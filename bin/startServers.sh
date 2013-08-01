#!/bin/sh  
# Script to start OSCARS services.
# Call with a context and a list of servers to start.
# ALL will start all the servers.Individual server args are:
#  authN authZ api coord topoBridge rm stubPSS lookup wbui
# stubPCE bwPCE connPCE dijPCE vlanPCE nullAGG stubPSS
# saves server pids in $OSCARS_HOME/run for stopServers to use
# server output is directed to files in the current directory

DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d $DEFAULT_PID_DIR ]
then
   mkdir $DEFAULT_PID_DIR
fi

if  [ -z $OSCARS_DIST ]; then
    echo "Please set the environment var OSCARS_DIST to the OSCARS source directory"
    exit -1
 fi
 
printUsage() {
   echo
   echo "usage startServers [-v] <context> <server >"
   echo " -v  prints out debugging messages. Must be first arg"
   echo "<context> is one of: PRODUCTION|pro UNITTEST|test DEVELOPMENT|dev SDK|sdk"
   echo "<server> is either ALL or one or more of:"
   echo "     authN authZ api coord topoBridge rm stubPSS eomplsPSS dragonPSS openflowPSS PSS"
   echo "     lookup wbui bwPCE connPCE dijPCE vlanPCE nullAgg notifyBridge wsnbroker ionui"
   exit 1
}
startService() {
   serviceDir=$1
   service=$2
   shortName=$3
   srcDir=$4
   Config=$(sh $OSCARS_DIST/bin/parseManifest.sh $serviceDir $service $CONTEXT | sed "s/'//g")
   if [ $debug ]; then
       echo "Starting $service Using configuration $Config"
   fi
   Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
   Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
   if [ "$Conf" == "conf" ]; then
       if [ $debug ]; then
           echo "using configuration file $OSCARS_HOME/$serviceDir/$Conf/$Yaml"
       fi
       port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_HOME/$serviceDir/$Conf/$Yaml)
   elif [ "$Conf" == "config" ]; then
       if [ $debug ]; then
           echo "using configuration file  $OSCARS_DIST/$srcDir/$Conf/$Yaml"
       fi
       port=$(awk -F: '/soap/,/public/ $1~/publishTo/{print $4}' $OSCARS_DIST/$srcDir/$Conf/$Yaml)
   fi
    if [ $debug ]; then
       echo "port definition line is  $port"
   fi
   port=$(echo $port | sed "s/[^0-9]//g")
   if [ $debug ]; then
       echo "port is $port"
   fi
   if [ $debug ]; then
       line=`netstat -na | grep tcp | grep LISTEN | grep "[:|\.]$port "`
       echo "checking netstat $line"
   fi
   porttest=`netstat -na | grep tcp | grep LISTEN | grep "[:|\.]$port "`
   if [ ! -z "$porttest" ]; then
       echo $service is  already running
   else
       echo starting $shortName on port $port
       if [ $shortName != "nullAgg" ]; then
           (cd $OSCARS_DIST/$srcDir; bin/startServer.sh $CONTEXT > $currDir/$shortName.out 2>&1  & )
       else
           (cd $OSCARS_DIST/$srcDir; bin/startNullAgg.sh $CONTEXT > $currDir/$shortName.out 2>&1 &)
       fi
   fi
}
startauthN() {
   startService  "AuthNService" "AuthNService" "authN" "authN"
}

startauthZ(){
   startService  "AuthZService" "AuthZService" "authZ"  "authZ"
}

startOSCARSService() {
    startService  "OSCARSService" "OSCARSService" "api" "api"
}

startCoord() {
    startService "CoordService" "CoordService" "coord" "coordinator"
}

startRM(){
    startService "ResourceManagerService" "ResourceManagerService" "rm" "resourceManager"
}

startTopoBridge() {
    startService "TopoBridgeService" "TopoBridgeService" "topoBridge" "topoBridge"
}

startNotificationBridge() {
    startService  "NotificationBridgeService" "NotificationBridgeService" "notificationBridge" "notificationBridge"
}

startWSNBroker() {
    startService "WSNBrokerService" "WSNBrokerService" "wsnbroker" "wsnbroker"
}

startStubPCE(){
    startService "StubPCE" "StubPCE" "stubPCE" "stubPCE"
}

startConnPCE() {
    startService "ConnectivityPCE" "ConnectivityPCE" "connPCE" "connectivityPCE"
}

startBandwidthPCE() {
    startService "BandwidthPCE" "BandwidthPCE" "bwPCE" "bandwidthPCE"
}

startDijPCE () {
    startService  "DijkstraPCE" "DijkstraPCE" "dijPCE" "dijkstraPCE"
}

startVlanPCE () {
    startService "VlanPCE" "VlanPCE" "vlanPCE" "vlanPCE"
}

startL3MplsPCE () {
    startService "L3MplsPCE" "L3MplsPCE" "l3mplsPCE" "l3mplsPCE"
}

startnullPCE () {
    startService "PCEService" "PCEService" "nullpce" "pce"

}

startnullAgg() {
    startService "PCEService" "NullAggregator" "nullAgg" "pce"
}

##########Subroutine to decide which PSS to start
startPSS() {
    # Why variables if these strings are used just once?
    DRAGONPSS="DRAGON"
    EOMPLSPSS="EOMPLS"
    OPENFLOWPSS="OPENFLOW"
    #Get PSS choice, but keep stubPSS the default
    whichPSS="STUB"
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh Utils Utils $CONTEXT )
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    # this is so cryptic. Why is it reading config from dist? 
    # Shouldn't all configs be at ${OSCARS_HOME}?
    if [ "$Conf" == "conf" ]; then
        whichPSS=$(awk -F: '$1~/PSSChoice/{print $2}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
        whichPSS=$(awk -F: '$1~/PSSChoice/{print $2}' $OSCARS_DIST/$Service/$Conf/$Yaml)
    fi
    whichPSS=$(echo $whichPSS | sed 's/^ *\(.*\) *$/\1/')
    if [ $debug ]; then
        echo "Starting PSS :$whichPSS"
    fi
    #Now start based on choice obtained
    if [ "$whichPSS" == "$DRAGONPSS" ]; then
        startDragonPSS
    elif [ "$whichPSS" == "$EOMPLSPSS" ]; then
        startEomplsPSS
    elif [ "$whichPSS" == "$OPENFLOWPSS" ]; then
        startOpenflowPSS
    elif [ "${whichPSS}" == "SDN" ]; then
        startSdnPSS
    else
        startStubPSS
    fi
}

startStubPSS(){
    startService "PSSService" "PSSService" "stubPSS" "stubPSS"
}

startSdnPSS(){
    startService "PSSService" "PSSService" "sdnPSS" "sdnPSS"
}

startDragonPSS(){
    startService "PSSService" "PSSService" "dragonPSS" "dragonPSS"
}

startEomplsPSS(){
    startService "PSSService" "PSSService" "eomplsPSS" "eomplsPSS"
}

startOpenflowPSS(){
    startService "PSSService" "PSSService" "openflowPSS" "openflowPSS"
}

startLookup(){
    startService "LookupService" "LookupService" "lookup" "lookup"
}

startWBUI(){
# gets its port from the jetty.xml file
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh WBUIService WBUIService $CONTEXT jetty.xml)
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    if [ "$Conf" == "conf" ]; then
        if [ $debug ]; then
            echo "configuration file is $OSCARS_HOME/$Service/$Conf/$Yaml"
        fi
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
        if [ debug ]; then
            echo "configuration file is $OSCARS_DIST/$Service/$Conf/$Yaml"
        fi
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_DIST/$Service/$Conf/$Yaml)
    fi
    port=$(echo $port | sed "s/[^0-9]//g")
    porttest=`netstat -na | grep tcp | grep LISTEN | grep $port`
    if [ ! -z "$porttest" ]; then
        echo WBUI  already running
    else
       echo starting WBUI Server on port $port
       (cd $OSCARS_DIST/wbui; bin/startServer.sh $CONTEXT > $currDir/wbui.out 2>&1 &)
    fi
}

startIONUI(){
    Config=$(sh $OSCARS_DIST/bin/parseManifest.sh IONUIService IONUIService $CONTEXT jetty.xml)
    Service=$(echo $Config | awk -F/ '$1~//{print $2}')
    Conf=$(echo $Config | awk -F/ '$1~//{print $3}')
    Yaml=$(echo $Config | awk -F/ '$1~//{print $4}' | sed "s/'//g")
    if [ "$Conf" == "conf" ]; then
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_HOME/$Service/$Conf/$Yaml)
    elif [ "$Conf" == "config" ]; then
        port=$(awk -F\" '$4~/jetty.port/{print $6}' $OSCARS_DIST/$Service/$Conf/$Yaml)
    fi
    port=$(echo $port | sed "s/[^0-9]//g")
    porttest=`netstat -na | grep tcp | grep LISTEN | grep $port`
    if [ ! -z "$porttest" ]; then
        echo IONUI already running
    else
        echo starting IONUI Server on port $port
       (cd $OSCARS_DIST/ionui; bin/startServer.sh $CONTEXT > $currDir/ionui.out 2>&1 &)
    fi
}


# execution starts here
if [ $# -lt 2 ]; then
    printUsage
fi
currDir=$(pwd)
if [ $1 == "-v" ]; then
    debug=1
    shift
fi
CONTEXT=$1
case $1 in
    d|D|dev|DEV) CONTEXT="DEVELOPMENT";;
    p|P|pro|PRO) CONTEXT="PRODUCTION";;
    t|T|test|TEST) CONTEXT="UNITTEST";;
    s|S|sdk) CONTEXT="SDK";;
esac
   
if [ "$CONTEXT" ==  "PRODUCTION" ] || [ "$CONTEXT" == "UNITTEST" ] || [ "$CONTEXT" == "DEVELOPMENT" ] || [ "$CONTEXT" == "SDK" ]; then
    echo "Start services in $CONTEXT context"
else
    echo "context  $CONTEXT is not recognized"
    printUsage
fi
shift
while [ ! -z $1 ]
    do 
    case $1 in
    ALL)
      startLookup
      startTopoBridge
      startRM
      startauthN
      startauthZ
      startCoord
#     startStubPCE
#     startnullPCE
      startnullAgg
      startDijPCE
      startConnPCE
      startBandwidthPCE
      startVlanPCE
      startL3MplsPCE
      startPSS
      startNotificationBridge
      startWSNBroker
      startWBUI
      startOSCARSService;;  
    authN)    startauthN;;
    authZ)    startauthZ;;
    api)      startOSCARSService;;
    coord)    startCoord;;
    topoBridge) startTopoBridge;;
    rm)       startRM;;
#   stubPCE)  startStubPCE;;
    bwPCE)    startBandwidthPCE;;
    connPCE)  startConnPCE;;
    dijPCE)   startDijPCE;;
    vlanPCE)  startVlanPCE;;
    nullPCE)  startnullPCE;;
    nullAgg)  startnullAgg;;
    PSS)      startPSS;;
    stubPSS) startPSS;; #TBD- remove generic used for testing startStubPSS;;
    sdnPSS)   startPSS;; #TBD- remove generic used for testing startStubPSS;;
    dragonPSS)startDragonPSS;;
    eomplsPSS)startEomplsPSS;;
    openflowPSS)startOpenflowPSS;;
    lookup)   startLookup;;
    wbui)     startWBUI;;
    notifyBridge)     startNotificationBridge;;
    wsnbroker) startWSNBroker;;
    ionui) startIONUI;;
    *)        echo $1 not a recognized server
  esac
  shift
done

