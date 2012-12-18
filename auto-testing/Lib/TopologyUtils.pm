package Lib::TopologyUtils;
use base qw(Class::Accessor);
use Carp;
use English qw(-no_match_vars);
use Log::Log4perl qw(get_logger);
use Params::Validate qw(:all);
use strict;
use warnings;
use XML::LibXML;


=head1 NAME

TopologyUtils
    
=head1 DESCRIPTION

This class contains tools for parsing OSCARS topology files.
The topology parsing functions return ann array of hashes that 
contain topology information. Each array element contains:
nodeId, linkId, remoteLinkId, vlanRange

=cut

our $Debugging = 0;

use fields qw(NAME);


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TopologyUtils";
    bless($self);
    return $self;
}


# Parse a V6 style topology file into an array. Pass in a topology file name.
# Each array element is a hash containing nodeId, linkId, remoteLinkId and vlanRange.
sub parseV6
{
	my $self = shift;
	my %args = validate(@_, {fileName => 1});
	my $parser = new XML::LibXML;
	my $doc = $parser->parse_file($args{'fileName'});
	my @ret;
	my @tmp;
	my $tag;
	my $i = 0;

	my @nodes = $doc->getElementsByTagName('CtrlPlane:node');
	foreach my $node (@nodes) {
		my $nodeId = $node->getAttribute('id');
		my @ports = $node->getElementsByTagName('CtrlPlane:port');
		foreach my $port (@ports) {
			my @links = $port->getElementsByTagName('CtrlPlane:link');
			my $link = $links[0];
			my $linkId = $link->getAttribute('id');
			my @remoteLinks = $link->getElementsByTagName('CtrlPlane:remoteLinkId');
			my $remoteLink = $remoteLinks[0];
			my $remoteLinkId = $remoteLink->textContent;
			@tmp = $link->getChildrenByTagName('CtrlPlane:SwitchingCapabilityDescriptors');
			my $scd = $tmp[0];
			$tag = $scd->textContent;
			@tmp = $scd->getChildrenByTagName('CtrlPlane:switchingCapabilitySpecificInfo');
			my $scs = $tmp[0];
			$tag = $scs->textContent;
			@tmp = $scs->getChildrenByTagName('CtrlPlane:vlanRangeAvailability');
			my $vlan = $tmp[0];
			my $vlanRange = $vlan->textContent;

			# Returned data structure is designed for easy access to 
			# linkId/remoteLinkId rather than accessing through nodeId.
			my %tmp = (
				'nodeId' => $nodeId,
				'linkId' => $linkId,
				'remoteLinkId' => $remoteLinkId,
				'vlanRange' => $vlanRange
			);
			$ret[$i++] = \%tmp;
		}
	}
	return @ret;
}


# Parse a V5.3 style topology file into an array. Pass in a topology file name.
# Each array element is a hash containing nodeId, linkId, remoteLinkId and vlanRange.
sub parseV53
{
	my $self = shift;
	my %args = validate(@_, {filename => 1});
	my $parser = new XML::LibXML;
	my $doc = $parser->parse_file($args{'filename'});
	my @ret;
	my @tmp;
	my $tag;
	my $i = 0;

	my @nodes = $doc->getElementsByTagName('node');
	foreach my $node (@nodes) {
		my $nodeId = $node->getAttribute('id');
		my @ports = $node->getElementsByTagName('port');
		foreach my $port (@ports) {
			my @links = $port->getElementsByTagName('link');
			my $link = $links[0];
			my $linkId = $link->getAttribute('id');
			my @remoteLinks = $link->getElementsByTagName('remoteLinkId');
			my $remoteLink = $remoteLinks[0];
			my $remoteLinkId = $remoteLink->textContent;
			@tmp = $link->getChildrenByTagName('SwitchingCapabilityDescriptors');
			my $scd = $tmp[0];
			$tag = $scd->textContent;
			@tmp = $scd->getChildrenByTagName('switchingCapabilitySpecificInfo');
			my $scs = $tmp[0];
			$tag = $scs->textContent;
			@tmp = $scs->getChildrenByTagName('vlanRangeAvailability');
			my $vlan = $tmp[0];
			my $vlanRange = $vlan->textContent;

			# Returned data structure is designed for easy access to 
			# linkId/remoteLinkId rather than accessing through nodeId.
			my %tmp = (
				'nodeId' => $nodeId,
				'linkId' => $linkId,
				'remoteLinkId' => $remoteLinkId,
				'vlanRange' => $vlanRange
			);
			$ret[$i++] = \%tmp;
		}
	}
	return @ret;
}


# Parse a urn into hash for easy urn comparison. Hash contains urn fields. 
# Hash keys are: urn, ogf, network, domain, node, port and link.
sub parse_urn
{
	my $self = shift;
	my %args = validate(@_, {urn => 1});
	my $urn = $args{'urn'};
	my @tmp = split(/:/, $urn);
	my %hsh = ();

	if ($tmp[0] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[0]);
		$hsh{'urn'} = $bar;
	} else {
		$hsh{'urn'} = $tmp[0];
	}	
	if ($tmp[1] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[1]);
		$hsh{'ogf'} = $bar;
	} else {
		$hsh{'ogf'} = $tmp[1];
	}
	if ($tmp[2] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[2]);
		$hsh{'network'} = $bar;
	} else {
		$hsh{'network'} = $tmp[2];
	}
	if ($tmp[3] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[3]);
		$hsh{'domain'} = $bar;
	} else {
		$hsh{'domain'} = $tmp[3];
	}
	if ($tmp[4] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[4]);
		$hsh{'node'} = $bar;
	} else {
		$hsh{'node'} = $tmp[4];
	}
	if ($tmp[5] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[5]);
		$hsh{'port'} = $bar;
	} else {
		$hsh{'port'} = $tmp[5];
	}
	if ($tmp[6] =~ m/=/) {
		my ($foo, $bar) = split(/=/, $tmp[6]);
		$hsh{'link'} = $bar;
	} else {
		$hsh{'link'} = $tmp[6];
	}

	return %hsh;
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

