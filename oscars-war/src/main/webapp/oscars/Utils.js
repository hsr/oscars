/*
Utils.js:       Miscellaneous utilities for browser interface.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
isBlank(str)
*/

dojo.provide("oscars.Utils");

// From Javascript book, p. 264

// check to see if no parameter set
oscars.Utils.isBlank = function (str) {
    if (!str) {
        return true;
    }
    for (var i = 0; i < str.length; i++) {
        var c = str.charAt(i);
        if ((c != ' ') && (c != '\n') && c) { return false; }
    }
    return true;
};

// Don't allow none and other options to be selected at once in a menu
// with multiple selections permitted.  Used by user add and user profile form.
oscars.Utils.constrainAttributeChoices = function (menuName) {
    var i;
    var menu = dojo.byId(menuName);
    if (menu.selectedIndex === 0) {
        for (i=1; i < menu.options.length; i++) {
            menu.options[i].selected = false;
        }
    } else {
        menu.options[0].selected = false;
    }
};
