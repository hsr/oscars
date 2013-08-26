#!/bin/bash



MODULEPATH=$(cd $(dirname $0); cd ..; pwd)
MODULENAME=${MODULEPATH##*/}

function usage () {
	echo "Usage: $0 [-h|-r|-b]"
	echo ""
	echo "       -r : restart only, do not rebuild"
	echo "       -b : rebuild only, do not restart"
	echo "       -h : print usage"
	exit 1
}

if [ "${1}" == "-h" ]; then
	usage;
elif [ "${1}" != "" ] && [ "${1}" != "-r" ] && [ "${1}" != "-b" ]; then
	echo "What is ${1}?"
	usage;
fi

if [ "${1}" != "-r" ]; then
	cd ${OSCARS_DIST}/${MODULENAME};
	mvn install -DskipTests;
	cd -;
fi

# Stupid lack of padronization
#MODULENAME=dijPCE

if [ "${1}" != "-b" ]; then
	${OSCARS_DIST}/bin/stopServers.sh ${MODULENAME}; 
	${OSCARS_DIST}/bin/startServers.sh PRO ${MODULENAME};
fi
