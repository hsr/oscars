/*
Reservations.js:   Handles reservations list form, including grid.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postSearch()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, oscarsStatus)
onResvRowSelect(evt)
convertSearchTimes()
convertReservationTimes()
*/

dojo.provide("oscars.Reservations");

// posts request to retrieve reservations from server based on search
// parameters
oscars.Reservations.postSearch = function () {
    // need to do at very beginning or won't catch as often
    if (oscarsState.reservationsEntered) {
        return;
    }
    oscarsState.reservationsEntered = true;
    var valid = dijit.byId("reservationsForm").validate();
    if (!valid) {
        oscarsState.reservationsEntered = false;
        return;
    }
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "inprocess";
    oscarsStatus.innerHTML = "Retrieving reservations...";
    oscars.Reservations.convertSearchTimes();
    dojo.xhrPost({
        url: 'servlet/ListReservations',
        handleAs: "json",
        load: oscars.Reservations.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationsForm").domNode
    });
};

// handles reply from list reservations servlet
oscars.Reservations.handleReply = function (responseObject, ioArgs) {
    // reset that not in process anymore in any event
    oscarsState.reservationsEntered = false;

    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    oscars.Form.applyParams(responseObject);
    var mainTabContainer = dijit.byId("mainTabContainer");
    var resvGrid = dijit.byId("resvGrid");
    // convert seconds to datetime format before displaying
    oscars.Reservations.convertReservationTimes(responseObject.resvData);
    var data = {
        identifier: 'id',
        label: 'id',
        items: responseObject.resvData
    };
    var store =
        new dojo.data.ItemFileWriteStore({data: data});
    resvGrid.setStore(store);
    oscarsState.resvGridInitialized = true;
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Reservations list";
};

// takes action based on this tab being selected
oscars.Reservations.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {

    // refresh reservations grid
    var resvGrid = dijit.byId("resvGrid");
    if (resvGrid && !oscarsState.resvGridInitialized) {
        dojo.connect(resvGrid, "onRowClick",
                     oscars.Reservations.onResvRowSelect);
        oscarsStatus.className = "inprocess";
        oscarsStatus.innerHTML = "Retrieving reservations...";
        dojo.xhrPost({
            url: 'servlet/ListReservations',
            handleAs: "json",
            load: oscars.Reservations.handleReply,
            error: oscars.Form.handleError,
            form: dijit.byId("reservationsForm").domNode
        });
    } else {
        oscarsStatus.className = "success";
        oscarsStatus.innerHTML = "Reservations list";
    }
};

// select reservation based on grid row select
oscars.Reservations.onResvRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var reservationDetailsPane = dijit.byId("reservationDetailsPane");
    var resvGrid = dijit.byId("resvGrid");
    // get reservation's GRI
    var item = evt.grid.selection.getFirstSelected();
    var gri = resvGrid.store.getValues(item, "gri");
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    formNode.gri.value = gri;
    // get reservation details
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
    // Set tab to reservation details.
    // Note that this generates an apparently harmless error message in
    // Firebug console.
    mainTabContainer.selectChild(reservationDetailsPane);
};

// sets hidden form fields' seconds values from search date and time
// constraints in list reservations form
oscars.Reservations.convertSearchTimes = function () {
    var currentDate = new Date();
    var startSeconds = null;
    var endSeconds = null;
    var startDateWidget = dijit.byId("startDateSearch");
    var startTimeWidget = dijit.byId("startTimeSearch");
    // don't do anything if both blank
    if (!(oscars.Utils.isBlank(startDateWidget.getDisplayedValue()) &&
          oscars.Utils.isBlank(startTimeWidget.getValue()))) {
        startSeconds =
            oscars.DigitalClock.convertDateTime(currentDate, "startDateSearch",
                                                "startTimeSearch", false);
    }
    var endDateWidget = dijit.byId("endDateSearch");
    var endTimeWidget = dijit.byId("endTimeSearch");
    if (!(oscars.Utils.isBlank(endDateWidget.getDisplayedValue()) &&
          oscars.Utils.isBlank(endTimeWidget.getValue()))) {
        endSeconds =
            oscars.DigitalClock.convertDateTime(currentDate, "endDateSearch",
                                                "endTimeSearch", false);
    }
    var startSecondsN = dojo.byId("startTimeSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("endTimeSeconds");
    endSecondsN.value = endSeconds;
};

// convert seconds in incoming reservations list to date and time
oscars.Reservations.convertReservationTimes = function (data) {
    for (var i=0; i < data.length; i++) {
        // These fields are sent by the server in epoch seconds
        // Note that if the grid layout changes, the indices need to
        // change.
        data[i].startTime =
            oscars.DigitalClock.convertFromSeconds(data[i].startTime);
        data[i].endTime =
            oscars.DigitalClock.convertFromSeconds(data[i].endTime);
    }
};
