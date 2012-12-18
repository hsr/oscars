#!/usr/bin/python

import cjson
import ConfigParser
import logging
import math
import re
import subprocess
import web

urls = ( '/ws.v1/OSCARS', 'OSCARS_WS' )

logger = logging.getLogger('nox.webapps.OSCARS.OSCARS')

config_file = "oscars_openflow.conf"

dpctl_command = "dpctl"
config = ConfigParser.ConfigParser()

class OSCARS_WS:
    def POST(self):
        request = None

        config.read(config_file)

        try:
            dpctl_command = config.get("main", "dpctl_command")
        except Exception as e:
            pass

        try:
            request = cjson.decode(web.data())
        except Exception as e:
            logger.debug("PROBLEM: %s: %s" % (e.__str__(), web.data()))
            return "PROBLEM: ", e.__str__()

        try:
            result = OSCARSRequestHandler(None, request).handle()
        except Exception as e:
            logger.error(e.__str__())
            return e.__str__()

        web.header('Content-Type', 'application/json')

        return result

class OSCARSRequestHandler:
    def __init__(self, raw_request, request):
        self.raw_request = raw_request
        self.request = request

    def handle(self):
        result = None

        try:
            oscars_request = OSCARSRequest(self.request)

            if oscars_request.action == "setup" or oscars_request.action == "teardown":
                result = self.handle_change(oscars_request)
            elif request.action == "verify":
                result = self.handle_verify(oscars_request)

        except Exception as e:
            logger.error("Invalid request: %s" % e.__str__())
            raise e

        return result

    def handle_change(self, request):

        error_msg = ""

        successful = None
        changes_applied = {}
        try:
            if request.path:
                logger.debug("Request has a path")
                for path_element in request.path:
                    logger.debug("Path is iteratable")
                    changes_applied[path_element["switch"]] = []

                    logger.debug("Configuring switch %s" % path_element["switch"])

                    for key in "del-flows", "add-flows":

                        logger.debug("Handling %s for switch %s" % (key, path_element["switch"]))
    
                        if key in path_element:
                            if len(path_element[key]) != 2:
                                raise ValueError("The flow needs to have two elements")

                            if key == "del-flows":
                                action = "delete"
                            else:
                                action = "add"
                    
                            logger.debug("Handling %s for switch %s" % (action, path_element["switch"]))
 
                            port0_queue = self.modify_queue(action, path_element["switch"], path_element[key][0]["port"], path_element[key][0]["vlan_range"], request.bandwidth)

                            changes_applied[path_element["switch"]].append([action, "queue", path_element[key][0]["port"], path_element[key][0]["vlan_range"], request.bandwidth])
    
                            port1_queue = self.modify_queue(action, path_element["switch"], path_element[key][1]["port"], path_element[key][1]["vlan_range"], request.bandwidth)
    
                            changes_applied[path_element["switch"]].append([action, "queue", path_element[key][1]["port"], path_element[key][1]["vlan_range"], request.bandwidth])
    
                            self.modify_flow(action, path_element["switch"], path_element[key][0], path_element[key][1], port1_queue);
    
                            changes_applied[path_element["switch"]].append([action, "flow", path_element[key][0], path_element[key][1], port1_queue])

                            self.modify_flow(action, path_element["switch"], path_element[key][1], path_element[key][0], port0_queue);

                            changes_applied[path_element["switch"]].append([action, "flow", path_element[key][0], path_element[key][1], port0_queue])

                successful = True
        except Exception as e:
            logger.error("Problem configuring switch: %s" % e)
            error_msg = "Problem configuring switch: %s" % e
            successful = False

        if successful == False:
            try:
                self.undo_changes(changes_applied)
            except Exception as e:
                logger.error("Problem undoing changes: %s" % e)
                error_msg += ": Problem backing out changes: %s" % e

        status = ""
        if successful:
            if request.action == "setup":
                status = "ACTIVE"
            elif request.action == "teardown":
                status = "FINISHED"
        else:
            status = "FAILED"

        response = {
            "type": "oscars-reply",
            "version": "1.0",
            "gri": request.gri,
            "action": request.action,
            "status": status,
            "err_msg": error_msg
        }

        return cjson.encode(response)

    def undo_changes(self, changes_applied):
        for switch in changes_applied:

            changes_applied[switch].reverse()

            for change in changes_applied[switch]:
                undo_action = "delete" if (change[0] == "add") else "add"
                element_type = change[1]

                if (element_type == "queue"):
                    port = change[2]
                    vlan_range = change[3]
                    bandwidth = change[4]

		    # Have an 'internal' try here so that we can undo as much
		    # as possible even if we can't undo a specific element
                    try:
                        self.modify_queue(undo_action, switch, port, vlan_range, bandwidth)
                    except Exception as e:
                        logger.error("Couldn't undo queue change: %s" % e)

                elif (element_type == "flow"):
                    source_port_info = change[2]
                    destination_port_info = change[3]
                    queue_name = change[4]

                    try:
                        self.modify_flow(undo_action, switch, source_port_info, destination_port_info, queue_name)
                    except Exception as e:
                        logger.error("Couldn't undo flow change: %s" % e)

                else:
                    raise ValueError("Unknown element type %s" % element_type)

    def lookup_port_size(self, switch, port):
        try:
            bandwidth = config.get(switch, "port_bandwidth")
            logger.debug("Found bandwidth in switch section")
            return convert_bandwidth_to_bps(bandwidth)
        except Exception:
            pass

        try:
            bandwidth = config.get("main", "port_bandwidth")
            logger.debug("Found bandwidth in main section")
            return convert_bandwidth_to_bps(bandwidth)
        except Exception as e:
            logger.debug("Problem: %s" % e)
            pass

        raise ValueError("No port bandwidth available for %s/%s" % (switch, port))


    def modify_queue(self, action, switch, port, vlan, bandwidth):
        logger.debug("Running modify_queue: %s, %s, %s, %s, %s" % (action, switch, port, vlan, bandwidth))

        # calculate the queue size (in tenths of a percent of the port's bandwidth)
        port_size = self.lookup_port_size(switch=switch, port=port)
        port_size = float(port_size) # ensure that a floating point divide gets done
        queue_size = "%d" % (math.ceil((bandwidth/port_size) * 1000)) 

        queue_name = self.get_queue_name(port, vlan, bandwidth)

        if action == "add":
            command = [ dpctl_command, "add-queue", "tcp:%s" % switch, port, queue_name, queue_size ]
        else:
            command = [ dpctl_command, "del-queue", "tcp:%s" % switch, port, queue_name ]

        logger.debug("Running command %s" % (" ".join(command)))

        retcode = subprocess.call(command);
        if retcode != 0: 
            raise ValueError("Couldn't %s queue %s to port %s on switch %s" % (action, queue_name, port, switch))

        logger.debug("retcode for command: %d" % retcode)

        return queue_name

    def modify_flow(self, action, switch, source_port_info, destination_port_info, port_queue):
        flow_information = self.get_flow_information(source_port_info, destination_port_info, port_queue)

        if action == "add":
            flow_description = "%s,actions=%s" % ( flow_information["flow_description"], flow_information["target_action"] )
            command = [ dpctl_command, "add-flow", "tcp:%s" % switch, flow_description ]
        else:
            command = [ dpctl_command, "del-flows", "tcp:%s" % switch, flow_information["flow_description"] ]

        logger.debug("Running command %s" % (" ".join(command)))

        retcode = subprocess.call(command);
        if retcode != 0: 
            raise ValueError("Couldn't %s flow %s on switch %s" % (action, flow_description, switch))

    def get_queue_name(self, port, vlan, bandwidth):
        return vlan

    def get_flow_information(self, source_port_info, destination_port_info, port_queue):
        target_action = "mod_vlan_vid:%s,enqueue:%s:%s" % (destination_port_info["vlan_range"], destination_port_info["port"], port_queue)
        flow_description = "in_port=%s,dl_vlan=%s" % (source_port_info["port"], source_port_info["vlan_range"])

        return { "flow_description": flow_description, "target_action": target_action }

#    def handle_verify(self, request):

class OSCARSRequest:
    def __init__(self, request):
        if not isinstance(request, dict):
            raise ValueError('JSON request is not a hash')

        if not request["version"]:
            raise ValueError('No version found in request')

        if request["version"] != "1.0":
            raise ValueError('Only version 1.0 requests are supported')

        for k, v in request.iteritems():
            if (k == "action"):
                if not isinstance(v, str):
                    raise ValueError('action must be a string')

                if v != "setup" and v != "teardown" and v != "modify" and v != "verify":
                    raise ValueError('Invalid action found in request')

                self.action = v
            elif (k == "gri"):
                if not isinstance(v, str):
                    raise ValueError('gri must be a string')

                self.gri = v
            elif (k == "path"):
                self.path = self.parse_path(request["path"])
            elif (k == "version"):
                self.version = v
            elif (k == "bandwidth"):
                self.bandwidth = convert_bandwidth_to_bps(v)
                logger.debug("Bandwidth: %s" % self.bandwidth)
            elif (k == "type"):
                continue
            else:
                raise ValueError('Unknown element %s in request' % k)

        if not self.action:
            raise ValueError('No action found in request')

        if not self.gri:
            raise ValueError('No gri found in request')

        if not self.path:
            raise ValueError('No path found in request')

    def parse_path (self, path):
        logger.debug("parse_path")
        if not isinstance(path, list):
            raise ValueError('Invalid path in request')

        new_path = []
        for element in path:
            new_path.append(self.parse_path_element(element))

        return new_path

    def parse_path_element (self, element):
        logger.debug("parse_path_element")
        if not isinstance(element, dict):
            raise ValueError('Invalid path element')

        new_element = {}
        for k, v in element.iteritems():
            if (k == "switch"):
                if not isinstance(v, str):
                    raise ValueError('invalid switch element in path element')

                # xxx: validate the switch as an ip or whatever

                new_element["switch"] = v
            elif (k == "del-flows" or k == "add-flows"):
                if not isinstance(v, list):
                    raise ValueError('invalid %s element in path element' % k)
                new_element[k] = self.parse_flows(v)
            else:
                raise ValueError('Unknown element %s in path element' % k)

        if (not new_element["switch"] or
             (not "del-flows" in new_element and not "add-flows" in new_element)):
            raise ValueError('path element is missing required elements')

        return new_element

    def parse_flows (self, flows):
        logger.debug("parse_flows")
        if not isinstance(flows, list):
            raise ValueError('Invalid flows in request')

        new_flows = []
        for element in flows:
            new_flows.append(self.parse_flow_element(element))

        return new_flows

    def parse_flow_element (self, element):
        logger.debug("parse_flow_element")
        if not isinstance(element, dict):
            raise ValueError('Invalid flow description')

        new_flow = {}
        for k, v in element.iteritems():
            if (k == "port"):
                if not isinstance(v, str):
                    raise ValueError('invalid port element in flow description')

                # xxx: validate the port

                new_flow[k] = v
            elif (k == "vlan_range"):
                if not isinstance(v, str):
                    raise ValueError('invalid vlan_range element in flow description')

                # xxx: validate the VLAN range

                new_flow[k] = v
            else:
                raise ValueError('Unknown element %s in flow description' % k)

        if not new_flow["port"] or not new_flow["vlan_range"]:
            raise ValueError('flow description is missing required elements')

        return new_flow

def convert_bandwidth_to_bps(bandwidth):
    m = re.search('^([0-9][0-9]*)([kmgKMG])([Bb])ps$', bandwidth)
    if (m):
        new_bandwidth = int(m.group(1))
        if (m.group(2) == "k" or m.group(2) == "K"):
            new_bandwidth *= 1000
        if (m.group(2) == "m" or m.group(2) == "M"):
            new_bandwidth *= 1000*1000
        if (m.group(2) == "g" or m.group(2) == "G"):
            new_bandwidth *= 1000*1000*1000
        if (m.group(3) == "B"):
            new_bandwidth *= 8
        return new_bandwidth

    m = re.search('^[0-9]+$', bandwidth)
    if (m):
        return bandwidth

    raise ValueError('Unknown bandwidth value %s' % bandwidth)

if __name__ == "__main__":
    config.read(config_file)

    try:
        log_file = config.get("main", "log_file")
    except Exception as e:
        pass

    logging.basicConfig(level=logging.DEBUG,filename=log_file)

    web.config.debug = True

    app = web.application(urls, globals())
    app.run()
