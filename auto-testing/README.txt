
OSCARS V6 perl testing


Quickstart:
cp -R $OSCARS_DIST/auto-testing/Lib $OSCARS_DIST/api
cp $OSCARS_DIST/simpletest_main.pl $OSCARS_DIST/api
mkdir $OSCARS_DIST/api/logs

cd $OSCARS_DIST/api
perl simpletest_main.pl



Description:

This suite of tests is designed to automate running of the OSCARS test scripts in $OSCARS_DIST/api/bin.


Installation:

The tests are written to be run from the $OSCARS_DIST/api directory.
Copy Lib and test_main.pl to $OSACRS_DIST/api, and create directory $OSCARS_DIST/api/logs, 
this is where the test logs and error logs will be stored.

You may need to install some perl modules if they are not already installed on your system.
You will need topology files to run your tests with. Some examples are in the resources directory.


Usage:

The tests are called from test_main.pl.

To run the test suite type 'perl test_main.pl'.

Enable/disable specific tests by removing/inserting # before test call in test_main.pl.

Logs are generated each test along with input parameters and test results in logs/oscars_tests.log.
Errors are gathered from the $OSCARS_HOME logs by GRI and stored in <testname>.log. 


Implementation:

Tests are defined in Lib/<testname>.pm


Creating new tests:

There are example tests for both unit style tests and tests that will process an entire topology file.

To create a new test copy SimpleTest.pm and set your parameters as needed.

