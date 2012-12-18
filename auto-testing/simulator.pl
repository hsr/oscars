#!/usr/bin/perl
use Carp;
use Log::Log4perl;
use Lib::Simulator;
use Time::localtime;
use strict;
use warnings;


# This file controls test execution.
# Enable/disable tests with comment '#'.


my $simulator = new Lib::Simulator;


## BEGIN TESTS

print "OSCARS V0.6 Automated Testing\n";
my $nowstr = ctime(); 
print "$nowstr\nRunning...\n";


run_simulators();


$nowstr = ctime();
print "$nowstr\nDone\n";

## END TESTS



sub run_simulators
{
	$simulator->create();
}

