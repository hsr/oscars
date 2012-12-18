#!/usr/bin/perl
package Lib::XLTests;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;
use Params::Validate qw(:all);

use fields qw(NAME);


my $NAME = "XLTest";

my $tester = new Lib::Tester;



sub run_set
{
	my $self = shift;
	my %params = validate(@_, {srcVlan => 1, dstVlan => 1});

	__test_1($params{'srcVlan'}, $params{'dstVlan'});	
	__test_2($params{'srcVlan'}, $params{'dstVlan'});	
	__test_3($params{'srcVlan'}, $params{'dstVlan'});	
	__test_4($params{'srcVlan'}, $params{'dstVlan'});	
	__test_5($params{'srcVlan'}, $params{'dstVlan'});	
	__test_6($params{'srcVlan'}, $params{'dstVlan'});	
	__test_7($params{'srcVlan'}, $params{'dstVlan'});	
	__test_8($params{'srcVlan'}, $params{'dstVlan'});	
	__test_9($params{'srcVlan'}, $params{'dstVlan'});	
}



sub __test_1 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_1",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node1:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node1:ge-1/1/2:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_2 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_2",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node1:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node2:ge-1/1/1:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_3 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_3",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node1:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node3:ge-1/1/3:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_4 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_4",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node2:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node1:ge-1/1/2:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_5 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_5",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node2:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node2:ge-1/1/2:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_6 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_6",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node2:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node3:ge-1/1/2:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_7 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_7",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node3:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node1:ge-1/1/1:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_8 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_8",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node3:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node2:ge-1/1/2:*",
			dstVlan => $dstVlan,
			expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub __test_9 
{
	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my $srcVlan = shift;
	my $dstVlan = shift;

	my %testParams = (
			testName => $NAME . "_test_9",
			topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-1-1.net.xml",
			src => "urn:ogf:network:domain=testdomain-1-1.net:node=node3:port=ge-1/1/1:link=*",
			srcVlan => $srcVlan,
			dst => "urn:ogf:network:testdomain-1-1.net:node3:ge-1/1/2:*",
			dstVlan => $dstVlan,
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

