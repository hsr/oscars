#!/usr/bin/perl
package Lib::MiscTests;
use Carp;
use Lib::Tester;
use Params::Validate qw(:all);
#use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "MiscTests";

my $tester = new Lib::Tester;


sub stress_test 
{
	my $self = shift;
	my %args = validate(@_, {sleep => {default => 2}, count => {default => 4}, start => {default => "now"}, end => {default => "+00:00:02"}});

	my @arr;

	my $sleep = $args{'sleep'};
	my $count = $args{'count'}; 
	my $start = $args{'start'}; 
	my $end = $args{'end'};
	my $result = "ACTIVE";

	my %testParams_0 = (
		testName => $NAME . "_stress_test",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-2:link=link-1",
		srcVlan => "3",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-2:link=link-1",
		dstVlan => "3",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-3:link=link-1",
		srcVlan => "5",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-3:link=link-1",
		dstVlan => "5",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_1;

	my %testParams_2 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-4:link=link-1",
		srcVlan => "4",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-4:link=link-1",
		dstVlan => "4",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_2;

	my %testParams_3 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-5:link=link-1",
		srcVlan => "6",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-5:link=link-1",
		dstVlan => "6",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_3;

	my %testParams_4 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-6:link=link-1",
		srcVlan => "3007",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-6:link=link-1",
		dstVlan => "3007",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_4;

	my %testParams_5 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-7:link=link-1",
		srcVlan => "3007",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-7:link=link-1",
		dstVlan => "3007",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_5;

	my %testParams_6 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-10:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "any",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_6;

	my %testParams_7 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-12:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-13:link=link-1",
		dstVlan => "any",
		sleep => $sleep,
		count => $count,
		startTime => $start,
		endTime => $end,
		expectedResult => "$result"
	);
	push @arr, \%testParams_7;

=head1
	my %testParams_8 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-2:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-2:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_8;

	my %testParams_9 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-3:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-3:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_9;

	my %testParams_10 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-4:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-4:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_10;

	my %testParams_11 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-5:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-5:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_11;

	my %testParams_12 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-6:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-6:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_12;

	my %testParams_13 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-7:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-7:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_13;

	my %testParams_14 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-2:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-2:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_14;

	my %testParams_15 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-3:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-3:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_15;

	my %testParams_16 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-4:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-4:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_16;

	my %testParams_17 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-5:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-5:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_17;

	my %testParams_18 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-6:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-6:link=link-1",
		dstVlan => "any",
		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_18;

	my %testParams_19 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-7:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-7:link=link-1",
		dstVlan => "any",

		sleep => "10",
		count => "10",
		expectedResult => "ACTIVE"
	);
	push @arr, \%testParams_19;
=cut

	$tester->multi_create(@arr);
}


sub single_test_long
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
			testName => "$NAME: single_test_long",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1.xml",
			src => "urn:ogf:network:domain=testdomain-1.net:node=node-1-1:port=ge-1/1/0:link=*",
			srcVlan => "any",
			dst => "urn:ogf:network:testdomain-1.net:node-1-2:ge-1/1/0:*",
			dstVlan => "any",
			startTime => "now",
			endTime => "+00:45:00",
			expectedResult => "CANCELLED",
			count => "50"	
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    bless($self);
    return $self;
}


sub name 
{
    my $self = shift;
    
    if ($Debugging) {
        carp "Debugging $self->{NAME}";
    }
    
    if (@_) { $self->{NAME} = shift }
    return $self->{NAME};
}

1;
