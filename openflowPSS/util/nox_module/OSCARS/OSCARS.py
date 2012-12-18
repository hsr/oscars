import logging
import ConfigParser
import math
import re
import simplejson

from nox.lib.core import *
from nox.webapps.webservice import webservice
from nox.lib.netinet import netinet

from nox.lib.packet.packet_utils  import mac_to_str

from nox.webapps.webservice.webservice import json_parse_message_body

from nox.netapps.flow_fetcher.pyflow_fetcher import flow_fetcher_app

from twisted.internet import defer

logger = logging.getLogger('nox.webapps.OSCARS.OSCARS')

default_config_file = 'OSCARS.conf'

config = ConfigParser.ConfigParser()

def getFactory():
    class Factory:
        def instance(self, ctxt):
            return OSCARS(ctxt)

    return Factory()

class OSCARS(Component):
    def __init__(self, ctxt):
        self.current_session = None
        self.ffa             = None
        Component.__init__(self, ctxt)
        self.known_switches  = {}

    def install(self):
	# The config file contains, among other things, a mapping from IP
	# address to datapath id. Presumably, this could be figured out
	# automagically, but for now I'm putting it in a configuration file.
        config.read(default_config_file)

        ws = self.resolve(str(webservice.webservice))
        v1 = ws.get_version("1")
        v1.register_request(lambda request, data: self.handle_ws_request(request, data), "POST", [ webservice.WSPathStaticString("OSCARS") ], "OSCARS JSON Target")

        self.ffa = self.resolve(flow_fetcher_app)
        if self.ffa == None:
            raise ValueError("couldn't find flow_fetcher_app")

        self.register_for_datapath_join(self.dp_join)
        self.register_for_datapath_leave(self.dp_leave)

    def getInterface(self):
        return str(OSCARS)

    def dp_join(self, dp, stats):
        if self.known_switches.has_key(dp):
            lg.err("Received datapath join for a known switch: %s" % hex(dp))
            del self.known_switches[dp]

        import pprint
        lg.debug("Received datapath join %s: %s" % (hex(dp), pprint.pformat(stats)))

        stats['dpid']     = dp 

        ip = self.ctxt.get_switch_ip(dp)
        stats["ip"] = str(create_ipaddr(c_htonl(ip)))

        self.known_switches[dp] = stats

        ports_by_id = {}
        ports_by_name = {}
        for port in stats["ports"]:
          new_mac = mac_to_str(port['hw_addr'])
          port['hw_addr'] = new_mac 

          ports_by_name[port['name']] = port
          ports_by_id[port['port_no']] = port

        stats["ports_by_id"] = ports_by_id
        stats["ports_by_name"] = ports_by_name

        return CONTINUE

    def dp_leave(self, dp): 
        if self.known_switches.has_key(dp):
            del self.known_switches[dp]
        else:
            lg.info("Unknown switch left network")

        return CONTINUE


    def handle_ws_request(self, request, data):
        if self.current_session != None:
            return webservice.conflictError(request, "Outstanding request pending. Try again later.")

        oscars_request = None

        try:
            json_content = webservice.json_parse_message_body(request)
            if json_content == None:
                raise ValueError('Invalid JSON request')

            oscars_request = OSCARSRequest(json_content)

            for path_element in oscars_request.path:
                dpid = None

                lg.debug("Trying to find switch in known_switches")
                for (curr_dpid, switch) in self.known_switches.items():
                    if switch["ip"] == path_element["switch"]:
                        dpid = switch["dpip"]
                        lg.debug("Found dpid for %s in existing switches" % (path_element["switch"]))
                        break

                if not dpid:
                    lg.debug("Trying to find switch in config")
                    try:
                        dpid = long(config.get(path_element["switch"], "datapath_id"), 0)
                    except Exception as e:
                        lg.error("Problem finding 'datapath_id' for '%s': %s" % (element["switch"], e.__str__()))
                        raise ValueError("Unknown switch '%s'" % element["switch"])

                curr_switch = None
                if dpid and self.known_switches.has_key(dpid):
                    curr_switch = self.known_switches[dpid]

                if not curr_switch:
                    error_msg = "Switch '%s' is not a known switch" % (path_element["switch"])
                    lg.error(error_msg)
                    raise ValueError(error_msg)

                lg.debug("Checking for commands")
                for key in "del-flows", "add-flows":
                    if key in path_element:
                        if len(path_element[key]) != 2:
                            raise ValueError("The flow needs to have two elements")
 
                        lg.debug("Trying %s" % key)
                        for i in range(0,1):
                            lg.debug("Parsing port")
                            port = int(path_element[key][i]["port"])
                            lg.debug("Checking ports_by_id")
                            if port in curr_switch["ports_by_id"]:
                                continue
                            lg.debug("Checking ports_by_name")
                            if str(port) in curr_switch["ports_by_name"]:
                                path_element[key][i]["port"] = curr_switch["ports_by_name"][str(port)]["port_no"]
                                continue
                            raise ValueError("Unknown port: %s in %s" % (path_element[key][i]["port"], hex(dpid)))
 
        except Exception as e:
            lg.error("Invalid request: %s" % e.__str__())
            return webservice.badRequest(request, e.__str__())

        self.current_session = OSCARSSession(request=oscars_request, raw_request=request)

        successful = True
        error_msg = ""
        try:
            if self.current_session.request.path:
                logger.debug("Request has a path")
                for path_element in self.current_session.request.path:
                    dpid = long(config.get(path_element["switch"], "datapath_id"), 0)

                    logger.debug("Configuring switch %s: %s" % (path_element["switch"], dpid))

                    for key in "del-flows", "add-flows":

                        logger.debug("Handling %s for switch %s/%s" % (key, path_element["switch"], dpid))
    
                        if key in path_element:
                            if key == "del-flows":
                                action = "delete"
                            else:
                                action = "add"
                    
                            logger.debug("Handling %s for switch %s" % (action, path_element["switch"]))
 
                            self.modify_flow(action, dpid, path_element[key][0], path_element[key][1]);
    
                            self.modify_flow(action, dpid, path_element[key][1], path_element[key][0]);
        except Exception as e:
            error_msg = "Problem handling request: %s" % e
            logger.error(error_msg)
            successful = False

        if successful:
            # wait 5 seconds and then check that the changes propagated
            self.post_callback(5, lambda: self.verify_changes(self.verify_changes_timeout_cb))
        else:
            self.undo_changes()

        return webservice.NOT_DONE_YET

    def verify_changes(self, callback):
        logger.debug("verify_changes")
        self.current_session.changes_to_verify = self.current_session.changes[:]

        # wait 5 seconds and then check that everything verified
        self.post_callback(5, callback)

        self.post_next_verify()

    def post_next_verify(self):
        change = None
        while len(self.current_session.changes_to_verify) > 0:
            curr_change = self.current_session.changes_to_verify.pop(0)
            if curr_change.status != "FAILED":
                change = curr_change
                break

        if change == None:
            return

        if (change.element == "flow"):
            source_port_info = change.parameters["source"]
            destination_port_info = change.parameters["destination"]

            request = { #'out_port': int(destination_port_info["port"]),
                        'match': { 'in_port': int(source_port_info["port"]),
                                   'dl_vlan': int(source_port_info["vlan_range"])
                        }
                      }

            dpid = netinet.create_datapathid_from_host(change.switch)

            logger.debug("Fetching %d.%d from %d" % (int(source_port_info["port"]), int(source_port_info["vlan_range"]), change.switch))

            change.ff = self.ffa.fetch(dpid, request, lambda: self.flow_result_cb(change))

        return

    def flow_result_cb(self, change):
        source_port_info = change.parameters["source"]
        destination_port_info = change.parameters["destination"]
        logger.debug("Received result for %d.%d from %d" % (int(source_port_info["port"]), int(source_port_info["vlan_range"]), change.switch))

        status = change.ff.get_status()
        logger.debug("Status is %d" % status)

        flows = change.ff.get_flows()
        if len(flows) == 0:
            logger.debug("Found 0 flows")
            expected_change = "delete"
        else:
            logger.debug("Found %d flows" % len(flows))
            expected_change = "add"

        for flow in flows:
            try:
                logger.debug(flow)
            except Exception as e:
                pass

        if expected_change != change.action:
            change.status="FAILED"
        else:
            change.status="SUCCESS"

        self.post_next_verify()

    def verify_changes_timeout_cb(self):
        for change in self.current_session.changes:
            if change.ff:
                change.ff.cancel()

        successful = True
        for change in self.current_session.changes:
            if change.status == "UNKNOWN" or change.status == "FAILED" or change.status == "PENDING":
                successful = False
                self.current_session.failed_changes.append(change)

        if successful:
            return self.finish_ws_request(successful=True)
        else:
            return self.undo_changes()

    def undo_changes_timeout_cb(self):
        for change in self.current_session.changes:
            if change.ff:
                change.ff.cancel()

        successful = True
        for change in self.current_session.changes:
            if change.status == "UNKNOWN" or change.status == "FAILED" or change.status == "PENDING":
                successful = False
                self.current_session.failed_undone_changes.append(change)

        return self.finish_ws_request(successful=False, undid_changes=successful)

    def finish_ws_request(self, successful=False, undid_changes=False, error_msg=""):
        status = ""
        if successful:
            if self.current_session.request.action == "setup":
                status = "ACTIVE"
            elif self.current_session.request.action == "teardown":
                status = "FINISHED"
        elif undid_changes:
            status = "FAILED"
        else:
            status = "UNKNOWN"

        for change in self.current_session.failed_changes:
            if error_msg != "":
                error_msg += ". "
            source_port_info = change.parameters["source"]

            error_msg += "Problem configuring switch: %s failed on %s %d.%d" % (change.action, hex(change.switch), int(source_port_info["port"]), int(source_port_info["vlan_range"]))

        for change in self.current_session.failed_undone_changes:
            if error_msg != "":
                error_msg += ". "
            source_port_info = change.parameters["source"]

            undo_action = "delete" if (change.action == "add") else "add"

            error_msg += "Problem undoing switch configuration: %s failed on %s %d.%d" % (undo_action, hex(change.switch), int(source_port_info["port"]), int(source_port_info["vlan_range"]))


        response = {
            "type": "oscars-reply",
            "version": "1.0",
            "gri": self.current_session.request.gri,
            "action": self.current_session.request.action,
            "status": status,
            "err_msg": error_msg
        }

        self.current_session.raw_request.setResponseCode(200, "Successful")
        self.current_session.raw_request.setHeader("Content-Type", "application/json")
        self.current_session.raw_request.write(simplejson.dumps(response))
        self.current_session.raw_request.finish()

        self.current_session = None

        return

    def undo_changes(self):
        existing_changes = self.current_session.changes
        self.current_session.changes = []  # reset changes_applied since modify_flow is used and it fills that

        for change in existing_changes:
            if (change.status != "SUCCESS"):
                continue

            undo_action = "delete" if (change.action == "add") else "add"

            if (change.element == "flow"):
                source_port_info = change.parameters["source"]
                destination_port_info = change.parameters["destination"]

                self.modify_flow(undo_action, change.switch, source_port_info, destination_port_info)
            else:
                logger.error("Unknown element type %s" % element_type)

        self.post_callback(2, lambda: self.verify_changes(self.undo_changes_timeout_cb))

        return

    def modify_flow(self, action, dpid, source_port_info, destination_port_info):
        flow = {}
        flow[core.IN_PORT] = int(source_port_info["port"])
        flow[core.DL_VLAN] = int(source_port_info["vlan_range"])

        successful = True
        try:
            if action == "add":
                actions = []
                actions.append([openflow.OFPAT_SET_VLAN_VID, int(destination_port_info["vlan_range"])])
                actions.append([openflow.OFPAT_OUTPUT, [0, int(destination_port_info["port"])]])

                self.install_datapath_flow(dp_id=dpid, attrs=flow, actions=actions, idle_timeout=openflow.OFP_FLOW_PERMANENT, hard_timeout=openflow.OFP_FLOW_PERMANENT)
            else:
                self.delete_datapath_flow(dp_id=dpid, attrs=flow)
        except Exception as e:
            logger.error("Problem modifying flow: %s" % e)
            successful = False

        if successful:
            self.current_session.changes.append(SwitchChange(status="PENDING", switch=dpid, action=action, element="flow", parameters={"source": source_port_info, "destination": destination_port_info}))
        else:
            self.current_session.changes.append(SwitchChange(status="FAILED", switch=dpid, action=action, element="flow", parameters={"source": source_port_info, "destination": destination_port_info}))

class OSCARSSession:
    def __init__(self, request=None, raw_request=None):
        self.request = request
        self.raw_request = raw_request
        self.changes = []
        self.changes_to_verify = []
        self.failed_changes = []
        self.failed_undone_changes = []

class OSCARSRequest:
    def __init__(self, request):
        logger.debug("in __init__")
        if not isinstance(request, dict):
            raise ValueError('JSON request is not a hash')

        logger.debug("is a dict")
        if not request["version"]:
            raise ValueError('No version found in request')

        logger.debug("is a version")
        if request["version"] != "1.0":
            raise ValueError('Only version 1.0 requests are supported')

        logger.debug("version 1.0")

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

        logger.debug("finished iterating")

        if not self.action:
            raise ValueError('No action found in request')

        if not self.gri:
            raise ValueError('No gri found in request')

        if not self.path:
            raise ValueError('No path found in request')

        logger.debug("request is correct")

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

class SwitchChange:
    def __init__(self, switch, element, action, parameters, status="PENDING"):
        self.switch = switch
        self.element = element
        self.action = action
        self.parameters = parameters
        self.status = status
        self.ff = None
