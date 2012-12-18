#!/bin/sh 
#
# oscarsdb: apply an action to the OSCARS databases AuthN, AUthZ, RM.
#
# syntax:
#	oscarsdb.sh <root sql> <action>
#
# actions are:
#
#  createTables        : create all databases and tables
#  removeTable         : drops all databases and tables
#  populateDefaults    : creates the initial entries in tables
#  upgradeTables0.5-0.6: upgrade tables from 0.5 to 0.6
#
# The following shell environment variables must be defined.
#
# OSCARS_HOME: deployment directory
# OSCARS_DIST: OSCARS distribution directory
#
SH=/bin/sh
SQLROOT="root"

if [ ! -z `which mysql` ]; then
    SQL=mysql
elif [ ! -z `which mysql5` ]; then
    SQL=mysql5
else
    echo mysql not found on path
    echo "Please refer to GETTINGSTARTED in $OSCARS_DIST"
    exit -1
fi

case $# in 
  0)
   echo "usage oscarsdb <cmd> where cmd is one of"
   echo "\t init - creates the oscars user"
   echo "\t ct - creates default tables"
   echo "\t rt - remove all tables"
   exit;;
esac

echo "input password for mysql root user: "
read -s passwd
case "$1" in
   init) 
       $SQL -u $SQLROOT -p${passwd}  < $OSCARS_DIST/bin/initOscars.sql ;;
   cempt)
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authN/sql/createTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authZ/sql/createTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/resourceManager/sql/createTables.sql
       #ion. Does not fill in the oscars related tables
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/ion-war/sql/createTables.sql;;
   ct)
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authN/sql/createTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authN/sql/populateDefaults.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authZ/sql/createTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authZ/sql/populateDefaults.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/resourceManager/sql/createTables.sql
       #ion tables
       $OSCARS_DIST/bin/ioncreatedb.sh $SQL $SQLROOT $passwd;;
   rt)
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authN/sql/removeTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/authZ/sql/removeTables.sql
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/resourceManager/sql/removeTables.sql
       #ion tables
       $SQL -u $SQLROOT -p$passwd < $OSCARS_DIST/ion-war/sql/removeTables.sql;;
esac
