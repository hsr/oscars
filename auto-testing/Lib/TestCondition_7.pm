#!/usr/bin/perl
package Lib::TestCondition_7;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);

my $NAME = "TestCondition_7";


=head1

Topology Condition (7)
Two peering domains with v0.6+EoMPLS network setting+eomplsPSS and v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut

my $tester = new Lib::Tester;

# Seconds to sleep between checks for state changes
our $SLEEP = 15;

# Number of times to check for state changes
our $COUNT = 4;

# Start time can be 'no' or a number of minutes in the future.
our $STARTTIME = 'now';

# End time is reservation duration.
our $ENDTIME = '+00:00:03';


sub single_test_7_1
{
	# Test Scenario (7.1)
	# specific_vlan_tag-to-specific_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-9:link=link-1", 
		srcVlan => "18",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-9:link=link-1",
		dstVlan => "18",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	
	my $result = $tester->create(%testParams);

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-6:link=link-1",
		srcVlan => "3100",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-6:link=link-1",
		dstVlan => "3100",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);
	
	my $result = $tester->create(%testParams);
	$result;
}


sub single_test_7_2
{
	# Test Scenario (7.2)
	# any_vlan_tag-to-any_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	my %testParams = (
		testName => $NAME . "_scenario_2",
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
	my $result = $tester->create(%testParams);

	my %testParams = (
		testName => $NAME . "_scenario_2",
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

	my $result = $tester->create(%testParams);
	$result;
}




sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_7";
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

