/*
DigitalClock.js:  Handles date and time conversions, as well as the main
                  clock display in the status bar, and the clock displays
                  on the create reservation form.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
updateClocks()
initClock(clock)
convertFromSeconds(seconds)
secondsToWidgets(seconds, dateId, timeId)
convertDateTime(jsDate, dateId, timeId, useCurrent)
convertDateWidget(jsDate, dateId)
convertTimeWidget(jsDate, timeId, useCurrent)
*/

dojo.provide("oscars.DigitalClock");

monthName = ['January', 'February', 'March', 'April', 'May',
   'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// Outputs datetime with format: July 1, 2005 13:00 in main clock.
// Updates default times on create reservation form page.
oscars.DigitalClock.updateClocks = function (clock) {
    var localDate = new Date();
    var ms = localDate.getTime();
    var month = localDate.getMonth();
    var year = localDate.getFullYear();
    var formattedDt = monthName[month] + " " + localDate.getDate() +
                   ", " + year + " ";

    var formattedTime = localDate.getHours() + ":";
    var minute = localDate.getMinutes();
    formattedTime += (minute > 9 ? '' : '0') + minute;
    clock.innerHTML = formattedDt + formattedTime;
    // update default times on create reservation form
    var startDateDefault = dojo.byId('startDateDefault');
    // page not loaded yet
    if (!startDateDefault) {
        return;
    }
    var startTimeDefault = dojo.byId('startTimeDefault');
    formattedDt = month + 1 + "/" + localDate.getDate() + "/" + year;
    startDateDefault.innerHTML = formattedDt;
    startTimeDefault.innerHTML = formattedTime;
    var endDateDefault = dojo.byId('endDateDefault');
    var endTimeDefault = dojo.byId('endTimeDefault');
    // get default end time (15 minutes in future)
    var endDate = new Date(ms + 60*15*1000);
    month = endDate.getMonth();
    formattedDt = month + 1 + "/" + endDate.getDate() + "/" +
                  endDate.getFullYear();
    formattedTime = endDate.getHours() + ":";
    minute = endDate.getMinutes();
    formattedTime += (minute > 9 ? '' : '0') + minute;
    endDateDefault.innerHTML = formattedDt;
    endTimeDefault.innerHTML = formattedTime;
};

// Init and updateClocks are adapted from the DHTML Utopia book.
oscars.DigitalClock.initClock = function () {
    var clock = dojo.byId('clock');

    oscars.DigitalClock.updateClocks(clock);
    // set up timer for update
    setInterval(function() { oscars.DigitalClock.updateClocks(clock); }, 60000);
};

// Following methods are variants of converting back and forth between
// seconds and either a string or a widget value.
//
// Outputs datetime with format: 1/1/2007 13:00, given seconds since epoch.
oscars.DigitalClock.convertFromSeconds = function (seconds) {
    var jsDate = new Date(seconds*1000);
    // put in format that is sortable
    var year = jsDate.getFullYear();
    var month = jsDate.getMonth() + 1;
    month = (month > 9 ? '' : '0') + month;
    var day = jsDate.getDate();
    day = (day > 9 ? '' : '0') + day;
    var hour = jsDate.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    var minute = jsDate.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
    var formattedDt = year + "/" + month + "/" + day + " " + hour +
                      ":" + minute; 
    return formattedDt;
};

// Given date and time widgets, converts to seconds since epoch.
// Blank widget values are illegal.
oscars.DigitalClock.widgetsToSeconds = function (dateId, timeId) {
    var dateWidget = dijit.byId(dateId);
    var timeWidget = dijit.byId(timeId);
    if  (oscars.Utils.isBlank(dateWidget.getDisplayedValue())) {
        return -1;
    }
    if (oscars.Utils.isBlank(timeWidget.getValue())) {
        return -1;
    }
    var dateFields = {};
    var fields = dateWidget.getDisplayedValue().split("/");
    dateFields.month = fields[0]-1;
    dateFields.day = fields[1];
    dateFields.year = fields[2];
    var timeFields = {};
    fields = timeWidget.getValue().split(":");
    timeFields.hour = fields[0];
    timeFields.minute = fields[1];
    //console.log("year: " + dateFields.year + ", month: " + dateFields.month + ", day: " + dateFields.day + ", hour: " + timeFields.hour + ", minute: " + timeFields.minute);
    var finalDate =
        new Date(dateFields.year, dateFields.month, dateFields.day,
                 timeFields.hour, timeFields.minute, 0, 0);
    var seconds = finalDate.getTime()/1000;
    return seconds;
};

// Fills in date and time widgets, given seconds since epoch.
oscars.DigitalClock.secondsToWidgets = function (seconds, dateId, timeId) {
    var jsDate = new Date(seconds*1000);
    var dateWidget = dijit.byId(dateId);
    dateWidget.setValue(jsDate);
    var hour = jsDate.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    var minute = jsDate.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
    var formattedTime = hour + ":" + minute; 
    var timeWidget = dijit.byId(timeId);
    timeWidget.setValue(formattedTime);
};

// Converts from date and time widget to seconds since epoch.  If either
// is blank, that portion of the current time is used.
oscars.DigitalClock.convertDateTime = function (jsDate, dateId, timeId,
                                                useCurrent) {
    // contains seconds, and any error message
    var dateFields = oscars.DigitalClock.convertDateWidget(jsDate, dateId);
    var timeFields = oscars.DigitalClock.convertTimeWidget(jsDate, timeId, 
                                                           useCurrent);
    var seconds;
    //console.log("year: " + dateFields.year + ", month: " + dateFields.month + ", day: " + dateFields.day + ", hour: " + timeFields.hour + ", minute: " + timeFields.minute);
    var finalDate =
        new Date(dateFields.year, dateFields.month, dateFields.day,
                 timeFields.hour, timeFields.minute, 0, 0);
    seconds = finalDate.getTime()/1000;
    return seconds;
};

// If the date widget is blank, returns the Javascript Date fields for the
// current date.  Otherwise, return the fields given the widget.
oscars.DigitalClock.convertDateWidget = function (jsDate, dateId) {
    var dateFields = {};
    var dateWidget = dijit.byId(dateId);
    if  (oscars.Utils.isBlank(dateWidget.getDisplayedValue())) {
        dateFields.year = jsDate.getFullYear();
        dateFields.month = jsDate.getMonth();
        dateFields.day = jsDate.getDate();
        dateWidget.setValue(jsDate);
    } else {
        var year = jsDate.getFullYear();
        var fields = dateWidget.getDisplayedValue().split("/");
        dateFields.month = fields[0]-1;
        dateFields.day = fields[1];
        dateFields.year = fields[2];
    }
    return dateFields;
};

// If the time widget is blank, returns the Javascript Date fields for the
// current time.  Otherwise, return the fields given the widget.
oscars.DigitalClock.convertTimeWidget = function (jsDate, timeId, useCurrent) {
    var timeFields = {};
    var timeWidget = dijit.byId(timeId);
    // if day of year filled in, but time isn't, use current time
    if (oscars.Utils.isBlank(timeWidget.getValue())) {
        // either use the current time, or midnight
        if (useCurrent) {
            timeFields.hour = jsDate.getHours();
            timeFields.minute = jsDate.getMinutes();
            if (timeFields.minute >= 10) {
                timeWidget.setValue(timeFields.hour + ":" + timeFields.minute);
            } else {
                timeWidget.setValue(timeFields.hour + ":0" + timeFields.minute);
            }
        } else {
            timeFields.hour = 0;
            timeFields.minute = 0;
            timeWidget.setValue("00:00");
        }
    } else {
        var fields = timeWidget.getValue().split(":");
        timeFields.hour = fields[0];
        timeFields.minute = fields[1];
    }
    return timeFields;
};
