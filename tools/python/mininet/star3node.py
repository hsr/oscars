#!/usr/bin/env python

"""
Creates a multi layer topology with 3 L1 switches, 3 L2 switches, and 2 hosts.
"""

from mininet.net import Mininet
from mininet.node import RemoteController, OVSKernelSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info

def multilayer():

    ""

    net = Mininet(
        controller=RemoteController,
        switch=OVSKernelSwitch)

    info( '*** Adding controller\n' )
    net.addController( 'c0', ip='192.168.56.1')

    info( '*** Adding switches\n' )
    # switches = {}
    # for i in xrange(1,11):
    #     switches['s%d' % i] = net.addSwitch( 's%d' % i )

    s1  = net.addSwitch( 's1' )

    h11 = net.addHost( 'h11', ip='10.0.0.11' )
    h12 = net.addHost( 'h12', ip='10.0.0.12' )
    h13 = net.addHost( 'h13', ip='10.0.0.13' )
    
    info( '*** Creating links\n' )

    h11.linkTo( s1 )
    h12.linkTo( s1 )
    h13.linkTo( s1 )

    info( '*** Starting network\n')
    net.start()

    info( '*** Running CLI\n' )
    CLI( net )

    info( '*** Stopping network' )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    multilayer()
