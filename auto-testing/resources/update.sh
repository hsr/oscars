#!/bin/bash

#MODE=DEVELOPMENT
MODE=PRODUCTION

export MAVEN_OPTS='-Xmx512M -XX:MaxPermSize=512M'

OSCARS_DIST=/usr/local/oscars_dist
OSCARS_HOME=/usr/local/oscars
MVN_HOME=/usr/local/maven


cd $OSCARS_DIST
$OSCARS_DIST/bin/stopServers.sh ALL

mkdir -p $HOME/TopoBridgeService
cp -f $OSCARS_HOME/TopoBridgeService/conf/config.HTTP.yaml $HOME/TopoBridgeService
cp -f $OSCARS_HOME/TopoBridgeService/conf/config.SSL.yaml $HOME/TopoBridgeService

mkdir -p $HOME/Utils
cp -f $OSCARS_HOME/Utils/conf/config.yaml $HOME/Utils

mkdir -p $HOME/WBUIService
cp -f $OSCARS_HOME/WBUIService/conf/jetty.HTTP.xml $HOME/WBUIService
cp -f $OSCARS_HOME/WBUIService/conf/jetty.SSL.xml $HOME/WBUIService

mkdir -p $HOME/WSNBrokerService
cp -f $OSCARS_HOME/WSNBrokerService/conf/config.HTTP.yaml $HOME/WSNBrokerService
cp -f $OSCARS_HOME/WSNBrokerService/conf/config.SSL.yaml $HOME/WSNBrokerService

cp -f $OSCARS_DIST/api/Lib/SimpleTest.pm $OSCARS_DIST/api/Lib/SimpleTest.pm.bak


$MVN_HOME/bin/mvn clean
find . -name \*wsdl -exec touch {} \;
svn update
$MVN_HOME/bin/mvn install 


dirs=( AuthNPolicyService AuthNService AuthZPolicyService AuthZService BandwidthPCE ConnectivityPCE CoordService DijkstraPCE LookupService NotificationBridgeService OSCARSInternalService OSCARSService PCEService PSSService ResourceManagerService TopoBridgeService Utils VlanPCE WBUIService WSNBrokerService )

for dir in ${dirs[@]}
do
	if [ -d $OSCARS_HOME/$dir/conf ]; then
		cd $OSCARS_HOME/$dir/conf
		rm -rf *
	fi
done

$OSCARS_DIST/bin/exportconfig

cp -f $HOME/TopoBridgeService/* $OSCARS_HOME/TopoBridgeService/conf
cp -f $HOME/Utils/* $OSCARS_HOME/Utils/conf
cp -f $HOME/WBUIService/* $OSCARS_HOME/WBUIService/conf
cp -f $HOME/WSNBrokerService/* $OSCARS_HOME/WSNBrokerService/conf

cp -f $OSCARS_DIST/auto-testing/resources/testdomain* $OSCARS_HOME/TopoBridgeService/conf/
cp -rf $OSCARS_DIST/auto-testing/Lib $OSCARS_DIST/api/
cp -f $OSCARS_DIST/api/Lib/SimpleTest.pm.bak $OSCARS_DIST/api/Lib/SimpleTest.pm

#switch back to OSCARS_DIST so that the .out files will be put there
cd $OSCARS_DIST

$OSCARS_DIST/bin/startServers.sh $MODE ALL
$OSCARS_DIST/bin/testServers.sh $MODE 

