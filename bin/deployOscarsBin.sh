#!/bin/sh
# This script will check out the latest version of the OSCARS 0.6 src tree into $OSCARS_DIST
# Create and initialize the authN, authZ and rm sql databases
# Copy template configuration files to non-template versions
# Install the configuration files to $OSCARS_HOME

if [ ! -d $OSCARS_DIST ]; then
        echo "Please set OSCARS_DIST to the directory that contains the oscars sources"
        exit 1;
fi
if [ -z $OSCARS_HOME ]; then
        echo "Please set OSCARS_HOME to the OSCARS deployment directory"
        exit 1;
fi
if [ ! -d $OSCARS_HOME ]; then
        echo "Creating $OSCARS_HOME";
        mkdir -p $OSCARS_HOME;
fi
cd $OSCARS_DIST

SQLRoot=root
SH=sh

if [ ! -z `which mysql` ]; then
    SQL=mysql
elif [ ! -z `which mysql5` ]; then
    SQL=mysql5
else
    echo mysql not found on path
    echo "Please refer to GETTINGSTARTED in $OSCARS_DIST"
    exit -1
fi
echo using $SQL

if [ -z `which svn` ]; then
    echo svn not found on path
    echo "Please refer to GETTINGSTARTED in $OSCARS_DIST"
    exit -1
fi

MYSQL_VERSION=$(echo $($SQL --version) | awk '$1~/mysql/{print substr($5,1,1)}')
if [ $MYSQL_VERSION -ge 5 ]; then
   echo "Installed Mysql is appropriate for OSCARS"
else 
   echo "OPTIONAL: Existing Mysql is not version 5 or above. Please consider Installing Mysql 5"
fi

ans=n
echo "do you want to create the mysql tables? [y|n] "
read ans
if [ $ans == "y" ]; then
echo "Creating mysql tables for OSCARS"
echo "Please enter Mysql root password"
read -s passwd

if [ -z $passwd ]; then
    echo "WARNING: Mysql root password is empty. It is not secure to leave the password empty"
    echo "Please set the Mysql root password using the following command"
    echo "/usr/bin/mysqladmin -u root password 'new-password'"
fi

echo Creating mysql tables
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/bin/initOscars.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authN/sql/createTables.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authN/sql/populateDefaults.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authZ/sql/createTables.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/authZ/sql/populateDefaults.sql
$SQL -u $SQLRoot -p$passwd < $OSCARS_DIST/resourceManager/sql/createTables.sql
#ion tables
$OSCARS_DIST/bin/ioncreatedb.sh $SQL $SQLRoot $passwd
fi

ans="n"
echo "do you want to edit the mysql oscars password or change any of the service ports? [y|n] "
read ans
if [ $ans == "y" ]; then
    echo "when you are done editing, run $OSCARS_DIST/bin/exportconfig"
    echo exiting
    exit 0
fi

$SH $OSCARS_DIST/bin/exportconfig
exit 0
