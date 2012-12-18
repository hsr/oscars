/*
AuthorizationDetails.js:  Handles authorization details form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
postAdd()
postModify()
postDelete()
close()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, oscarsStatus)
resetFields(useSaved)
validate()
*/

dojo.provide("oscars.AuthorizationDetails");

oscars.AuthorizationDetails.init = function () {
    dojo.xhrPost({
        url: 'servlet/AuthorizationForm',
        handleAs: "json",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authDetailsForm").domNode
    });
};

// posts authorization add to server
oscars.AuthorizationDetails.postAdd = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    if (!oscars.AuthorizationDetails.validate(formNode)) {
        return;
    }
    dojo.xhrPost({
        url: 'servlet/AuthorizationAdd',
        handleAs: "json",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authDetailsForm").domNode
    });
};

oscars.AuthorizationDetails.postModify = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    if (!oscars.AuthorizationDetails.validate(formNode)) {
        return;
    }
    dojo.xhrPost({
        url: 'servlet/AuthorizationModify',
        handleAs: "json",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: formNode
    });
};

// posts delete request to server
oscars.AuthorizationDetails.postDelete = function () {
    var formNode = dijit.byId("authDetailsForm").domNode;
    dojo.xhrPost({
        url: 'servlet/AuthorizationRemove',
        handleAs: "json",
        load: oscars.AuthorizationDetails.handleReply,
        error: oscars.Form.handleError,
        form: formNode
    });
};

// Changes to the add version of the page with the current parameters.
// This is a client-side only method.
oscars.AuthorizationDetails.clone = function () {
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "none";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "";
};

// handles all servlet replies
oscars.AuthorizationDetails.handleReply = function (responseObject, ioArgs) {
    var formNode;
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    // set parameter values in form from responseObject
    oscars.Form.applyParams(responseObject);
    if (responseObject.method == "AuthorizationForm") {
        if (responseObject.rpcData) {
            oscarsState.authorizationState.setRpc(responseObject.rpcData);
        }
        formNode = dijit.byId("authDetailsForm").domNode;
        // ensure server won't send back rpcData again
        formNode.rpc.value = "set";
    }
    if ((responseObject.method != "AuthorizationForm") &&
        (responseObject.method != "AuthorizationAdd")) {
        // after deleting or modifying an authorization, refresh the
        // authorizations list and display that tab
        var pane = dijit.byId("authorizationsPane");
        mainTabContainer.selectChild(pane);
        // must refresh when visible
        oscars.Authorizations.refreshAuthGrid();
    }
    if (responseObject.method != "AuthorizationAdd") {
        oscarsState.authorizationState.clearAuthState();
    } else {
        formNode = dijit.byId("authListForm").domNode;
        formNode.authsAdded.value = "changed";
    }
};

// take action based on this tab's selection
oscars.AuthorizationDetails.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Authorization details";
    var formNode = dijit.byId("authDetailsForm").domNode;
    // attributes list has been changed, need to update
    if (formNode.authAttrsUpdated.value) {
        oscars.AuthorizationDetails.init();
        formNode.authAttrsUpdated.value = "";
    }
};

oscars.AuthorizationDetails.resetFields = function (useSaved) {
    // clear everything
    if (!useSaved) {
        oscarsState.authorizationState.clearAuthState();
    } else {
        oscarsState.authorizationState.recoverAuthState();
    }
};

oscars.AuthorizationDetails.validate = function (formNode) {
    var menu = formNode.constraintName;
    var constraintName = menu.options[menu.selectedIndex].value;
    var oscarsStatus = dojo.byId("oscarsStatus");
    var constraintValue = formNode.constraintValue.value;
    if (constraintName != 'none') {
        if (!constraintValue) {
            oscarsStatus.className = "failure";
            oscarsStatus.innerHTML = "Constraint value must be filled in";
            return false;
        } else {
            menu = formNode.resourceName;
            var resourceName = menu.options[menu.selectedIndex].value;
            menu = formNode.permissionName;
            var permissionName = menu.options[menu.selectedIndex].value;
            var constraintType = 
                oscarsState.authorizationState.getConstraintType(resourceName,
                                                permissionName, constraintName);
            // try converting string to right Javascript type
            if (constraintValue == "true") {
                constraintValue = true;
            } else if (constraintValue == "false") {
                constraintValue = false;
            } else {
                // try converting string to number
                constraintValue = parseInt(constraintValue, 10);
            }
            var newConstraintType = typeof constraintValue;
            if (constraintType == "numeric") {
                constraintType = "number";
            } 
            if (newConstraintType != constraintType) {
                oscarsStatus.className = "failure";
                oscarsStatus.innerHTML = "Constraint value not of type " +
                                         constraintType;
                return false;
            }
        }
    }
    return true;
};
