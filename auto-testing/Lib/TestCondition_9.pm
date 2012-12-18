#!/usr/bin/perl
package Lib::TestCondition_9;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);



my $NAME = "TestCondition_9";



=head1

Topology Condition (9)
Three peering domains (1, 2 and 3) in linear topology, two configured with EoMPLS network setting+eomplsPSS and on with v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut



my $tester = new Lib::Tester;

# Seconds to sleep between checks for state changes
our $SLEEP = 30;

# Number of times to check for state changes
our $COUNT = 10;

# Start time can be 'no' or a number of minutes in the future.
#our $STARTTIME = 'now';
our $STARTTIME = 3;

# End time is reservation duration.
our $ENDTIME = '+00:00:10';


sub single_test_9_1
{
	# Test Scenario (9.1)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : crash-individual-module-during-active

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-3:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_9_2
{
	# Test Scenario (9.2)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : crash-individual-module-during-setup

	my %testParams = (
		testName => $NAME . "_scenario_2",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-3:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-3:link=link-1",
		dstVlan => "any",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_9_3
{
	# Test Scenario (9.3)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : restart-oscars-during-active

	my %testParams = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-3:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_9_4
{
	# Test Scenario (9.4)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : restart-oscars-during-setup 

	my %testParams = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-3:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_9_5
{
	# Test Scenario (9.5)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain path : restart-osacars-during-active

	my %testParams = (
		testName => $NAME . "_scenario_5",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-3:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_9_6
{
	# Test Scenario (9.6)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain path : restart-osacars-during-setup

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-3:link=link-1",
		dstVlan => "1000",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}



sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_9";
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

