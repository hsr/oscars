#!/bin/sh

# service names
SVC_API=OSCARSService
SVC_API_INTERNAL=OSCARSInternalService
SVC_AUTHN=AuthNService
SVC_AUTHN_POLICY=AuthNPolicyService
SVC_AUTHZ=AuthZService
SVC_AUTHZ_POLICY=AuthZPolicyService
SVC_COORD=CoordService
SVC_LOOKUP=LookupService
SVC_NOTIFY=NotificationBridgeService
SVC_WSNBROKER=WSNBrokerService
SVC_PCE=PCEService
SVC_PSS=PSSService
SVC_RM=ResourceManagerService
SVC_TOPO=TopoBridgeService
SVC_WBUI=WBUIService
SVC_PCE_BW=BandwidthPCE
SVC_PCE_CONN=ConnectivityPCE
SVC_PCE_DIJ=DijkstraPCE
SVC_PCE_VLAN=VlanPCE
SVC_PCE_L3MPLS=L3MplsPCE
SVC_UTILS=Utils
SVC_ION=IONUIService #Added for ION

# convenient stuff so that we don't typo
CONFIG=config.yaml
CONFIG_HTTP=config.HTTP.yaml
CONFIG_SSL=config.SSL.yaml
MANIFEST=manifest.yaml
CXF_SERVER_HTTP_CFG=server-cxf-http.xml
CXF_SERVER_SSL_CFG=server-cxf-ssl.xml
CXF_CLIENT_HTTP_CFG=client-cxf-http.xml
CXF_CLIENT_SSL_CFG=client-cxf-ssl.xml
LOG4J_PROPS_INFO=log4j.INFO.properties
LOG4J_PROPS_DEBUG=log4j.DEBUG.properties
LOG4J_PROPS_MESSAGE=log4j.MESSAGE.properties

# if we don't have 2 args, complain and exit
function verifyArgs () {
    if [ -z "$1" -o -z "$2" ]
    then
        echo $0: usage: $0 OSCARS_DIST OSCARS_HOME
        exit 1
    fi
}

# make sure the service name is not typoed
verifySvcName () {
    SERVICE=$1
    case $SERVICE in
        $SVC_AUTHN)
            ;;
        $SVC_AUTHN_POLICY)
            ;;
        $SVC_AUTHZ)
            ;;
        $SVC_AUTHZ_POLICY)
            ;;
        $SVC_API)
            ;;
        $SVC_API_INTERNAL)
            ;;
        $SVC_COORD)
            ;;
        $SVC_LOOKUP)
            ;;
        $SVC_NOTIFY)
            ;;
        $SVC_WSNBROKER)
            ;;
        $SVC_PCE)
            ;;
        $SVC_PSS)
            ;;
        $SVC_RM)
            ;;
        $SVC_TOPO)
            ;;
        $SVC_WBUI)
            ;;
        $SVC_PCE_BW)
            ;;
        $SVC_PCE_CONN)
            ;;
        $SVC_PCE_DIJ)
            ;;
        $SVC_PCE_VLAN)
            ;;
        $SVC_PCE_L3MPLS)
            ;;
        $SVC_UTILS)
            ;;
	$SVC_ION) #Added for ION
	    ;;
        *)
            echo "Invalid service name: $SERVICE"
            exit 1
            ;;
    esac
}
