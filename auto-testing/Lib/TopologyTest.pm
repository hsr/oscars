#!/usr/bin/perl
package Lib::TopologyTest;
use Carp;
use Lib::Tester;

use fields qw(NAME);


my $NAME = "TopologyTest";

my $tester = new Lib::Tester;



sub topology_test
{
	my $topology = "";

	my $hostname = `hostname -f`;
	chomp($hostname);
	if ($hostname eq "odev-vm-16.es.net") {
		$topology = "testdomain-1.net.xml";
	} elsif ($hostname eq "odev-vm-17.es.net") {
		$topology = "testdomain-2.net.xml";
	} elsif ($hostname eq "odev-vm-18.es.net") {
		$topology = "testdomain-3.net.xml";
	} else {
		carp ("$NAME: unknown host!");
		return;
	}

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

