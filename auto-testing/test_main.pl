#!/usr/bin/perl
use Carp;
use Log::Log4perl;
use Lib::TestCondition_1;
use Lib::TestCondition_2;
use Lib::TestCondition_3;
use Lib::TestCondition_4;
use Lib::TestCondition_5;
use Lib::TestCondition_6;
use Lib::TestCondition_7;
use Lib::TestCondition_8;
use Lib::TestCondition_9;
use Lib::TopologyTest;
use Lib::MiscTests;
use Time::localtime;
use strict;
use warnings;


# This file executes the tests. 


# Logging is done in Lib::OscarsClientUtils.createCancelLoop()
# logs/oscars_test.log records each tests parameters and end result.
# logs/<testname>.log contains detailed error messages for tests that fail.


my $testCondition_1 = new Lib::TestCondition_1;
my $testCondition_2 = new Lib::TestCondition_2;
my $testCondition_3 = new Lib::TestCondition_3;
my $testCondition_4 = new Lib::TestCondition_4;
my $testCondition_5 = new Lib::TestCondition_5;
my $testCondition_6 = new Lib::TestCondition_6;
my $testCondition_7 = new Lib::TestCondition_7;
my $testCondition_8 = new Lib::TestCondition_8;
my $testCondition_9 = new Lib::TestCondition_9;
my $topologyTest = new Lib::TopologyTest;
my $miscTests = new Lib::MiscTests;



## BEGIN TESTS

print "OSCARS V0.6 Automated Testing\n";
my $nowstr = ctime(); 
print "$nowstr\nRunning...\n";

run_cond_1();

run_cond_2();

run_cond_3();

#run_cond_4();

#run_cond_5();

#run_cond_6();

#run_cond_7();

# Requires 10,000 circuits
#run_cond_8();

# Requires system crash
#run_cond_9();

# Look for syntax errors in topology file
#$topologyTest->topology_test();

#run_misc_tests();


$nowstr = ctime();
print "$nowstr\nDone\n";

## END TESTS


sub run_misc_tests
{
	$miscTests->single_test_long();
}


sub run_cond_1
{
    # Topology Condition (1)
    # Single domain with EoMPLS network setting 
    # Common VLANs in path with sufficient bandwidth; 
    # L2SC edge links with VLAN translation enabled and PSC trunk links; 

    # Test Scenario (1.1)
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation
    $testCondition_1->single_test_1_1();

    # Test Scenario (1.2) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : translation (src_tag!=dst_tag) 
    $testCondition_1->single_test_1_2();

    # Test Scenario (1.3) 
    # any_vlan_tag-to-any_vlan_tag : serial-execution 
    $testCondition_1->single_test_1_3();

    # Test Scenario (1.4) 
    # specific_vlan_tag-to-any_vlan_tag : serial-execution 
    $testCondition_1->single_test_1_4();

    # Test Scenario (1.5) 
    # any_vlan_tag-to-specific_vlan_tag : serial-execution 
    $testCondition_1->single_test_1_5();

    # Test Scenario (1.6) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : single-node-path 
    $testCondition_1->single_test_1_6();

    # Test Scenario (1.7) 
    # any_vlan_tag-to-any_vlan_tag : serial-execution : single-node-path 
    $testCondition_1->single_test_1_7();
}



sub run_cond_2
{
    # Topology Condition (2)
    # Single domain with EoMPLS network setting 
    # No common VLANs in path with sufficient bandwidth; 
    # L2SC edge links with VLAN translation enabled and PSC trunk links; 

    # Test Scenario (2.1) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation 
    $testCondition_2->single_test_2_1();

    # Test Scenario (2.2) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : translation (src_tag!=dst_tag) 
    $testCondition_2->single_test_2_2();

    # Test Scenario (2.3) 
    # any_vlan_tag-to-any_vlan_tag : serial-execution 
    $testCondition_2->single_test_2_3();

    # Test Scenario (2.4) 
    # specific_vlan_tag-to-any_vlan_tag : serial-execution 
    $testCondition_2->single_test_2_4();

    # Test Scenario (2.5) 
    # any_vlan_tag-to-specific_vlan_tag : serial-execution 
    $testCondition_2->single_test_2_5();
}



sub run_cond_3
{
    # Topology Condition (3)
    # Two peering domains with EoMPLS network setting + eomplsPSS and Ethernet network setting + dragonPSS respectively; 
    # Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (3.1) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain 
    $testCondition_3->single_test_3_1();

    # Test Scenario (3.2) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : translation (src_tag!=dst_tag) : inter-domain 
    $testCondition_3->single_test_3_2();

    # Test Scenario (3.3) : inter-domain 
    # any_vlan_tag-to-any_vlan_tag : serial-execution : inter-domain 
    $testCondition_3->single_test_3_3();

    # Test Scenario (3.4) : inter-domain 
    # specific_vlan_tag-to-any_vlan_tag : serial-execution : inter-domain 
    $testCondition_3->single_test_3_4();

    # Test Scenario (3.5) 
    # any_vlan_tag-to-specific_vlan_tag : serial-execution : inter-domain 
    $testCondition_3->single_test_3_5();

    # Test Scenario (3.6) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : inter-domain : single-node-path-in-both-domains 
    #$testCondition_3->single_test_3_6();
}



sub run_cond_4
{
    # Topology Condition (4)
    # Single domain with EoMPLS network setting 
    # Bottleneck link in path with very small bandwidth and very limited number of VLANs;
    # L2SC edge links with VLAN translation enabled and PSC trunk links; 

    # Test Scenario (4.1) 
    # specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : mixed translation and no-translation 
    #$testCondition_4->multi_test_4_1();

    # Test Scenario (4.2) 
    # mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : no-translation 
    #$testCondition_4->multi_test_4_2();

    # Test Scenario (4.3) 
    # specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : no-translation : single-node-path with one end on the bottleneck link 
    #$testCondition_4->multi_test_4_3();
}



sub run_cond_5
{
    # Topology Condition (5)
    # Two peering domains with EoMPLS network setting + eomplsPSS and Ethernet network setting + dragonPSS respectively;
    # Bottleneck intra- and inter-domain links; 
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (5.1) 
    # specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : mixed translation and no-translation : inter-domain path 
    #$testCondition_5->multi_test_5_1();

    # Test Scenario (5.2) 
    # mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : no-translation : inter-domain path 
    #$testCondition_5->multi_test_5_2();

    # Test Scenario (5.3) 
    # specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : no-translation : single-hop-path-on-bottleneck-interdomain-link 
    #$testCondition_5->multi_test_5_3;
}



sub run_cond_6
{
    # Topology Condition (6)
    # Two peering domains with v0.6+EoMPLS network setting+eomplsPSS and v0.5.3+Ethernet network setting+dragonPSS respectively;
    # Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (6.1) 
    # specific_vlan_tag-to-specific_vlan_tag : v0.5-api-client-at-v0.6-domain : serial-execution : translation and no-translation : inter-domain path 
    #$testCondition_6->single_test_6_1();

    # Test Scenario (6.2) 
    # any_vlan_tag-to-any_vlan_tag : v0.5-api-client-at-v0.6-domain : serial-execution : translation and no-translation : inter-domain path 
    #$testCondition_6->single_test_6_2();

    # Test Scenario (6.3) 
    # specific_vlan_tag-to-specific_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path 
    $testCondition_6->single_test_6_3();

    # Test Scenario (6.4) 
    # any_vlan_tag-to-any_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path 
    $testCondition_6->single_test_6_4();
}



sub run_cond_7
{
    # Topology Condition (7)
    # Two peering domains with v0.6+EoMPLS network setting+eomplsPSS and v0.5.3+Ethernet network setting+dragonPSS respectively;
    # Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (7.1) 
    # specific_vlan_tag-to-specific_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path 
    $testCondition_7->single_test_7_1();

    # Test Scenario (7.2) 
    # any_vlan_tag-to-any_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path 
    $testCondition_7->single_test_7_2();
}



sub run_cond_8
{
    # Topology Condition (8)
    # Three peering domains in linear topology, two configured with EoMPLS network setting+eomplsPSS and on with v0.5.3+Ethernet network setting+dragonPSS respectively;
    # Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (8.1) 
    # mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : continue-random-execution-and-monitor 
    #$testCondition_8->multi_test_8_1();

    # Test Scenario (8.2) 
    # mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : :schedule-for-ten-thousand-circuits : continue-random-execution-and-monitor 
    #$testCondition_8->multi_test_8_2();
}



sub run_cond_9
{
    # Topology Condition (9)
    # Three peering domains (1, 2 and 3) in linear topology, two configured with EoMPLS network setting+eomplsPSS and on with v0.5.3+Ethernet network setting+dragonPSS respectively;
    # Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
    # L2SC edge links with VLAN translation enabled and PSC trunk links

    # Test Scenario (9.1) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : crash-individual-module-during-active & 
    $testCondition_9->single_test_9_1();

    # Test Scenario (9.2) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : crash-individual-module-during-setup & 
    $testCondition_9->single_test_9_2();

    # Test Scenario (9.3) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : restart-oscars-during-active 
    $testCondition_9->single_test_9_3();

    # Test Scenario (9.4) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : intra-domain path : restart-oscars-during-setup 
    $testCondition_9->single_test_9_3();

    # Test Scenario (9.5) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain path : restart-osacars-during-active && 
    $testCondition_9->single_test_9_5();

    # Test Scenario (9.6) 
    # specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain path : restart-osacars-during-setup && 
    $testCondition_9->single_test_9_6();
}

