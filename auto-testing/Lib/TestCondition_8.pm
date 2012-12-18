#!/usr/bin/perl
package Lib::TestCondition_8;
use Carp;
use Lib::Tester;
use Lib::TopologyUtils;
use Lib::Simulator;

use fields qw(NAME);


my $NAME = "TestCondition_8";


=head1

Topology Condition (8)
Three peering domains in linear topology, two configured with EoMPLS network setting+eomplsPSS and on with v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut


my $tester = new Lib::Tester;

our $COUNT = 0;
our $DURATION = 4;
our $INTERVAL = 90;


sub multi_test_8_1
{
	# Test Scenario (8.1)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : continue-random-execution-and-monito

	$COUNT = 15;
	_do_test("_multi_test_8_1", $COUNT);
}


sub multi_test_8_2
{
	# Test Scenario (8.2)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : :schedule-for-ten-thousand-circuits : continue-random-execution-and-monitor  

	$COUNT = 100;
	_do_test("_multi_test_8_2", $COUNT);
}


sub _do_test
{
	my $t_name = shift;
	my $iterations = shift;

	my $topologyFile = "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml";
	my $parser = new Lib::TopologyUtils;
	my @topology = $parser->parseV6(fileName => $topologyFile);
	my $n = @topology;
	my $c = 0;
	my $src; 
	my $dst; 
	my %pSrc;
	my %pDst;
	my $endMins;
	my $endTime;

	srand(1000);

	while ($c < $iterations) {
		my $r = int(rand($n));		
		$src = $topology[$r]->{'linkId'};
		%pSrc = $parser->parse_urn(urn => $src); 

		$r = int(rand($n));		
		$dst = $topology[$r]->{'remoteLinkId'};
		%pDst = $parser->parse_urn(urn => $dst);
	
		next if ($src eq $dst);
		#next if ($pSrc{'domain'} ne $pDst{'domain'});
		next if ($pSrc{'node'} eq '*');
		next if ($pDst{'node'} eq '*');

		next if ($pDst{'domain'} eq 'testdomain-1.net');
		next if ($pDst{'domain'} eq 'testdomain-3.net');
		next if (($c % 3 == 0) && ($pDst{'domain'} ne 'testdomain-4.net'));

		#$endMins = 10 + int(rand(59 - 10));
		$endMins = 4 + int(rand($DURATION));
		$endTime = "+00:00:" . $endMins;

	    my %testParams = (
        	testName => $NAME . $t_name,
	        topology => $topologyFile, 
   	    	src => $src,
        	srcVlan => 'any', 
        	dst => $dst,
        	dstVlan => 'any',
			endTime => $endTime,	
        	expectedResult => "ACTIVE"
   		);
		#print map { "$_ => $testParams{$_}\n" } sort keys %testParams;
		#print "\n";

    	$tester->create(%testParams);
		$c++;
		if ($c != $iterations) {
			#my $sleepTime = 1 + int(rand(2 * 45));
			my $sleepTime = 1 + int(rand($INTERVAL));
			print "sleeping ...\n";
			sleep($sleepTime);
		}
	}
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_8";
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
