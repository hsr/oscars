/*
UserProfile.js:  Handles user profile form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
postUserQuery
postUserModify
postUserRemove
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, oscarsStatus)
*/

dojo.provide("oscars.UserProfile");

// posts query to server for user details; only used on form init and on
// selection from user list tab
oscars.UserProfile.postUserQuery = function () {
    dojo.xhrPost({
        url: 'servlet/UserQuery',
        handleAs: "json",
        load: oscars.UserProfile.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userProfileForm").domNode
    });
};

// posts request to server to modify user profile
oscars.UserProfile.postUserModify = function () {
    valid = dijit.byId("userProfileForm").validate();
    if (!valid) {
        return;
    }
    dojo.xhrPost({
        url: 'servlet/UserModify',
        handleAs: "json",
        load: oscars.UserProfile.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userProfileForm").domNode
    });
};

// posts request to server to remove user
oscars.UserProfile.postUserRemove = function () {
    dojo.xhrPost({
        url: 'servlet/UserRemove',
        handleAs: "json",
        load: oscars.UserProfile.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("userProfileForm").domNode
    });
};

// handles servlet replies for user query, modify, and remove requests
oscars.UserProfile.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    if ((responseObject.method == "UserQuery") ||
        (responseObject.method == "UserModify")) {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if (responseObject.method == "UserRemove") {
        // after adding or removing a user, refresh the user list and
        // display that tab
        var userListPane = dijit.byId("userListPane");
        mainTabContainer.selectChild(userListPane);
        oscars.UserList.refreshUserGrid();
    }
};

// take action based on which tab was clicked on
oscars.UserProfile.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    var node = dijit.byId("userProfileForm").domNode;
    // will only be blank if not coming in from user form,
    // and this is the first time the tab has been selected
    if (oscars.Utils.isBlank(node.profileName.value)) {
        node.userInstsUpdated.value = "";
        node.userAttrsUpdated.value = "";
        node.profileName.value = oscarsState.login;
        oscars.UserProfile.postUserQuery();
    } else {
        // if institutions list has been updated, need to update
        // institutions menu
        if ((node.userInstsUpdated.value) || (node.userAttrsUpdated.value)) {
            oscars.UserProfile.postUserQuery();
            node.userInstsUpdated.value = "";
            node.userAttrsUpdated.value = "";
        } else  {
            oscarsStatus.innerHTML = "Profile for user " +
                                      node.profileName.value;
        }
    }
};
