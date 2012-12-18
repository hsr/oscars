/*
ReservationDetails.js:  Handles reservation details form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postQueryReservation()
postQueryReservationStatus()
postCancelReservation(dialogFields)
postCreatePath(dialogFields)   not implemented
postTeardownPath(dialogFields)  not implemented
postOverrideStatus(dialogFields)
handleReply(responseObject, ioArgs)
layerParams(responseObject)
tabSelected(contentPaneWidget, oscarsStatus)
*/

dojo.provide("oscars.ReservationDetails");

// posts reservation query to server
oscars.ReservationDetails.postQueryReservation = function (newGri) {
    if (oscarsState.reservationDetailsEntered) {
        return;
    }
    oscarsState.reservationDetailsEntered = true;
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    if (!newGri) {
        oscars.ReservationDetails.setCurrentGri(formNode);
    } else {
        newGri = dijit.byId("newGri").getValue();
        // can happen if hit enter accidentally more than once
        if (oscars.Utils.isBlank(newGri)) {
            oscarsState.reservationDetailsEntered = false;
            return;
        }
        formNode.gri.value = newGri;
    }
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
};

// posts reservation status query to server
oscars.ReservationDetails.postQueryReservationStatus = function () {
    dojo.xhrPost({
        url: 'servlet/QueryReservationStatus',
        handleAs: "json",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
};

oscars.ReservationDetails.postModify = function () {
    var legalDates = oscars.ReservationDetails.checkDateTimes();
    if (!legalDates) {
        return;
    }
    valid = dijit.byId("reservationDetailsForm").validate();
    if (!valid) {
        return;
    }
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    var bandwidth = dojo.byId("bandwidthReplace").value;
    formNode.modifyBandwidth.value = bandwidth;
    var description = dojo.byId("descriptionReplace").value;
    formNode.modifyDescription.value = description;
    dojo.xhrPost({
        url: 'servlet/ModifyReservation',
        handleAs: "json",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: formNode
    });
};

// posts cancel request to server
oscars.ReservationDetails.postCancelReservation = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/CancelReservation',
            handleAs: "json",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// posts create path request to server
/*
oscars.ReservationDetails.postCreatePath = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/PathSetupReservation',
            handleAs: "json",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

// posts teardown path request to server
oscars.ReservationDetails.postTeardownPath = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    dojo.xhrPost({
        url: 'servlet/PathTeardownReservation',
            handleAs: "json",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};
*/

// posts override status request to server
oscars.ReservationDetails.postOverrideStatus = function (dialogFields) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscars.ReservationDetails.setCurrentGri(formNode);
    var currentStatus = dojo.byId("statusReplace").innerHTML;
    formNode.status.value = currentStatus;
    if ((formNode.forcedStatus.value === null) ||
        oscars.Utils.isBlank(formNode.forcedStatus.value)) {
        oscars.ReservationDetails.forcedStatus();
    }
    dojo.xhrPost({
        url: 'servlet/OverrideStatusReservation',
            handleAs: "json",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: formNode
    });
};

oscars.ReservationDetails.forcedStatus = function () {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    var menu = dojo.byId("forcedStatusMenu");
    var selectedChoice = menu.selectedIndex;
    formNode.forcedStatus.value = menu.options[selectedChoice].value;
};

// Clones current reservation except for date/times, changing to the
// create reservation page with those parameters filled in.  This is a
// client-side only method.
oscars.ReservationDetails.cloneReservation = function () {
    var layer2Reservation = true;  // default is layer 2
    var i;
    var tableNode;

    oscars.ReservationCreate.resetFields();
    // copy fields from reservation details form to reservation creation form
    var node = dojo.byId("descriptionReplace");
    dijit.byId("reservationDescription").setValue(node.value);
    node = dojo.byId("bandwidthReplace");
    dijit.byId("bandwidth").setValue(node.value);
    node = dojo.byId("sourceReplace");
    dijit.byId("source").setValue(node.innerHTML);
    node = dojo.byId("destinationReplace");
    dijit.byId("destination").setValue(node.innerHTML);
    // see if path widget on create reservation page is displayed before
    // cloning path
    var pathSectionNode = dojo.byId("authorizedPathDisplay");
    node = dojo.byId("srcVlanReplace");
    if (oscars.Utils.isBlank(node.innerHTML)) {
        layer2Reservation = false;
    }
   /* Removing section below because a) it doesn't work and b) the more common
      case is you almost always don't want it to copy the path. This basically 
      moves the extra clicks from the common case (not specifying path) where 
      you would need to delete the path, to the less common case where you want 
      the exact same path and can just copy-paste.
      
   if (pathSectionNode.style.display != "none") {
        if (layer2Reservation) {
            tableNode = dojo.byId("pathReplace");
        } else {
            tableNode = dojo.byId("path3Replace");
        }
        var tbodyNode = tableNode.firstChild;
        // failed reservation might not have path
        if (tbodyNode) {
            var trNodes = tbodyNode.childNodes;
            var pathStr = "";
            for (i = 0; i < trNodes.length; i++) {
                // get contents of text element in td (hop)
                pathStr += trNodes[i].firstChild.firstChild.data + "\n";
            }
            // set path text area on create reservation page
            var textareaWidget = dijit.byId("explicitPath");
            textareaWidget.setValue(pathStr);
        }
    }
    */
    
    if (layer2Reservation) {
        var srcVlan = node.innerHTML;
        dijit.byId("srcVlan").setValue(node.innerHTML);
        node = dojo.byId("srcTaggedReplace");
        var taggedSrcVlan = dojo.byId("taggedSrcVlan");
        if (node.innerHTML == "true") {
            taggedSrcVlan.selectedIndex = 0;
        } else {
            taggedSrcVlan.selectedIndex = 1;
        }
        node = dojo.byId("destVlanReplace");
        // if source VLAN same as destination VLAN, don't display extra field
        // on reservation creation form
        var cb = dijit.byId("sameVlan");
        // only one
        var nodes = dojo.query(".destVlan");
        if (node.innerHTML == srcVlan) {
            nodes[0].style.display = "none";
            cb.setAttribute('Ded', true);
            dijit.byId("destVlan").setValue("");
        } else {
            nodes[0].style.display = "";
            cb.setAttribute('checked', false);
            dijit.byId("destVlan").setValue(node.innerHTML);
        }
        node = dojo.byId("destTaggedReplace");
        var taggedDestVlan = dojo.byId("taggedDestVlan");
        if (node.innerHTML == "true") {
            taggedDestVlan.selectedIndex = 0;
        } else {
            taggedDestVlan.selectedIndex = 1;
        }
    } else {
        var radioWidget = dijit.byId("layer3");
        radioWidget.setValue(true);
        // show layer 3 parameters
        oscars.ReservationCreate.toggleLayer("layer3");
        node = dojo.byId("sourceIPReplace");
        dijit.byId("srcIP").setValue(node.innerHTML);
        node = dojo.byId("destinationIPReplace");
        dijit.byId("destIP").setValue(node.innerHTML);
        node = dojo.byId("sourcePortReplace");
        dijit.byId("srcPort").setValue(node.innerHTML);
        node = dojo.byId("destinationPortReplace");
        dijit.byId("destPort").setValue(node.innerHTML);
        node = dojo.byId("protocolReplace");
        dijit.byId("protocol").setValue(node.innerHTML);
        node = dojo.byId("dscpReplace");
        dijit.byId("dscp").setValue(node.innerHTML);
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    // set to create reservation tab
    var resvCreatePane = dijit.byId("reservationCreatePane");
    mainTabContainer.selectChild(resvCreatePane);
};

// handles all servlet replies
oscars.ReservationDetails.handleReply = function (responseObject, ioArgs) {
    oscarsState.reservationDetailsEntered = false;
    //console.log("start handleReply");
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    // table cell
    var statusN = dojo.byId("statusReplace");
    if (responseObject.method == "QueryReservation") {
        // reset node which indicates whether layer 2 or layer 3 before
        // applying results of query
        var layerNodes = dojo.query(".layerFields");
        for (var i = 0; i < layerNodes.length; i++) {
            layerNodes[i].innerHTML = "";
        }
        // reset node which indicates there is an errorReport
        var node1 = dojo.byId("errorReportReplace");
        node1.innerHTML = "";
        var refreshButton = dojo.byId("resvRefreshDisplay");
        refreshButton.style.display = "";
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        var node2=dojo.byId("bandwidthReplace");
        node2.value=responseObject["bandwidthReplace"];
        var node3 = dojo.byId("descriptionReplace");
        node3.value=responseObject["descriptionReplace"];

        // for displaying only layer 2 or layer 3 fields
        oscars.ReservationDetails.layerParams(responseObject);
        //oscars.ReservationDetails.errorReportParams(responseObject);
        oscars.ReservationDetails.setDateTimes();
        var reservationDetailsNode = dojo.byId("reservationDetailsDisplay");
        reservationDetailsNode.style.display = "";
    } else if (responseObject.method == "QueryReservationStatus") {
        // set parameter values in form from responseObject if current
        if (oscarsState.statusPoll.gri == responseObject.griReplace) {
            oscars.Form.applyParams(responseObject);
        }
    } else if (responseObject.method == "CancelReservation") {
        statusN.innerHTML="INCANCEL";
    } else if (responseObject.method == "ModifyReservation"){
         statusN.innerHTML="INMODIFY";
    } else if (responseObject.method == "OverrideStatusReservation") {
        var formNode = dijit.byId("reservationDetailsForm").domNode;
        oscars.Form.applyParams(responseObject);
        formNode.forcedStatus.value = "";
    }
    if (responseObject.method != "QueryReservationStatus") {
        var griN = dojo.byId("griReplace");
        oscarsState.statusPoll.restart(statusN.innerHTML, griN.innerHTML);
    }
};

oscars.ReservationDetails.setCurrentGri = function (formNode) {
    var currentGri = dojo.byId("griReplace").innerHTML;
    formNode.gri.value = currentGri;
};

// check modified start and end date and times, and converts hidden form fields
// to seconds
oscars.ReservationDetails.checkDateTimes = function () {
    var msg;
    var startSeconds =
        oscars.DigitalClock.widgetsToSeconds("modifyStartDate",
                                             "modifyStartTime");
    var endSeconds =
        oscars.DigitalClock.widgetsToSeconds("modifyEndDate",
                                             "modifyEndTime");
    // additional checks for legality
    if (startSeconds < 0) {
        msg = "Both start date and time must be specified";
    } else if (endSeconds < 0) {
        msg = "Both end date and time must be specified";
    } else if (startSeconds > endSeconds) {
        msg = "End time is before start time";
    } else if (startSeconds == endSeconds) {
        msg = "End time is the same as start time";
    }
    if (msg) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = msg;
        return false;
    }
    var startSecondsN = dojo.byId("modifyStartSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("modifyEndSeconds");
    endSecondsN.value = endSeconds;
    return true;
};

oscars.ReservationDetails.setDateTimes = function () {
    var secondsN = dojo.byId("modifyStartSeconds");
    oscars.DigitalClock.secondsToWidgets(secondsN.value, "modifyStartDate",
                                         "modifyStartTime");
    secondsN = dojo.byId("modifyEndSeconds");
    oscars.DigitalClock.secondsToWidgets(secondsN.value, "modifyEndDate",
                                         "modifyEndTime");
};

// reset fields which may not be present in new reservation
// chooses which params to display in reservation details page
oscars.ReservationDetails.layerParams = function (responseObject) {
    var i;
    var tableN;
    var n = dojo.byId("srcVlanReplace");
    var layer2Nodes = dojo.query(".layer2Replace");
    var layer3Nodes = dojo.query(".layer3Replace");
    
    // blank tables if not present in new reservation
    if (!responseObject.interPathReplace) {
        tableN = dojo.byId("interPathReplace");
        tableN.innerHTML = "";
    }
    if (!responseObject.vlanInterPathReplace) {
        tableN = dojo.byId("vlanInterPathReplace");
        tableN.innerHTML = "";
    }
    
    //display layer 2 or layer 3 parameters
    if (!oscars.Utils.isBlank(n.innerHTML)) {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = "";
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = "none";
        }
    } else {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = "none";
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = "";
        }
    }
};

// The loop over errorReportNodes didn't work for me.
// I just blanked the whole errorReplaceReport in the servlet -mrt
/*oscars.ReservationDetails.errorReportParams = function (responseObject) {
    var i;
    var tableN;
    var n = dojo.byId("errorReportReplace");
    //var errorReportNodes = dojo.query(".errRepReplace");
    if (!oscars.Utils.isBlank(n.innerHTML)) {
        // blank tables if not present in new reservation
        if (!responseObject.errorReportReplace) {
            tableN = dojo.byId("errorReportReplace");
            tableN.innerHTML = "";
        }
       for (i=0; i < errorReportNodes.length; i++){
            errorReportNodes[i].style.display="";
       }
    }
}
*/

// take action based on this tab's selection
oscars.ReservationDetails.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    oscarsStatus.className = "success";
    if (formNode.gri && formNode.gri.value) {
        oscarsStatus.innerHTML = "Reservation details for " + formNode.gri.value;
    } else {
        oscarsStatus.innerHTML = "Reservation details";
    }
};
