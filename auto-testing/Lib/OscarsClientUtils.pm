package Lib::OscarsClientUtils;
use base qw(Class::Accessor);
use Carp;
use English qw(-no_match_vars);
use Fcntl qw(:flock);
use Log::Log4perl qw(get_logger);
use Params::Validate qw(:all);
use	POSIX qw(strftime); 
use Time::Format qw(time_format);
use strict;
use warnings;


=head1 NAME

OscarsClientUtils
    
=head1 DESCRIPTION

Lib::OscarsClientUtils contains tools for for automation of test scripts.

=cut

our $Debugging = 0;
#our $MODE = "";
our $MODE = "-C PRODUCTION";


use fields qw(NAME);

# Location of error debug logs.
our $OSCARS_logs = "/usr/local/oscars/logs/";
# Location of scripts.
our $BINPATH = "./bin";
our $COUNT = 30;
our $SLEEP = 60;


# Main log of test name, topology file, command arguments and test result.
my $log_conf = q(
	log4perl.category = INFO, Logfile
	log4perl.appender.Logfile = Log::Log4perl::Appender::File
	log4perl.appender.Logfile.filename = logs/oscars_tests.log
	log4perl.appender.Logfile.mode = append 
	log4perl.appender.Logfile.layout = Log::Log4perl::Layout::PatternLayout
	log4perl.appender.Logfile.layout.ConversionPattern = %d %p %m %n
);

Log::Log4perl::init(\$log_conf);
my $logger = Log::Log4perl::get_logger();


# Request a reservation only
sub reserve 
{
	my $self = shift;
	my %args = validate(@_, {yamlFile => 1, topology => 1, logFile => 1, testName => 1, 
				src => 1, dst => 1, sleep => {default => 15}, count => {default => 5}, result => 1});
	my $gri = "";
	my $status = "";

	if ($Debugging) {
		unlink $args{'yamlFile'};
		$status = "Debugging";
		return $status;
	}

	# Call createRes.sh
	$gri = __execCreate(yamlFile => $args{'yamlFile'});
	if ($gri eq '') {
		$status = "execCreate() failed\n";
		return $status;
	}
	print "GRI: $gri - ";
	print strftime('%T', localtime);
	print " - Made reservation\n";

	unlink($args{'yamlFile'});
	return $status;
}


# Execute createRes.sh, list.sh, cancelRes.sh and list.sh and log errors.
# This could use some refactoring.
sub createCancelRes
{
	my $self = shift;
	my %args = validate(@_, {yamlFile => 1, topology => 1, logFile => 1, testName => 1, 
				src => 1, dst => 1, sleep => {default => $SLEEP}, count => {default => $COUNT}, result => 1});
	my $gri = "";
	my $status = "";

	if ($Debugging) {
		unlink $args{'yamlFile'};
		$status = "Debugging";
		return $status;
	}

	# Call createRes.sh
	$gri = __execCreate(yamlFile => $args{'yamlFile'});
	if ($gri eq '') {
		$status = "execCreate() failed\n";
		return $status;
	}
	print "GRI: $gri - Creating reservation\n";

	# Check reservation status 
	my $count = 0;
	do {
		sleep($args{'sleep'});
		$status = __execList(gri => $gri);
		chomp($status);
		print "GRI: $gri - Status: $status\n";
		$count++;
	} while (($status ne 'ACTIVE') and ($status ne 'FINISHED') and ($status ne 'FAILED') and ($count < $args{'count'}));

	# Cancel reservation
	if ($status eq 'ACTIVE') { 
		$count = 0;
		do {
			print "GRI: $gri - Attempting to cancel reservation\n";
			__execCancel(gri => $gri);
			sleep($args{'sleep'});
			$status = __execList(gri => $gri);
			print "GRI: $gri - Status: $status\n";
			$count++;
		} while (($status ne 'CANCELLED') and ($status ne 'FINISHED') and ($status ne 'FAILED') and ($count < $args{'count'}));
	# Try to clean up timed out or 'stuck' reservation.
	} elsif (($status ne 'CANCELLED') and ($status ne 'FINISHED') and ($status ne 'FAILED')) {
		$count = 0;
		do {
			print "GRI: $gri - Attempting to clean up timed out reservation\n";
			__execCancel(gri => $gri);
			sleep($args{'sleep'});
			$status = __execList(gri => $gri);
			print "GRI: $gri - Status: $status\n";
			$count++;
		} while (($status ne 'CANCELLED') and ($status ne 'FINISHED') and ($status ne 'FAILED') and ($count < $args{'count'}));
		$status = "TIMEDOUT";
	}	 

	# Check result
	print "GRI: $gri - Expected status: $args{'result'} Status: ";
	if ($status eq $args{'result'}) {
		$status = "SUCCESS";
		print "$status\n";
	} else {
		$status = "FAIL";
		print "$status\n";
		# Record errors and debug output
		print "GRI: $gri - Logging errors\n";
		open FILE, ">>logs/$args{'logFile'}" or die $!;
		flock(FILE, LOCK_EX) or die "Cannot lock $args{'logFile'}: $!\n";
		print FILE "$args{'testName'}: $gri: $args{'src'} => $args{'dst'}\n";
		print FILE "Expected status: $args{'result'} Status: $status\n";
		my @output = `grep $gri $OSCARS_logs*`;
		foreach (@output) {
			print FILE "$_\n";
		}		
		print FILE "\n\n";
		flock(FILE, LOCK_UN);
		close FILE;
	}


	# Log test results
	$logger->info("$args{'testName'}: $args{'topology'} GRI: $gri Status: $status");
	open FILE, "<$args{'yamlFile'}" or $logger->info("unable to read $args{'yamlFile'}");
	while (<FILE>) {
		chomp;
		$logger->info($_);
	}	
	$logger->info("\n");
	close(FILE);

	unlink($args{'yamlFile'});
	return $status;
}


# Write test parameters to yaml file for test input. 
sub writeYaml 
{
	my $self = shift;
    my %args = validate(@_, {yamlFile => 1, testName => 1, src => 1, dst => 1, srcVlan => {default => 'any'}, 
			dstVlan => {default => 'any'}, layer => {default => 2}, bandwidth => {default => 100}, 
			startTime => {default => 'now'}, endTime => {default => '+00:00:12'}});
	my $status = "";
    my $str;
	
	if ($Debugging) {
		print "OscarsClientUtils->writeYaml() $args{'yamlFile'}: $args{'testName'}: $args{'src'} $args{'srcVlan'} => $args{'dst'} $args{'dstVlan'}\n";
	}

	my $start;
	if ($args{'startTime'} ne 'now') {
		$start = getStartTime($args{'startTime'});
	} else {
		$start = $args{'startTime'};
	}
		
    open FILE, ">$args{'yamlFile'}" or die $!;

    $str = "---\n";
    $str .= "create:\n";
    $str .= "    gri:    ''\n";
    $str .= "    login:    'client'\n";
    $str .= "    layer:    '$args{'layer'}'\n";
    $str .= "    src:    $args{'src'}\n";
    $str .= "    dst:    $args{'dst'}\n";
    $str .= "    bandwidth:    $args{'bandwidth'}\n"; 
    $str .= "    description:    'a timer-automatic reservation'\n";
    #$str .= "    start-time:    '$args{'startTime'}'\n";
    $str .= "    start-time:    '$start'\n";
    $str .= "    end-time:    '$args{'endTime'}'\n";
    $str .= "    path-setup-mode:    'timer-automatic'\n";
    $str .= "    srcvlan:    '$args{'srcVlan'}'\n";
    $str .= "    dstvlan:    '$args{'dstVlan'}'\n";
   
	#print $str; 
    print FILE $str;
    close FILE;
	return $status;
}


# Request reservation through createRes.sh.
# Returns gri of reservation.
sub __execCreate 
{
    my %args = validate(@_, {yamlFile => 1});
    my $gri = "";

	if ($Debugging) {
		print "__execCreate() $args{'yamlFile'}\n";
	}

    my @output = `$BINPATH/createRes.sh -pf $args{'yamlFile'} $MODE`;
    foreach my $line (@output) {
        if ($line =~ /gri=/) {
             my @tmp = split(/ /, $line);
            $gri = $tmp[3];
            chomp($gri);
        }
    }
    return $gri;
}


# Request cancel through cancelRes.sh.
sub __execCancel
{
    my %args = validate(@_, {gri => 1});
	my $status = "";

    my @output = `$BINPATH/cancelRes.sh -gri $args{'gri'} $MODE`;

	return $status;
}


# Request reservation list through list.sh.
sub __execList
{
    my %args = validate(@_, {gri => 1});
    my $status = "list.sh Failed";
	my $found = 0;

    my @output = `$BINPATH/list.sh -n 1 $MODE`;
    foreach my $line (@output) {
        if ($line =~ /$args{'gri'}/) {
			$found = 1;
		}
        if ($found and $line =~ /Status/) {
            my @tmp = split(/ /, $line);
            $status = $tmp[1];
            chomp($status);
			return $status;
        }
    }
    return $status;
}


# Create a reservation monitor it through one state transition
sub create
{
	my $self = shift;
	my %args = validate(@_, {yamlFile => 1, topology => 1, logFile => 1, testName => 1, 
				src => 1, dst => 1, sleep => {default => 15}, count => {default => 5}, result => 1});
	my $gri = "";
	my $status = "";

	if ($Debugging) {
		unlink $args{'yamlFile'};
		$status = "Debugging";
		return $status;
	}

	# Call createRes.sh
	$gri = __execCreate(yamlFile => $args{'yamlFile'});
	if ($gri eq '') {
		$status = "execCreate() failed\n";
		return $status;
	}
	#print "GRI: $gri - ";
	print "$gri - ";
	#print strftime('%T', localtime);
	#print " - Made reservation\n";
	print "$args{'src'} => $args{'dst'}\n";

	# Check reservation status 
	my $count = 0;
	do {
		sleep($args{'sleep'});
		$status = __execList(gri => $gri);
		chomp($status);
		print "GRI: $gri - ";
		print strftime('%T', localtime);
		print " - Status: $status\n";
		$count++;
	} while (($status ne 'RESERVED') and ($status ne 'ACTIVE') and ($status ne 'CANCELLED') and ($status ne 'FINISHED') and ($status ne 'FAILED') and ($count < $args{'count'}));

	# Log test results
	$logger->info("$args{'testName'}: $args{'topology'} GRI: $gri Status: $status");
	open FILE, "<$args{'yamlFile'}" or $logger->info("unable to read $args{'yamlFile'}");
	while (<FILE>) {
		chomp;
		$logger->info($_);
	}	
	$logger->info("\n");
	close(FILE);

	unlink($args{'yamlFile'});
	return $status;
}



sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "OscarsClientUtils";
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


sub getStartTime()
{
	my $mins = shift;
	
	my $t = time();
	my $starttime = $t + (60 * $mins);
	my $start = time_format('yyyy-mm-dd hh:mm', $starttime);
}


1;
