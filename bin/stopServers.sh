#!/bin/sh

# Script to stop OSCARS services.
# Call with list of servers to be stopped.
# ALL will start all the servers.Individual server args are:
#  authN authZ api coord topoBridge rm stubPSS lookup wbui
#  stubPCE bwPCE connPCE dijPCE vlanPCE nullAGG stubPSS
# Uses the pid files in $OSCARS_HOME/run to find processes to stop


printUsage() {
   echo
   echo "usage stopServers <server>"
   echo "<server> is either ALL or one or more of:"
   echo "     authN authZ api coord topoBridge rm stubPSS dragonPSS eomplsPSS openflowPSS PSS"
   echo "     lookup wbui stubPCE bwPCE connPCE dijPCE vlanPCE nullAgg notifyBridge wsnbroker ionui"
   exit 1
}

if [ $# -lt 1 ]; then
    printUsage
fi
if  [ -z $OSCARS_HOME ]; then
    echo "Please set the environment var OSCARS_HOME to the OSCARS deployment directory"
    exit -1
 fi
 DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
 
stopService() {
PID_FILE=$DEFAULT_PID_DIR/$ShortName.pid
if [ -f $PID_FILE ]
then
   PID=`cat $PID_FILE`
   echo killing $Service
   `kill -9 $PID`
   rm $PID_FILE
else
    echo "$Service is not running"
fi
}

stopauthN() {
    Service="AuthNService"
    ShortName="authN"
    stopService
}
stopauthZ() {
    Service="AuthZService"
    ShortName="authZ"
    stopService
}

stopCoord () {
    Service="CoordinatorService"
    ShortName="coord"
    stopService
}

stopRM () {
    Service="ResourceManager"
    ShortName="rm"
    stopService
}

stopTopoBridge() {
    Service="TopoBridgeService"
    ShortName="topoBridge"
    stopService
}

stopStubPCE(){
    Service="stubPCE"
    ShortName="stubPCE"
    stopService
}

stopConnPCE() {
    Service="ConnectivityPCE"
    ShortName="connPCE"
    stopService
}

stopBWPCE() {
    Service="BandwidthPCE"
    ShortName="bwPCE"
    stopService
}

stopDijPCE () {
    Service="DijkstraPCE"
    ShortName="dijPCE"
    stopService
}

stopVlanPCE () {
    Service="VlanPCE"
    ShortName="vlanPCE"
    stopService
}

stopL3MplsPCE () {
    Service="L3MplsPCE"
    ShortName="l3mplsPCE"
    stopService
}

stopstubPSS () {
    Service="StubPSS"
    ShortName="stubPSS"
    stopService
}

stopsdnPSS () {
    Service="SdnPSS"
    ShortName="sdnPSS"
    stopService
}



##############################################################################
# Subroutine to stop PSS. Checks for arguments and stops the respective PSS
# If none specified, then looks for PIDs from a known list of PSS and stops the
# PSS based on the PID found
# Note: Does not check for unix oid of process, only what OSCARS stores
###############################################################################
stopPSS () {
	#echo "PSS Type:$1"
	PSSOptions=( stub dragon eompls openflow sdn )
	if [ ! -z $1 ] ; then
		PSSType="$1PSS"
	else
		PSSType="*PSS"
	fi
	#echo "PSS Type = $PSSType"
	PID_FILE=$DEFAULT_PID_DIR/$PSSType.pid
	
	NO_PSS=0
	if [ -f $PID_FILE ]; then
		NO_PSS=1
	else
		for opt in  ${PSSOptions[@]}
		do
			PID_FILE="$DEFAULT_PID_DIR/"$opt"PSS.pid"
			#echo "PSS : $PSSFile $opt"
			if [ -f $PID_FILE ]; then
				echo "The PSS currently running is not the one you specified.."
				echo ""$opt"PSS is running. Now killing ..." 
				NO_PSS=1
				break;
			fi
		done
	fi
	#echo "PSS FOUND? $NO_PSS"
	if [ $NO_PSS -eq 0 ]; then
		echo "PSS is not running"
	else
		PID=`cat $PID_FILE`
   		echo killing ""$opt"PSS" #PSS
   		`kill -9 $PID`
    		rm $PID_FILE
	fi
}

stopnullPCE () {
    Service="PCEService"
    ShortName="nullpce"
    stopService
}

stopnullAgg () {
    Service="NullAggregator"
    ShortName="nullagg"
    stopService
}

stopOSCARSService() {
    Service="OSCARSService"
    ShortName="api"
    stopService
}

stopLookup() {
    Service="LookupService"
    ShortName="lookup"
    stopService
}

stopNotificationBridge() {
    Service="NotificationBridgeService"
    ShortName="notificationBridge"
    stopService
}

stopWSNBroker() {
    Service="WSNBrokerService"
    ShortName="wsnbroker"
    stopService
}

stopWBUI() {
    Service="WBUIService"
    ShortName="wbui"
    stopService
}

stopIONUI() {
    Service="IONUIService"
    ShortName="ionui"
    stopService
}

while [ ! -z $1 ]
  do 
  case $1 in
  ALL)
    stopauthN
    stopauthZ
    stopOSCARSService
    stopCoord
    stopRM
    stopTopoBridge
#    stopStubPCE 
    stopConnPCE
    stopBWPCE
    stopDijPCE
    stopVlanPCE
    stopL3MplsPCE
#    stopstubPSS
    stopPSS #Stops whichever PID is present
#    stopnullPCE
    stopnullAgg
    stopLookup
    stopNotificationBridge
    stopWSNBroker
    stopWBUI
    stopIONUI;;
  authN)    stopauthN;;
  authZ)    stopauthZ;;
  api)      stopOSCARSService;;
  coord)    stopCoord;;
  rm)       stopRM;;
  topoBridge) stopTopoBridge;;
#  stubPCE)  stopStubPCE;;
  connPCE)  stopConnPCE;;
  bwPCE)    stopBWPCE;;
  dijPCE)   stopDijPCE;;
  vlanPCE)  stopVlanPCE;;
  nullPCE)  stopnullPCE;;
  nullAgg)  stopnullAgg;;
#  stubPSS)  stopstubPSS;;
#PSS option stops which ever PSS is running
  PSS)	stopPSS;;
  stubPSS)  stopPSS "stub";;
  sdnPSS)  stopPSS "sdn";;
  sdnPSS)  stopPSS "sdn";;
  dragonPSS) stopPSS "dragon";;
  eomplsPSS) stopPSS "eompls";;
  openflowPSS) stopPSS "openflow";;
  lookup)   stopLookup;;
  wbui)     stopWBUI;;
  notifyBridge)     stopNotificationBridge;;
  wsnbroker) stopWSNBroker;;
  ionui)    stopIONUI;;
  *)        echo server $1 not recognized;;
  esac
  shift
done
