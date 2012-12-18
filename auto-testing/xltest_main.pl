#!/usr/bin/perl
use Carp;
use Lib::XLTests;
use Log::Log4perl;
use Time::localtime;
use strict;
use warnings;


# This file executes the tests. 


# Logging is done in Lib::OscarsClientUtils.createCancelLoop()
# logs/oscars_test.log records each tests parameters and end result.
# logs/<testname>.log contains detailed error messages for tests that fail.


my $xltest = new Lib::XLTests;


my $nowstr = ctime(); 
print "$nowstr\nRunning...\n";

$xltest->run_set(srcVlan => 'any', dstVlan => 'any');
$xltest->run_set(srcVlan => '2500', dstVlan => '2500');
$xltest->run_set(srcVlan => '2500', dstVlan => 'any');
$xltest->run_set(srcVlan => 'any', dstVlan => '2500');
$xltest->run_set(srcVlan => '2500', dstVlan => '2501');

$nowstr = ctime(); 
print "$nowstr\nDone\n";

