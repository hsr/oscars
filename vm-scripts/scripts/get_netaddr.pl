#!/usr/bin/perl

my @addrarr = split(/\./,$ARGV[1]);
my $ipaddress = unpack("N", pack("C4",@addrarr));
my @maskarr=split(/\./,$ARGV[2]);
my $netmask = unpack("N", pack("C4",@maskarr));
my $netadd = ($ipaddress & $netmask);
my @netarr=unpack("C4", pack("N",$netadd ));
my $NETADDR=join(".",@netarr);
my $bcast = ($ipaddress & $netmask ) + (~ $netmask);
my @bcastarr = unpack("C4", pack( "N",$bcast)) ;
my $BROADCAST = join(".",@bcastarr);

if ($ARGV[0] eq "broadcast") {
    print $BROADCAST;
} elsif ($ARGV[0] eq "network") {
    print $NETADDR;
} elsif ($ARGV[0] eq "randmac") {
    my $dir=`pwd`;
    chop($dir);
    my $line = `$dir/scripts/easymac.sh -R |grep MAC`;
    $line =~ /MAC Address: ([^\s]*)/;
    print $1;
} elsif ($ARGV[0] eq "hwaddr") {
    my $line = `grep mac /etc/xen/$ARGV[1]`;
    $line =~ /mac=\s*([^,\s"]*)/;
    print $1;
} 
