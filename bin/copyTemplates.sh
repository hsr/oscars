#!/bin/sh

if [ -z $OSCARS_DIST ]; then
        echo "Please set OSCARS_DIST to the OSCARS source directory"
        exit 1
fi
if [ ! -d $OSCARS_DIST ]; then
        echo "$OSCARS_DIST does not exist"
        exit 1
fi

cd $OSCARS_DIST

echo "Copying template config files in $OSCARS_DIST to non-template versions."
echo

echo "$OSCARS_DIST/authN/config/authN.HTTP.yaml.template -> authN.HTTP.yaml"
cp $OSCARS_DIST/authN/config/authN.HTTP.yaml.template $OSCARS_DIST/authN/config/authN.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.HTTP.yaml.template $OSCARS_DIST/authN/config/authN.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/authN/config/authN.SSL.yaml.template -> authN.SSL.yaml"
cp $OSCARS_DIST/authN/config/authN.SSL.yaml.template $OSCARS_DIST/authN/config/authN.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.SSL.yaml.template $OSCARS_DIST/authN/config/authN.SSL.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/authN/config/authN.TESTING.yaml.template -> authN.TESTING.yaml"
cp $OSCARS_DIST/authN/config/authN.TESTING.yaml.template $OSCARS_DIST/authN/config/authN.TESTING.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authN/config/authN.TESTING.yaml.template $OSCARS_DIST/authN/config/authN.TESTING.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/authZ/config/authZ.HTTP.yaml.template -> authZ.HTTP.yaml"
cp $OSCARS_DIST/authZ/config/authZ.HTTP.yaml.template $OSCARS_DIST/authZ/config/authZ.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.HTTP.yaml.template $OSCARS_DIST/authZ/config/authZ.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/authZ/config/authZ.SSL.yaml.template -> authZ.SSL.yaml"
cp $OSCARS_DIST/authZ/config/authZ.SSL.yaml.template $OSCARS_DIST/authZ/config/authZ.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.SSL.yaml.template $OSCARS_DIST/authZ/config/authZ.SSL.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/authZ/config/authZ.TESTING.yaml.template -> authZ.TESTING.yaml"
cp $OSCARS_DIST/authZ/config/authZ.TESTING.yaml.template $OSCARS_DIST/authZ/config/authZ.TESTING.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/authZ/config/authZ.TESTING.yaml.template $OSCARS_DIST/authZ/config/authZ.TESTING.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/coordinator/config/pce-configuration-http-template.xml -> pce-configuration-http.xml"
cp  $OSCARS_DIST/coordinator/config/pce-configuration-http-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-http.xml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/coordinator/config/pce-configuration-http-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-http.xml failed"
   exit 1
fi
echo "$OSCARS_DIST/coordinator/config/pce-configuration-ssl-template.xml -> pce-configuration-ssl.xml"
cp $OSCARS_DIST/coordinator/config/pce-configuration-ssl-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-ssl.xml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/coordinator/config/pce-configuration-ssl-template.xml $OSCARS_DIST/coordinator/config/pce-configuration-ssl.xml failed"
   exit 1
fi
echo "$OSCARS_DIST/topoBridge/config/config.HTTP.yaml.template -> config.HTTP.yaml"
cp $OSCARS_DIST/topoBridge/config/config.HTTP.yaml.template $OSCARS_DIST/topoBridge/config/config.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/topoBridge/config/config.HTTP.yaml.template $OSCARS_DIST/topoBridge/config/config.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/topoBridge/config/config.SSL.yaml.template -> config.SSL.yaml"
cp $OSCARS_DIST/topoBridge/config/config.SSL.yaml.template $OSCARS_DIST/topoBridge/config/config.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/topoBridge/config/config.SSL.yaml.template $OSCARS_DIST/topoBridge/config/config.SSL.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/resourceManager/config/config.HTTP.yaml.template -> config.HTTP.yaml"
cp $OSCARS_DIST/resourceManager/config/config.HTTP.yaml.template $OSCARS_DIST/resourceManager/config/config.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.HTTP.yaml.template $OSCARS_DIST/resourceManager/config/config.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/resourceManager/config/config.SSL.yaml.template -> config.SSL.yaml"
cp $OSCARS_DIST/resourceManager/config/config.SSL.yaml.template $OSCARS_DIST/resourceManager/config/config.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.SSL.yaml.template $OSCARS_DIST/resourceManager/config/config.SSL.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/resourceManager/config/config.TESTING.yaml.template -> config.TESTING.yaml"
cp $OSCARS_DIST/resourceManager/config/config.TESTING.yaml.template $OSCARS_DIST/resourceManager/config/config.TESTING.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/resourceManager/config/config.TESTING.yaml.template $OSCARS_DIST/resourceManager/config/config.TESTING.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/wbui/config/config.HTTP.yaml.template -> config.HTTP.yaml"
cp $OSCARS_DIST/wbui/config/config.HTTP.yaml.template $OSCARS_DIST/wbui/config/config.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/wbui/config/config.HTTP.yaml.template $OSCARS_DIST/wbui/config/config.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/wbui/config/config.SSL.yaml.template -> config.SSL.yaml"
cp $OSCARS_DIST/wbui/config/config.SSL.yaml.template $OSCARS_DIST/wbui/config/config.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/wbui/config/config.SSL.yaml.template $OSCARS_DIST/wbui/config/config.SSL.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/ionui/config/config.HTTP.yaml.template -> config.HTTP.yaml"
cp $OSCARS_DIST/ionui/config/config.HTTP.yaml.template $OSCARS_DIST/ionui/config/config.HTTP.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/ionui/config/config.HTTP.yaml.template $OSCARS_DIST/ionui/config/config.HTTP.yaml failed"
   exit 1
fi
echo "$OSCARS_DIST/ionui/config/config.SSL.yaml.template -> config.SSL.yaml"
cp $OSCARS_DIST/ionui/config/config.SSL.yaml.template $OSCARS_DIST/ionui/config/config.SSL.yaml
if [ $? -eq 1 ]; then
   echo "cp $OSCARS_DIST/ionui/config/config.SSL.yaml.template $OSCARS_DIST/ionui/config/config.SSL.yaml failed"
   exit 1
fi

echo
echo "edit the config files in authN, authZ,resourceManager and ionui so that the oscars user and password matches the one in the installed mysql database"
echo "--Alternatively: cd tools; bin/idc-dbpassmod"
echo "edit the pce-configuration files in coordinator if you wish to run different PCEs"
echo "edit the config in topologyBridge to reflect your topology files."
echo "--Alternatively: see tools/bin/idc-toposerveradd, tools/bin/idc-domainXXX for your needs"
echo "edit the config file in wbui if you wish to use secure cookies or rename the cookies"
echo "then run  $OSCARS_DIST/bin/exportconfig"
exit 0;
