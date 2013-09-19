#!/usr/bin/env python

"""
Creates a multi layer topology with 3 L1 switches, 2 L2 switches, and 8 hosts.
"""

import sys

from mininet.net import Mininet
from mininet.node import RemoteController, OVSKernelSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info

def demo(controller='192.168.56.1'):
    net = Mininet(
        controller=RemoteController,
        switch=OVSKernelSwitch)

    info( '*** Adding controller\n' )
    net.addController( 'c0', ip=controller)

    info( '*** Adding switches\n' )
    s1  = net.addSwitch( 's1' ) # brocade #1

    s2  = net.addSwitch( 's2' )
    s2.dpid = ('11:11:00:00:00:00:00:02'.replace(':', '')) # DTNx #1

    s3  = net.addSwitch( 's3' )
    s3.dpid = ('11:11:00:00:00:00:00:03'.replace(':', '')) # DTNx #2
    
    s4  = net.addSwitch( 's4' )
    s4.dpid = ('11:11:00:00:00:00:00:04'.replace(':', '')) # DTNx #3
    
    s5  = net.addSwitch( 's5' ) # brocade #2
    
    info( '*** Adding hosts\n' )
    h11 = net.addHost( 'h11', ip='10.0.0.11' )
    h12 = net.addHost( 'h12', ip='10.0.0.12' )
    h13 = net.addHost( 'h13', ip='10.0.0.13' )
    h14 = net.addHost( 'h14', ip='10.0.0.14' )
    h51 = net.addHost( 'h51', ip='10.0.0.51' )
    h52 = net.addHost( 'h52', ip='10.0.0.52' )
    h53 = net.addHost( 'h53', ip='10.0.0.53' )
    h54 = net.addHost( 'h54', ip='10.0.0.54' )
    
    info( '*** Creating links\n' )
    # s1 has 2 links to s2
    s1.linkTo(s2)
    s1.linkTo(s2)

    s2.linkTo(s3)
    s2.linkTo(s4)

    s3.linkTo(s4)

    # s4 has 2 links to s5
    s4.linkTo(s5)
    s4.linkTo(s5)

    h11.linkTo(s1)
    h12.linkTo(s1)
    h13.linkTo(s1)
    h14.linkTo(s1)

    h51.linkTo(s5)
    h52.linkTo(s5)
    h53.linkTo(s5)
    h54.linkTo(s5)

    info( '*** Starting network\n')
    net.start()

    info( '*** Running CLI\n' )
    CLI( net )

    info( '*** Stopping network' )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    
    if len(sys.argv) < 2:
        print "Usage: %s <controller IP>" % sys.argv[0]
    else:
        demo(controller=sys.argv[1])
    sys.exit(0)
