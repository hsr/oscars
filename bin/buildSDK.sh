#!/bin/sh
cd $OSCARS_DIST
rm SDK.tar
tar -cf SDK.tar --files-from $OSCARS_DIST/bin/incFile --exclude-from $OSCARS_DIST/bin/excFile
tar -rf SDK.tar pce/target/classes 
