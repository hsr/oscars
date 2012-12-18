#!/usr/bin/perl
use Carp;
use Log::Log4perl;
use Lib::MiscTests;
use Params::Validate qw(:all);
use POSIX;
use strict;
use warnings;


# This file controls test execution.
# Enable/disable tests with comment '#'.


my $misctest = new Lib::MiscTests;


## BEGIN TESTS

print "OSCARS V0.6 Automated Testing\n";
print "Running...\n";


run_misctests();



## END TESTS



sub run_misctests
{
	my $iterations = 3;
	my $i;
	my $start;

	for ($i = 0; $i < $iterations; $i++) {
		$start = get_start(mins => 3 * ($i + 1));
		$misctest->stress_test(sleep => 2, count => 20, start => $start, end => "+00:00:02");
	}
}


sub get_start
{
	my %args = validate(@_, {mins => 1});

	my $mins = $args{'mins'};

	my $time = time();
	$time += 60 * $mins;
	my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime($time);
	$mon += 1;
	$year += 1900;
	my $start = sprintf("%02d-%02d-%02d %02d:%02d", $year,$mon,$mday,$hour,$min);
}

