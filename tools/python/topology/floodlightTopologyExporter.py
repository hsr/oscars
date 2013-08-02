#!/usr/bin/env python

import sys

"""
SYNOPSIS
 
    Usage: %s [options]
 
DESCRIPTION
 
    This program extracts Floodlight network topology information using Floodlight's 
    REST interface and creates an XML file with the extracted topology using the
    NMWG format

@author Henrique Rodrigues hsr@cs.ucsd.edu

""" % sys.argv[0]

import os
import sys
import subprocess
import json
import argparse
import io
import time


from NMWG import *

parser = argparse.ArgumentParser(description=sys.argv[0])
parser.add_argument('--controller', dest='controllerRestIp', action='store', default='localhost:8080', help='controller IP:RESTport, e.g., localhost:8080 or A.B.C.D:8080')
parser.add_argument('--output', dest='output', action='store', default='topology.xml', help='The output file. Defaults to topology.xml')
parser.add_argument('--domain', dest='domain', action='store', default='testdomain-1', help='The domain name to use. Defaults to testdomain-1')

args             = parser.parse_args()
controllerRestIp = args.controllerRestIp
domain           = args.domain
outputFile       = args.output

command = "curl -s http://%s/wm/topology/links/json" % (args.controllerRestIp)
result = os.popen(command).read()

f = open('floodlight_'+outputFile, 'w');
f.write(result)
f.close()

topologyLinks = json.loads(result)

topologyNodes = {}

for l in topologyLinks:
    srcNode = l['src-switch'].replace(':','.')
    dstNode = l['dst-switch'].replace(':','.')
    
    srcPort = l['src-port']
    dstPort = l['dst-port']
    
    if not topologyNodes.has_key(srcNode):
        topologyNodes[srcNode] = {
            'links' : []
        }
    if not topologyNodes.has_key(dstNode):
        topologyNodes[dstNode] = {
            'links' : []
        }
    
    # TODO: check for bidirectional links
    topologyNodes[srcNode]['links'] += [{
        'srcNode': srcNode,
        'srcPort': srcPort,
        'dstNode': dstNode,
        'dstPort': dstPort,
    }]
    topologyNodes[dstNode]['links'] += [{
        'srcNode': dstNode,
        'srcPort': dstPort,
        'dstNode': srcNode,
        'dstPort': srcPort,
    }]

nodes=''
for nodeID,n in topologyNodes.items():
    nodePorts = ''
    for l in n['links']:
        # print l['srcNode']+l['srcPort'], '->', l['dstNode']+l['dstPort']
        nodeLink = NMWGXML_Link(domain,l['srcNode'],l['srcPort'],
                                domain,l['dstNode'],l['dstPort'])
        # TODO: this is assuming that a node's port only has one link. FIXME for multiple
        # links per port
        nodePorts += NMWGXML_Port(domain, l['srcNode'], l['srcPort'], nodeLink)
    nodes += NMWGXML_Node (domain, nodeID, nodePorts, '127.0.0.1')

try: 
    f = open(outputFile, 'w');
    f.write(NMWGXML_Domain(domain, nodes))
    f.close()
except Exception, e:
    print 'Error writing to file: %s' % str(e)
