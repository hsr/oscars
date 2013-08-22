# OSCARS: ESnet's Circuit Reservation System

This repository is a git import of [OSCARS](http://www.es.net/services/virtual-circuits-oscars/), the ESnet's circuit reservation system. The original repository can be found [here](https://oscars.es.net/repos/).

## Why? 
In the summer of 2013, I worked with the integration of [OSCARS](http://www.es.net/services/virtual-circuits-oscars/ OSCARS) and [Floodlight](http://www.projectfloodlight.org/floodlight/). In the SDN context, OSCARS is an application that controls the [es.net](http://es.net/) 100G network and Floodlight is a SDN controller that communicates with network devices using [OpenFlow](http://www.openflow.org/). I used this repository to keep track of my changes to OSCARS.

## Usage

This version of OSCARS was deployed and tested in a CentOS 6.4 machine. If you want to use it on a Ubuntu machine, you need to fix the syntax of the scripts. Also, this version of OSCARS requires Floodlight deployed and running.

The first step is to get a copy of the repository and its submodules:

    git clone --recursive https://github.com/hsr/oscars


#### Configuration

**To use the SDN Path Setup Subsystem (PSS):** change the PSS module you want to use in `oscars/tools/utils/config/config.yaml ` to `SDN` instead of `STUB`. You also need to change maven's project object model at `oscars/pom.xml` to build the SDN PSS instead of the original Stub PSS. Replace `<module>stubPSS</module>` with `<module>sdnPSS</module>`.

**(Optional) To fetch topology from Floodlight:** change the local domain id in `oscars/tools/utils/config/config.yaml ` to a valid SDN Topology Service Identification (TSI)  `sdn:<controller name>:<yourdomain>`. An example of a valid TSI would be `sdn:floodlight:my.domain`. This TSI instruct OSCARS's Topology Bridge module to use the Floodlight Topology Service implementation of ISDNTopologyService to fetch the topology (for more information, see net.es.oscars.topoBridge.sdn.ISDNTopologyService). You also need to add the parameter 'sdn' at the same level of the id you changed and set it to your controller's URL. Here is a complete example for domain `testdomain` and Floodlight's REST interface listening on port `8080` of server `floodlight.es.net`:

	...
    localDomain:
    	id: 'sdn.floodlight.testdomain'
    	sdn: 'http://floodlight.es.net:8080'
    ...

*Note that you don't need to use the SDN PSS to fetch topology from Floodlight.*


#### Compile and Install

Before compiling OSCARS, you need to set OSCARS's environment variables OSCARS_DIST and OSCARS_HOME. They should be set to the paths of OSCARS's source code directory and OSCARS's runtime configurations directory respectively. 

    export OSCARS_DIST=$(pwd)/oscars 
    export OSCARS_HOME=$(pwd)/oscars_home

Use maven to compile and install OSCARS:

    cd ${OSCARS_DIST}
    mvn install -DskipTests
      
After that, make sure you start your mysql database (and also reset if you want a fresh install of OSCARS):

    mysql -u root -e 'drop database rm; drop database authn; drop database authz'; \
    ${OSCARS_DIST}/authN/scripts/configure_database ${OSCARS_DIST}/authN/sql; \
    ${OSCARS_DIST}/authZ/scripts/configure_database ${OSCARS_DIST}/authZ/sql; \
    ${OSCARS_DIST}/resourceManager/scripts/configure_database ${OSCARS_DIST}/resourceManager/sql


#### Running OSCARS

Before starting OSCARS, you need to create an user and password. To do that, use the following script:

    ${OSCARS_DIST}/tools/bin/idc-useradd


OSCARS is composed by various modules. You can start and stop each of them manually, using the scrips `startServers.sh` and `stopServers.sh` from `${OSCARS_DIST}/bin/`. To start all of them in production mode, just run the following command:

    ${OSCARS_DIST}/bin/startServers.sh PRO ALL
    
Other possible contexts are DEV and SDK. 

Wait until all the services have started, and check if they are listening for connections using netstat:

    netstat -ntpl
    
Here is a list of imporant services:

	Lookup, port 9014
    TopologyBridge, port 9019
	ResourceManager, port 9006
    AuthN, port 9090
	AuthZ, port 9190
    Coordinator, port 9003
	NullAgg, port 10001
    DijkstraPCE, port 9008
	ConnectivityPCE, port 9007
    BandwidthPCE, port 9009
	VlanPCE, port 9010
    l3mplsPCE on port 90153
	sdnPSS on port 9050
    notificationBridge on port 9012
	wsnbroker on port 9013
    WBUI Server on port 8443
	api on port 9001

Once all the services started, point your browser to `https://<yourmachine>:8443/OSCARS`. Use the username and password created before.

You can check the logs and (possible) errors messages in the directory `${OSCARS_HOME}/logs/`.

