#!/usr/bin/perl
use Carp;
use Log::Log4perl;
use Lib::SimpleTest;
use Time::localtime;
use strict;
use warnings;


# This file controls test execution.
# Enable/disable tests with comment '#'.


my $simpletest = new Lib::SimpleTest;


## BEGIN TESTS

print "OSCARS V0.6 Automated Testing\n";
my $nowstr = ctime(); 
print "$nowstr\nRunning...\n";


run_simpletests();


$nowstr = ctime();
print "$nowstr\nDone\n";

## END TESTS



sub run_simpletests
{
	print "testing local reservation\n";
	$simpletest->test_local();

	print "testing remote reservation\n";
	$simpletest->test_remote();

	#print "testing multi hop reservation\n";
	#$simpletest->test_transitive();

	#print "testing intra version\n";
	#$simpletest->test_versions();
}

