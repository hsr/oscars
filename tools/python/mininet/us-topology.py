#!/usr/bin/env python

"""
Creates a topology in which nodes can be arranged in a similar way as the esnet us network
"""

from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel, info

def multilayer():

    ""

    net = Mininet(
        controller=RemoteController)

    info( '*** Adding controller\n' )
    net.addController( 'c0', ip='192.168.56.99')


    info( '*** Adding switches\n' )
    # switches = {}
    # for i in xrange(1,11):
    #     switches['s%d' % i] = net.addSwitch( 's%d' % i )
    s1  = net.addSwitch( 's1'  )
    s2  = net.addSwitch( 's2'  )
    s3  = net.addSwitch( 's3'  )
    s4  = net.addSwitch( 's4'  )
    s5  = net.addSwitch( 's5'  )
    s6  = net.addSwitch( 's6'  )
    s7  = net.addSwitch( 's7'  )
    s8  = net.addSwitch( 's8'  )
    s9  = net.addSwitch( 's9'  )
    s10 = net.addSwitch( 's10' )
    s11 = net.addSwitch( 's11' )


    info( '*** Adding hosts\n' )
    h12 = net.addHost( 'h12', ip='10.0.0.12' )
    h13 = net.addHost( 'h13', ip='10.0.0.13' )
    h14 = net.addHost( 'h14', ip='10.0.0.14' )
    h15 = net.addHost( 'h15', ip='10.0.0.15' )
    h16 = net.addHost( 'h16', ip='10.0.0.16' )
    
    info( '*** Creating links\n' )   
    s1.linkTo(s2)
    s1.linkTo(s3)
    
    s2.linkTo(s4)
    
    s3.linkTo(s4)
    s3.linkTo(s6)
    
    s4.linkTo(s5)
    
    s5.linkTo(s7)
    
    s6.linkTo(s8)
    
    s7.linkTo(s8)
    
    s8.linkTo(s9)
    
    s9.linkTo(s10)
    s9.linkTo(s11)
    
    s10.linkTo(s11)


    h12.linkTo( s1 )
    h13.linkTo( s4 )
    h14.linkTo( s7 )
    h15.linkTo( s8 )
    h16.linkTo( s10 )

    info( '*** Starting network\n')
    net.start()

    info( '*** Running CLI\n' )
    CLI( net )

    info( '*** Stopping network' )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    multilayer()