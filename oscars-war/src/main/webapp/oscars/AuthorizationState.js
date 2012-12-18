/*
AuthorizationState.js:  Class handling state associated with authorizations
                        forms
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
setRpc()
saveAuthState(attributeName, resourceName, permissionName, constraintName,
              constraintValue)
recoverAuthState()
clearAuthState()
constraintChoices(menuName)
setConstraintType(resourceName, permissionName, constraintName)
*/

dojo.provide("oscars.AuthorizationState");

dojo.declare("oscars.AuthorizationState", null, {
    constructor: function(){
        this.rpcData = {};
    },

    // Not all combinations of these triplets are permissible.  For example,
    // choose a resource may disable some options in the permissions menu.
    setRpc: function(rpcGrid) {
        for (var i=0; i < rpcGrid.length; i++) {
            var resource = rpcGrid[i][0];
            var permission = rpcGrid[i][1];
            var constraint = rpcGrid[i][2];
            var constraintType = rpcGrid[i][3];
            if (!this.rpcData[resource]) {
                this.rpcData[resource] = {};
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = constraintType;
            } else if (!this.rpcData[resource][permission]) {
                this.rpcData[resource][permission] = {};
                this.rpcData[resource][permission][constraint] = constraintType;
            } else if (!this.rpcData[resource][permission][constraint]) {
                this.rpcData[resource][permission][constraint] = 
                    constraintType;
            }
        }
        //console.dir(this.rpcData);
    },

    // Called when clicking on row in authorizations list; saves initial
    // authorization state for possible recovery later.  Sets authorization
    // details form state as well.
    saveAuthState: function(attributeName, resourceName, permissionName,
                           constraintName, constraintValue) {
        var deleteWidget = dijit.byId("deleteAuthorization");
        deleteWidget.setAttribute("disabled", false);
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.permissionName = permissionName;
        this.constraintName = constraintName;
        this.constraintValue = constraintValue;
        var formNode = dijit.byId("authDetailsForm").domNode;
        formNode.oldAuthAttributeName.value = attributeName;
        formNode.oldResourceName.value = resourceName;
        formNode.oldPermissionName.value = permissionName;
        formNode.oldConstraintName.value = constraintName;
        this.setOptionChoices();
        this.constrainChoices("resourceName", false);
    }, 

    // Resets authorization details form to initial state using saved values,
    // and resets other form state to the beginning.
    recoverAuthState: function() {
        var deleteWidget = dijit.byId("deleteAuthorization");
        deleteWidget.setAttribute("disabled", false);
        this.setOptionChoices();
        this.constrainChoices("resourceName", false);
    },

    // Clears all authorization state to default values,
    // and resets other form state to the beginning.
    clearAuthState: function() {
        var deleteWidget = dijit.byId("deleteAuthorization");
        deleteWidget.setAttribute("disabled", false);
        var formNode = dijit.byId("authDetailsForm").domNode;
        var menu = formNode.authAttributeName;
        this.attributeName = menu.options[0].value;
        menu = formNode.resourceName;
        this.resourceName =  menu.options[0].value;
        menu = formNode.permissionName;
        this.permissionName = menu.options[0].value;
        menu = formNode.constraintName;
        this.constraintName = menu.options[0].value;
        this.constraintValue = "";
        formNode.oldAuthAttributeName.value = "";
        formNode.oldResourceName.value = "";
        formNode.oldPermissionName.value = "";
        formNode.oldConstraintName.value = "";
        this.setOptionChoices();
        this.constrainChoices("resourceName", false);
    },

    setOptionChoices: function() {
        var formNode = dijit.byId("authDetailsForm").domNode;
        var menu = formNode.authAttributeName;
        oscars.Form.setMenuSelected(menu, this.attributeName);
        menu = formNode.resourceName;
        oscars.Form.setMenuSelected(menu, this.resourceName);
        menu = formNode.permissionName;
        oscars.Form.setMenuSelected(menu, this.permissionName);
        menu = formNode.constraintName;
        oscars.Form.setMenuSelected(menu, this.constraintName);
        formNode.constraintValue.value = this.constraintValue;
    },

    // Constrains menu and button choices based on current menu chosen.
    constrainChoices: function(menuName, modified) {
        var i;
        var val;
        var legalChoice;
        var illegalChoice;
        var formNode;
        if (modified) {
            var deleteWidget = dijit.byId("deleteAuthorization");
            deleteWidget.setAttribute("disabled", true);
        }
        if (menuName == "authAttributeName") {
            return;
        }
        formNode = dijit.byId("authDetailsForm").domNode;
        var resourceMenu = formNode.resourceName;
        var permissionMenu = formNode.permissionName;
        var constraintMenu = formNode.constraintName;
        var resourceName = resourceMenu.options[resourceMenu.selectedIndex].value;
        var permissionName =
            permissionMenu.options[permissionMenu.selectedIndex].value;
        var constraintName =
            constraintMenu.options[constraintMenu.selectedIndex].value;
        // constrain permissions menu
        if (menuName == "resourceName") {
            legalChoice = -1;
            illegalChoice = -1;
            // can't disable selected index, so need to reset if the
            // current one is no longer valid
            for (i=0; i < permissionMenu.options.length; i++) {
                val = permissionMenu.options[i].value;
                if (this.rpcData[resourceName][val]) {
                    if (legalChoice < 0) {
                        legalChoice = i;
                    }
                } else {
                    if (permissionMenu.selectedIndex == i) {
                        illegalChoice = i;
                    }
                }
            }
            // pick first legal value
            if (illegalChoice > -1) {
                permissionMenu.selectedIndex = legalChoice;
                permissionName = permissionMenu.options[legalChoice].value;
            }
            for (i=0; i < permissionMenu.options.length; i++) {
                val = permissionMenu.options[i].value;
                if (this.rpcData[resourceName][val]) {
                    permissionMenu.options[i].disabled = false;
                } else {
                    permissionMenu.options[i].disabled = true;
                }
            }
        }
        // constrain constraints menu
        if ((menuName == "resourceName") ||
             (menuName == "permissionName")) {
            legalChoice = -1;
            illegalChoice = -1;
            for (i=0; i < constraintMenu.options.length; i++) {
                val = constraintMenu.options[i].value;
                if (this.rpcData[resourceName][permissionName].hasOwnProperty(val)) {
                    if (legalChoice < 0) {
                        legalChoice = i;
                    }
                } else {
                    if (constraintMenu.selectedIndex == i) {
                        illegalChoice = i;
                    }
                }
            }
            if (illegalChoice > -1) {
                constraintMenu.selectedIndex = legalChoice;
                constraintName = constraintMenu.options[legalChoice].value;
            }
            for (i=0; i < constraintMenu.options.length; i++) {
                val = constraintMenu.options[i].value;
                if (this.rpcData[resourceName][permissionName].hasOwnProperty(val)) {
                    constraintMenu.options[i].disabled = false;
                } else {
                    constraintMenu.options[i].disabled = true;
                }
            }
        }
        // constraint constraints value and type
        if ((menuName == "resourceName") ||
             (menuName == "permissionName") ||
             (menuName == "constraintName")) {
            var constraintValueNode = formNode.constraintValue;
            var constraintTypeNode = dojo.byId("constraintType");
            if (constraintName == 'none') {
                constraintValueNode.value = "";
                constraintValueNode.disabled = true;
                constraintTypeNode.disabled = true;
                constraintTypeNode.innerHTML = "";
            } else {
                constraintValueNode.disabled = false;
                this.setConstraintType(resourceName, permissionName, constraintName);
            }
        }
    },

    setConstraintType: function(resourceName, permissionName, constraintName) {
        var constraintTypeNode = dojo.byId("constraintType");
        // consistency check
        if (!this.rpcData[resourceName] ||
            !this.rpcData[resourceName][permissionName] ||
            !this.rpcData[resourceName][permissionName].hasOwnProperty(constraintName)) {
            var oscarsStatus = dojo.byId("oscarsStatus");
            oscarsStatus.className = "failure";
            oscarsStatus.innerHTML = "Triplet resource: " +
                resourceName +
                ", permission: " + permissionName +
                ", constraint: " + constraintName + " not allowed." +
                "Contact an admin.";
            constraintTypeNode.innerHTML = "";
            return;
        } 
        var constraintType =
            this.rpcData[resourceName][permissionName][constraintName];
        if (constraintName != 'none') {
            constraintTypeNode.innerHTML = constraintType;
        } else {
            constraintTypeNode.innerHTML = "";
        }
    },

    getConstraintType: function(resourceName, permissionName, constraintName) {
        return this.rpcData[resourceName][permissionName][constraintName];
    }
});
