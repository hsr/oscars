#!/bin/bash
#
# Script to setup OSCARS for OSCARS IDC
#
# Xi Yang xyang@isi.edu November 20, 2011
# Andy Lake andy@es.net December 7, 2011
###################################################


SRCDIR=`dirname $0`
CONFDIR="$SRCDIR/../etc"
. ${CONFDIR}/ADVANCED_CONF > /dev/null
. ${CONFDIR}/OSCARS_VM_CONF > /dev/null

#Check that everything is set
if [ -z "$DOMAIN_ID" ]; then
    echo "Error: DOMAIN_ID not specified in OSCARS_VM_CONF";
    exit 1;
fi
if [ -z "$ORGANIZATION" ]; then
    echo "Error: ORGANIZATION not specified in OSCARS_VM_CONF";
    exit 1;
fi
if [ -z "$PUBLISH_SERVICE_INFO" ]; then
    echo "Error: PUBLISH_SERVICE_INFO not specified in OSCARS_VM_CONF";
    exit 1;
fi

if [ -z "$SSHCMD" ]; then
    echo "Error: SSHCMD not specified in ADVANCED_CONF";
    exit 1;
fi

if [ -z "$OSCARS_HOME" ]; then
    echo "Error: OSCARS_HOME not specified in ADVANCED_CONF";
    exit 1;
fi

if [ -z "$IDC_HOST" ]; then
    echo "Error: IDC_HOST not specified in ADVANCED_CONF";
    exit 1;
fi

if [ -z "$TS_HOST" ]; then
    echo "Error: TS_HOST not specified in ADVANCED_CONF";
    exit 1;
fi

if [ -z "$LS_HOST" ]; then
    echo "Error: LS_HOST not specified in ADVANCED_CONF";
    exit 1;
fi


# update up publishUrl
result=`$SSHCMD sudo grep -r MY_OSCARS_HOSTNAME $OSCARS_HOME/* | grep ml | cut -d ":" -f 1 | $SSHCMD sudo xargs sed -i "s,MY_OSCARS_HOSTNAME,${IDC_HOST},g " > /dev/null 2> /dev/null`

# update localDomain ID
result=`$SSHCMD sudo sed -i "s,id:.*,id:\ \'${DOMAIN_ID}\',g" $OSCARS_HOME/Utils/conf/config.yaml`

# update topoBridge
echo "   Updating topology..."
result=`$SSHCMD sudo cp $OSCARS_HOME/TopoBridgeService/conf/mydomain.xml $OSCARS_HOME/TopoBridgeService/conf/${DOMAIN_ID}.xml`;
result=`$SSHCMD sudo sed -i "s,MYDOMAIN,${DOMAIN_ID},g" $OSCARS_HOME/TopoBridgeService/conf/${DOMAIN_ID}.xml`
result=`$SSHCMD sudo sed -i "s,MYDOMAIN,${DOMAIN_ID},g" $OSCARS_HOME/TopoBridgeService/conf/config.*.yaml`
if [ $PUBLISH_SERVICE_INFO == 1 ]; then
    result=`$SSHCMD sudo sed -i "s,#registerUrl,registerUrl,g" $OSCARS_HOME/TopoBridgeService/conf/config.*.yaml`
    result=`$SSHCMD sudo sed -i "s,registerUrl:.*,registerUrl:\ \ \ \ \'http://${TS_HOST}:8012/perfSONAR_PS/services/topology\',g" $OSCARS_HOME/TopoBridgeService/conf/config.*.yaml`
else
    result=`$SSHCMD sudo sed -i "s,registerUrl,#registerUrl,g" $OSCARS_HOME/TopoBridgeService/conf/config.*.yaml`
fi
result=`$SSHCMD sudo sed -i "s,servers:.*,servers:\ \ \ \ \[\ \'http://${TS_HOST}:8012/perfSONAR_PS/services/topology\'\ \],g" $OSCARS_HOME/TopoBridgeService/conf/config.*.yaml`

#Update lookup service
echo "   Updating lookup service information..."
if [ $PUBLISH_SERVICE_INFO == 1 ]; then
    result=`$SSHCMD sudo sed -i "s,disableRegister:.*,disableRegister: 0,g" $OSCARS_HOME/LookupService/conf/config.*.yaml`
else
    result=`$SSHCMD sudo sed -i "s,disableRegister:.*,disableRegister:\ 1,g" $OSCARS_HOME/LookupService/conf/config.*.yaml`
fi
result=`$SSHCMD sudo sed -i "s,MY_LS_HOST,http://${LS_HOST}:8005/perfSONAR_PS/services/hLS,g" $OSCARS_HOME/LookupService/conf/config.*.yaml`

#Add user organization and site
echo "   Updating user tables..."
result=`$SSHCMD "sudo echo \"INSERT IGNORE INTO institutions VALUES(NULL, '${ORGANIZATION}');\" | mysql -u oscars -poscars123! authn"`
result=`$SSHCMD "sudo echo \"UPDATE users SET institutionId=(SELECT id FROM institutions WHERE name='${ORGANIZATION}');\" | mysql -u oscars -poscars123! authn"`
result=`$SSHCMD "sudo echo \"INSERT IGNORE INTO sites VALUES(NULL, '${DOMAIN_ID}', '${ORGANIZATION}');\" | mysql -u oscars -poscars123! authz"`

#Generate certificates
echo "   Generating certificates..."
result=`$SSHCMD "keytool -genkey \
    -dname \"CN=localhost, OU=OSCARS, O=${ORGANIZATION}, C=US\" \
    -keystore $OSCARS_HOME/sampleDomain/certs/localhost.jks -keyalg RSA -storetype jks -storepass changeit -keypass changeit"`;
result=`$SSHCMD "keytool -export -keystore $OSCARS_HOME/sampleDomain/certs/localhost.jks -alias mykey -storepass changeit -file $OSCARS_HOME/sampleDomain/certs/localhost.cer"`
result=`$SSHCMD "keytool -import -keystore $OSCARS_HOME/sampleDomain/certs/template.jks -noprompt -alias localhost -storepass changeit -file $OSCARS_HOME/sampleDomain/certs/localhost.cer"`
result=`$SSHCMD "rm $OSCARS_HOME/sampleDomain/certs/localhost.cer"`
result=`$SSHCMD "keytool -genkey \
    -dname \"CN=${IDC_HOST}, OU=OSCARS, O=${ORGANIZATION}, C=US\" \
    -keystore $OSCARS_HOME/sampleDomain/certs/template.jks -keyalg RSA -storetype jks -storepass changeit -keypass changeit"`;
result=`$SSHCMD mv $OSCARS_HOME/sampleDomain/certs/template.jks $OSCARS_HOME/sampleDomain/certs/oscarsidc.jks`;
result=`$SSHCMD ln -s $OSCARS_HOME/sampleDomain/certs/oscarsidc.jks $OSCARS_HOME/sampleDomain/certs/client.jks`;
result=`$SSHCMD ln -s $OSCARS_HOME/sampleDomain/certs/oscarsidc.jks $OSCARS_HOME/sampleDomain/certs/service.jks`;
result=`$SSHCMD ln -s $OSCARS_HOME/sampleDomain/certs/oscarsidc.jks $OSCARS_HOME/sampleDomain/certs/truststore.jks`;

#Insert NB user
result=`$SSHCMD "sudo echo \"UPDATE users SET certIssuer='CN=${IDC_HOST}, OU=OSCARS, O=${ORGANIZATION}, C=US', certSubject='CN=${IDC_HOST}, OU=OSCARS, O=${ORGANIZATION}, C=US', login='$DOMAIN_ID' WHERE login='localdomain';\" | mysql -u oscars -poscars123! authn"`

