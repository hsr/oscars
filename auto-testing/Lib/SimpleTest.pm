#!/usr/bin/perl
package Lib::SimpleTest;
use Carp;
use Lib::Tester;

use fields qw(NAME);


my $NAME = "SimpleTest";

my $tester = new Lib::Tester;


sub test_versions
{
	my %testParams = (
			testName => $NAME . "_test_local",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1.net.xml",
			src => "",
			srcVlan => "any",
			dst => "",
			dstVlan => "any",
			sleep => "15",
			count => "10", 
			startTime => "now",
			endTime => "+00:00:02",
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}

sub test_transitive
{
	my %testParams = (
			testName => $NAME . "_test_transitive",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1.net.xml",
			src => "",
			srcVlan => "any",
			dst => "",
			dstVlan => "any",
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}

sub test_local 
{
	my %testParams = (
			testName => $NAME . "_test_local",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-2:link=link-1",
			srcVlan => "any",
			dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-2:link=link-1",
			dstVlan => "any",
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub test_remote
{
	my %testParams = (
			testName => $NAME . "_test_remote",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-2:link=link-1",
			srcVlan => "any",
			dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-2:link=link-1",
			dstVlan => "any",
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub multi_test
{
	my @arr;

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams_0 = (
			testName => $NAME . "_multi_test",
			topology => "",
			src => "",
			srcVlan => "",
			dst => "",
			dstVlan => "",
			expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
			testName => $NAME . "_multi_test",
			topology => "",
			src => "",
			srcVlan => "",
			dst => "",
			dstVlan => "",
			expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;


	my $result = $tester->multi_test(@arr);
	$result;
}



sub topology_test
{
	my $topology = "";

	my $path = "/usr/local/oscars/TopoBridgeService/conf/";
 	$topology = $path . $topology;

	print "$NAME: Using $topology\n";
	$tester->topology_test(testName => $NAME, topology => $topology, expectedResult => 'CANCELLED');
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

