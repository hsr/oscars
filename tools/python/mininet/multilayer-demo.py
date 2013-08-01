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
    s2  = net.addSwitch( 's2' )
    s2.dpid = ('11:11:00:00:00:00:00:02'.replace(':', ''))

    s3  = net.addSwitch( 's3' )
    s3.dpid = ('11:11:00:00:00:00:00:03'.replace(':', ''))
    
    
    s4  = net.addSwitch( 's4' )
    s5  = net.addSwitch( 's5' )
    s5.dpid = ('11:11:00:00:00:00:00:05'.replace(':', ''))
    
    s6  = net.addSwitch( 's6' )
    s6.dpid = ('11:11:00:00:00:00:00:06'.replace(':', ''))
    
    s7  = net.addSwitch( 's7' )


    info( '*** Adding hosts\n' )
    h11 = net.addHost( 'h11', ip='10.0.0.11' )
    h12 = net.addHost( 'h12', ip='10.0.0.12' )
    h71 = net.addHost( 'h71', ip='10.0.0.71' )
    
    info( '*** Creating links\n' )
    s1.linkTo(s2)

    s2.linkTo(s3)
    s2.linkTo(s6)
    
    s3.linkTo(s4)
    
    s4.linkTo(s5)
    
    s5.linkTo(s6)

    s6.linkTo(s7)
    
    h11.linkTo( s1 )
    h12.linkTo( s1 )
    h71.linkTo( s7 )

    info( '*** Starting network\n')
    net.start()

    info( '*** Running CLI\n' )
    CLI( net )

    info( '*** Stopping network' )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    multilayer()
