#!/usr/bin/perl
package Lib::TestCondition_4;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);

my $NAME = "TestCondition_4";


=head1

Topology Condition (4)
Single domain with EoMPLS network setting 
Bottleneck link in path with very small bandwidth and very limited number of VLANs;
L2SC edge links with VLAN translation enabled and PSC trunk links; 

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


# Add reservations as needed
sub multi_test_4_1
{
	# Test Scenario (4.1)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : mixed translation and no-translation

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-8:link=link-1",
		srcVlan => "3",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-8:link=link-1",
		dstVlan => "3",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "5",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-9:link=link-1",
		dstVlan => "5",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

	my %testParams_2 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-8:link=link-1",
		srcVlan => "7",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-8:link=link-1",
		dstVlan => "8",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "+00:00:05",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_2;

	my %testParams_3 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-9:link=link-1",
		srcVlan => "9",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		dstVlan => "9",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "+00:00:05",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_3;

	$tester->multi_create(@arr);
}


# Add reservations as needed
sub multi_test_4_2
{
	# Test Scenario (4.2)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : no-translation

    my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-12:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-13:link=link-1",
		dstVlan => "any",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-5:link=link-1",
		srcVlan => "12",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-5:link=link-1",
		dstVlan => "12",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

	my %testParams_2 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-7:link=link-1", 
		srcVlan => "15",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-7:link=link-1", 
		dstVlan => "15",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_2;

	my %testParams_3 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-5:link=link-1",
		srcVlan => "16",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-5:link=link-1",
		dstVlan => "16",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_3;

	$tester->multi_create(@arr);
}


# Add reservations as needed
sub multi_test_4_3
{
	# Test Scenario (4.3)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : no-translation : single-node-path with one end on the bottleneck link

    my @arr;

    my %testParams_0 = (
        testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "10",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "10",
		bandwidth => "10",
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
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "15",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "15",
		bandwidth => "10",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "+00:00:06",
        expectedResult => "CANCELLED"
    );
    push @arr, \%testParams_1;

	$tester->multi_create(@arr);
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_4";
    bless($self);
    return $self;
}


sub name 
{
    my $self = shift;
    
    if ($Demo) {
        carp "Demo $self->{NAME}";
    }
    
    if (@_) { $self->{NAME} = shift }
    return $self->{NAME};
}

1;

