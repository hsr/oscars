/*
StatusPoll.js:  Class handling updating status and path of a reservation on
                the reservation details page.
David Robertson (dwrobertson@lbl.gov)
*/

/* Methods:
stop()
restart()
*/

dojo.provide("oscars.StatusPoll");

dojo.declare("oscars.StatusPoll", null, {
    constructor: function(){
        this.intervalId = null;
        this.pollInterval = 15000;  // check every 15 seconds
        this.currentStatusTime = 0;
        // maximum time in a given status before exit (5 minutes)
        this.maxStatusTime = 300000;
        this.currentStatus = "";
        this.gri = "";
        // perform poll
        this._poll = function() {
            var status = dojo.byId("statusReplace").innerHTML;
            if (status != oscarsState.statusPoll.currentStatus) {
                if ((status == "FINISHED") || (status == "FAILED") ||
                    (status == "CANCELLED") || (status == "ACTIVE") ||
                    (status == "RESERVED")){
                    oscarsState.statusPoll.stop();
                    if (status == "FAILED") {
                        var oscarsStatus = dojo.byId("oscarsStatus");
                        oscarsStatus.className = "failure";
                    }
                    return;
                }
                oscarsState.statusPoll.currentStatusTime = 0;
                oscarsState.statusPoll.currentStatus = status;
            } else {
                oscarsState.statusPoll.currentStatusTime +=
                    oscarsState.statusPoll.pollInterval;
                if (oscarsState.statusPoll.currentStatusTime >
                    oscarsState.statusPoll.maxStatusTime) {
                    oscarsState.statusPoll.stop();
                    return;
                }
            }
            dojo.xhrPost({
                url: 'servlet/QueryReservationStatus',
                handleAs: "json",
                load: oscars.ReservationDetails.handleReply,
                error: oscars.Form.handleError,
                form: dijit.byId("reservationDetailsForm").domNode
            });
        };
    },

    // stop any currently scheduled polling
    stop: function() {
        if (this.intervalId !== null) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
        this.currentStatusTime = 0;
        this.currentStatus = "";
    },

    // restart polling
    restart: function(status, gri) {
        this.stop();
        // if in terminal state
        if ((status == "FINISHED") || (status == "FAILED") ||
            (status == "CANCELLED")) {
            return;
        }
        this.gri = gri;
        this.intervalId = setInterval(this._poll, this.pollInterval);
    }
});
