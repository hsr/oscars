/*
Form.js:        General form handling for browser interface.  Functionality
                specific to a single form is in its own module.
                Note that all security is enforced on the server side.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
handleError(responseObject, ioArgs)
resetStatus(responseObject);
applyParams(responseObject)
selectedChanged(contentPaneWidget)
initBackForwardState()
*/

dojo.provide("oscars.Form");

oscars.Form.handleError = function (responseObject, ioArgs) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "failure";
    oscarsStatus.innerHTML = responseObject.message +
          ".  If it is a servlet problem, contact an admin to restart the Web server.";
};

// handles resetting status message if any, and checking for valid reply 
oscars.Form.resetStatus = function (responseObject) {
    var oscarsStatus = dojo.byId("oscarsStatus");
    if (!responseObject.method) {
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = "Invalid servlet reply: no method returned; " +
                                 "contact administrator";
        return false;
    }
    if (responseObject.success === undefined) {
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = "Invalid servlet reply: no success status " +
                                 "returned; contact administrator";
        return false;
    }
    if (responseObject.status) {
        if (responseObject.success) {
            oscarsStatus.className = "success";
        } else {
            oscarsStatus.className = "failure";
        }
        oscarsStatus.innerHTML = responseObject.status;
        if (!responseObject.success && responseObject.status) {
            // Special error cases where user should go back to initial
            // login page; all reply handlers must immediately return when
            // this method returns false, so there will be no further
            // processing.
            if (responseObject.status.match(/^Your\slogin\ssession/)) {
                oscars.UserLogout.handleLogout();
            }
        }
    }
    if (!responseObject.success) {
        return false;
    }
    return true;
};

// NOTE:  Depends on naming convention agreements between client and server.
// Parameter names ending with Enable, Display, Replace, Menu and TimeConvert
// are treated differently than other names, which are treated as widget ids.
// Note that  widget id's of "method", "status", and "succeed" will mess
// things up, since they are parameter names used by handleReply.
oscars.Form.applyParams = function (responseObject) {
    for (var param in responseObject) {
      if (responseObject.hasOwnProperty(param)) {
        var n = dojo.byId(param);
        //console.log(param);
        //console.log(n);
        var cb;
        var opt;
        var selected;
        var result;
        var i = 0;
        // will currently only work for inputs with disabled property
        if ((result = param.match(/(\w+)Enable$/i))) {
            var enableNode = dojo.byId(result[1]);
            if (enableNode && (enableNode.disabled !== 'undefined')) {
                enableNode.disabled = !responseObject[param];
            }
        // not currently used
        } else if (param.match(/Checkboxes$/i)) {
            var disabled = false;
            // first search to see if checkboxes can be modified
            for (cb in responseObject[param]) {
                if (cb == "modify") {
                    if (!responseObject[param][cb]) {
                        disabled = true;
                        break;
                    }
                }
            }
            // set checkbox attributes
            for (cb in responseObject[param]) {
                if (responseObject[param].hasOwnProperty(cb)) {
                    // get check box
                    var w = dijit.byId(cb);
                    if (w) {
                        if (responseObject[param][cb]) {
                            w.setAttribute('checked', true);
                        } else {
                            w.setAttribute('checked', false);
                        }
                        w.setAttribute('disabled', disabled);
                    }
                }
            }
        // if info for a group of menu options
        } else if ((result = param.match(/(\w+)Menu$/i))) {
            var newMenu = dojo.byId(result[1]);
            if (newMenu) {
                if (responseObject[param] instanceof Array) {
                    newMenu.options.length = 0;
                    for (i=0; i < responseObject[param].length; i += 2) {
                        // list is of type string on server
                        if (responseObject[param][i+1] == "true") {
                            selected = true;
                        } else {
                            selected = false;
                        }
                        //console.log(responseObject[param][i]);
                        //console.log(selected);
                        opt = new Option(responseObject[param][i],
                                         responseObject[param][i],
                                         selected, selected);
                        newMenu.add(opt, null);
                    }
                }
            }
        } else if (param.match(/Display$/i)) {
            if (n) {
                n.style.display= responseObject[param] ? "" : "none";
            }
        } else if (param.match(/TimeConvert$/i)) {
            if (n) {
                n.innerHTML = oscars.DigitalClock.convertFromSeconds(
                                                        responseObject[param]);
            }
        } else if (!n) {
            continue;
        // if info to replace div section with; must be existing div with that
        // id
        } else if (param.match(/Replace$/i)) {
            n.innerHTML = responseObject[param];
        // set widget value
        } else {
            n.value = responseObject[param];
        }   
      }
    }
};

// take action based on which tab was clicked on
oscars.Form.selectedChanged = function (/* ContentPane widget */ contentPane) {
    var mainTabContainer;
    // start of back/forward button functionality
    var state = {
        back: function() {
        },
        forward: function() {
        }
    };
    var oscarsStatus = dojo.byId("oscarsStatus");
    var n;
    // selected reservations tab
    if (contentPane.id == "reservationsPane") {
        oscars.Reservations.tabSelected(contentPane, oscarsStatus);
    // selected reservation details tab
    } else if (contentPane.id == "reservationDetailsPane") {
        oscars.ReservationDetails.tabSelected(contentPane, oscarsStatus);
    // selected create reservation tab
    } else if (contentPane.id == "reservationCreatePane") {
        oscars.ReservationCreate.tabSelected(contentPane, oscarsStatus);
    // selected user details tab
    } else if (contentPane.id == "userProfilePane") {
        oscars.UserProfile.tabSelected(contentPane, oscarsStatus);
    // selected user list tab
    } else if (contentPane.id == "userListPane") {
        oscars.UserList.tabSelected(contentPane, oscarsStatus);
    // selected add user tab
    } else if (contentPane.id == "userAddPane") {
        oscars.UserAdd.tabSelected(contentPane, oscarsStatus);
    // selected institutions management tab
    } else if (contentPane.id == "institutionsPane") {
        oscars.Institutions.tabSelected(contentPane, oscarsStatus);
    // selected attributes management tab
    } else if (contentPane.id == "attributesPane") {
        oscars.Attributes.tabSelected(contentPane, oscarsStatus);
    // selected authorization list tab
    } else if (contentPane.id == "authorizationsPane") {
        oscars.Authorizations.tabSelected(contentPane, oscarsStatus);
    // selected authorization details tab
    } else if (contentPane.id == "authDetailsPane") {
        oscars.AuthorizationDetails.tabSelected(contentPane, oscarsStatus);
    // selected login/logout tab
    } else if (contentPane.id == "sessionPane") {
        oscars.UserLogin.tabSelected(contentPane, oscarsStatus);
    }
};

oscars.Form.setMenuSelected = function (menu, optionName) {
    for (var i=0; i < menu.options.length; i++) {
        if (menu.options[i].value == optionName) {
            menu.options[i].selected = true;
            break;
        }
    }
};

oscars.Form.initBackForwardState = function () {
    var mainTabContainer;
    // callbacks handle back/forward button functionality
    var state = {
        back: function() { },
        forward: function() { }
    };
    dojo.back.setInitialState(state);
};

