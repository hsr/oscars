#!/usr/bin/perl
package Lib::TestCondition_6;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "TestCondition_6";


=head1

Topology Condition (6)
Two peering domains with v0.6+EoMPLS network setting+eomplsPSS and v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut


my $tester = new Lib::Tester;

# Seconds to sleep between checks for state changes
our $SLEEP = 15;

# Number of times to check for state changes
our $COUNT = 4;

# Start time can be 'now' or a number of minutes in the future.
our $STARTTIME = 'now';

# End time is reservation duration.
our $ENDTIME = '+00:00:03';


# This test must be run manually from testdomain-4.net to testdomain-2.net
# urn:ogf:network:domain=testdomain-4.net:node=node-2:port=port-10:link=link-1
# urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-9:link=link-1
#
# urn:ogf:network:domain=testdomain-4.net:node=node-1:port=port-1:link=link-1
# urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-1:link=link-1
sub single_test_6_1
{
	# Test Scenario (6.1)
	# specific_vlan_tag-to-specific_vlan_tag : v0.5-api-client-at-v0.6-domain : serial-execution : translation and no-translation : inter-domain path

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "",
		srcVlan => "1000",
		dst => "", 
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	$tester->multi_create(@arr);
}


# This test must be run manually from testdomain-4.net to testdomain-2.net
# urn:ogf:network:domain=testdomain-4.net:node=node-2:port=port-10:link=link-1
# urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-9:link=link-1
#
# urn:ogf:network:domain=testdomain-4.net:node=node-1:port=port-1:link=link-1
# urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-1:link=link-1
sub single_test_6_2
{
	# Test Scenario (6.2)
	# any_vlan_tag-to-any_vlan_tag : v0.5-api-client-at-v0.6-domain : serial-execution : translation and no-translation : inter-domain path 

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_2",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "", 
		srcVlan => "any",
		dst => "", 
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	$tester->multi_create(@arr);
}



sub single_test_6_3
{
	# Test Scenario (6.3)
	# specific_vlan_tag-to-specific_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-2:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-2:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-3:link=link-1",
		srcVlan => "1100",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-3:link=link-1", 
		dstVlan => "1100",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

	my %testParams_2 = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-4:link=link-1", 
		srcVlan => "900",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-4:link=link-1",
		dstVlan => "900",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_2;

	my %testParams_3 = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-5:link=link-1",
		srcVlan => "1030",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-5:link=link-1", 
		dstVlan => "1030",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_3;

	$tester->multi_create(@arr);
}


sub single_test_6_4
{
	# Test Scenario (6.4)
	# any_vlan_tag-to-any_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-6:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-6:link=link-1",
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-7:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-7:link=link-1",
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

	my %testParams_2 = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-8:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-8:link=link-1",
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_2;

	my %testParams_3 = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-9:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-9:link=link-1",
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_3;

	$tester->multi_create(@arr);
}




sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;

	$tester->multi_create(@arr);
}




sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;

	$tester->multi_create(@arr);
}




sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
	$tester->multi_create(@arr);
}




sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_6";
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


