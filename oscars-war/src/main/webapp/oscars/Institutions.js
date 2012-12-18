/*
Institutions.js:     Handles form for institutions table.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
manage(opName)
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, oscarsStatus)
createInstitutionGrid()
*/

dojo.provide("oscars.Institutions");

oscars.Institutions.manage = function (opName) { 
    var valid;
    var oscarsStatus = dojo.byId("oscarsStatus");
    var formNode = dijit.byId("institutionsForm").domNode;
    var choiceType = dojo.byId("institutionOpChoice");
    var editWidget = dijit.byId("institutionEditName");
    var addButton = dijit.byId("institutionAddButton").domNode;
    var saveButton = dijit.byId("institutionSaveButton").domNode;
    var deleteButton = dijit.byId("institutionDeleteButton").domNode;
    if (opName == "add") {
        addButton.style.color = "#FF0000";
        saveButton.style.color = "#00FF00";
        deleteButton.style.color = "#FF0000";
        editWidget.required = false;
        valid = dijit.byId("institutionsForm").validate();
        formNode.saveName.value = "";
        formNode.institutionEditName.value = "";
        choiceType.innerHTML = "Adding";
    } else if (opName == "delete") {
        editWidget.required = true;
        valid = dijit.byId("institutionsForm").validate();
        if (!valid) {
            return;
        } 
        dojo.xhrPost({
            url: 'servlet/Institutions?op=delete',
            handleAs: "json",
            load: oscars.Institutions.handleReply,
            error: oscars.Form.handleError,
            form: formNode
        });
        formNode.saveName.value = "";
        choiceType.innerHTML = "";
    } else if (opName == "save") {
        editWidget.required = true;
        valid = dijit.byId("institutionsForm").validate();
        if (!valid) {
            return;
        } 
        if (!formNode.saveName.value) {
            dojo.xhrPost({
                url: 'servlet/Institutions?op=add',
                handleAs: "json",
                load: oscars.Institutions.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        } else {
            dojo.xhrPost({
                url: 'servlet/Institutions?op=modify',
                handleAs: "json",
                load: oscars.Institutions.handleReply,
                error: oscars.Form.handleError,
                form: formNode
            });
        }
        formNode.saveName.value = "";
        choiceType.innerHTML = "";
    }
};

// handles reply from request to server to operate on Institutions table
oscars.Institutions.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var institutionGrid = dijit.byId("institutionGrid");
    var data = {
        identifier: 'id',
        label: 'id',
        items: responseObject.institutionData
    };
    var store = new dojo.data.ItemFileWriteStore({data: data});
    institutionGrid.setStore(store);
    institutionGrid.setSortIndex(0, true);
    institutionGrid.sort();
    oscarsState.institutionGridInitialized = true;
    var formNode = dijit.byId("institutionsForm").domNode;
    formNode.institutionEditName.value = "";
    var addButton = dijit.byId("institutionAddButton").domNode;
    var saveButton = dijit.byId("institutionSaveButton").domNode;
    var deleteButton = dijit.byId("institutionDeleteButton").domNode;
    addButton.style.color = "#000000";
    saveButton.style.color = "#000000";
    deleteButton.style.color = "#000000";
    if (responseObject.method != "InstitutionList") {
        formNode = dijit.byId("userProfileForm").domNode;
        formNode.userInstsUpdated.value = "changed";
        // doesn't exist until user add tab first clicked on
        if (dijit.byId("userAddForm")) {
            formNode = dijit.byId("userAddForm").domNode;
            formNode.userAddInstsUpdated.value = "changed";
        }
    }
    if (responseObject.method == "InstitutionModify") {
        var listFormNode = dijit.byId("userListForm").domNode;
        listFormNode.userListInstsUpdated.value = "changed";
    }
};

// take action based on this tab being selected
oscars.Institutions.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "Institutions Management";
    var institutionGrid = dijit.byId("institutionGrid");
    if (institutionGrid && (!oscarsState.institutionGridInitialized)) {
        dojo.connect(institutionGrid, "onRowClick",
                oscars.Institutions.onRowSelect);
        oscars.Institutions.createInstitutionGrid();
    }
};

// create initial institution list from servlet
oscars.Institutions.createInstitutionGrid = function () {
    dojo.xhrPost({
        url: 'servlet/Institutions?op=list',
        handleAs: "json",
        load: oscars.Institutions.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("institutionsForm").domNode
    });
};

// select name based on row select in grid
oscars.Institutions.onRowSelect = function (/*Event*/ evt) {
    var institutionGrid = dijit.byId("institutionGrid");
    // get institution name
    var item = evt.grid.selection.getFirstSelected();
    var institutionName = institutionGrid.store.getValues(item, "name");
    var formNode = dijit.byId("institutionsForm").domNode;
    formNode.institutionEditName.value = institutionName;
    formNode.saveName.value = institutionName;
    var choiceType = dojo.byId("institutionOpChoice");
    choiceType.innerHTML = "Selected";
    var addButton = dijit.byId("institutionAddButton").domNode;
    var saveButton = dijit.byId("institutionSaveButton").domNode;
    var deleteButton = dijit.byId("institutionDeleteButton").domNode;
    addButton.style.color = "#FF0000";
    saveButton.style.color = "#00FF00";
    deleteButton.style.color = "#00FF00";
};
