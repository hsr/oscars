/**
 * @author arlake
 * @author aaron@internet2.edu
 * @author kkumar@internet2.edu
 */
dojo.provide("ion");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Dialog");
dojo.require("dijit.ProgressBar");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.HorizontalSlider");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.layout.ContentPane");
dojo.require("dojo.data.ItemFileReadStore");

google.load("visualization", "1", {packages:["areachart"]});
google.load('visualization', '1', {packages:['annotatedtimeline']});

//dojo.registerModulePath("ion", "../../ion");
dojo.registerModulePath("ion", "../../../ion_js");

function navReserveCircuit(){
	wizardCleanup();
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', 'selected');
	dojo.attr(dojo.byId('view'), 'class', '');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	dojo.place('<a href="#" id="sourceStepLink">source</a>', secondaryNav);
	dojo.place('<a href="#" id="destStepLink">destination</a>', secondaryNav);
	dojo.place('<a href="#" id="timeStepLink">time</a>', secondaryNav);
	dojo.place('<a href="#" id="bandwidthStepLink">bandwidth</a>', secondaryNav);
	dojo.place('<a href="#" id="vlanStepLink">vlan</a>', secondaryNav);
	dojo.place('<a href="#" id="summStepLink">create circuit</a>', secondaryNav);
	
	//update content
	dijit.byId("contentDiv").setContent('<div dojoType="ion.CircuitWizard"></div>');
}

function navQueryCircuit(gri){
	hideErrorDiv();
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', '');
	dojo.attr(dojo.byId('view'), 'class', 'selected');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	var curLink = dojo.place('<a href="#" id="current">current</a>', secondaryNav);
	var histLink = dojo.place('<a href="#" id="history">history</a>', secondaryNav);
	var detailsLink = dojo.place('<a href="#" id="resvDetails" class="selected">details</a>', secondaryNav);
	dojo.connect(curLink, "onclick", function(){
		navViewCircuits();
	});
	dojo.connect(histLink, "onclick", function(){
		navViewCircuits("FINISHED, CANCELLED, FAILED");
	});
	dijit.byId("contentDiv").setContent('<div dojoType="ion.CircuitInfo" gri="'+gri+'"></div>');
}

function navViewCircuits(statusString){
	hideErrorDiv();
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', '');
	dojo.attr(dojo.byId('view'), 'class', 'selected');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	dojo.place('<a href="#" id="current">current</a>', secondaryNav);
	dojo.place('<a href="#" id="history">history</a>', secondaryNav);
	
	//update content
	if (statusString == null || statusString == '') {
		dijit.byId("contentDiv").setContent('<div dojoType="ion.CircuitList"></div>');
	}else{
		dijit.byId("contentDiv").setContent('<div dojoType="ion.CircuitList" statuses="'+statusString+'"></div>');
	}
}

function navListUsers(){
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', '');
	dojo.attr(dojo.byId('view'), 'class', 'selected');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	var allLink = dojo.place('<a href="#" id="all" class="selected">all</a>', secondaryNav);
	dojo.connect(allLink, "onclick", function(){
		navListUsers();
	});
	
	dijit.byId("contentDiv").setContent('<div dojoType="ion.UserList"></div>');

}

function navAddUser(){
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', 'selected');
	dojo.attr(dojo.byId('view'), 'class', '');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	
	dijit.byId("contentDiv").setContent('<div dojoType="ion.UserAdd"></div>');
}

function navViewUser(login){
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', '');
	dojo.attr(dojo.byId('view'), 'class', 'selected');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	var allLink = dojo.place('<a href="#" id="all" class="">all</a>', secondaryNav);
	dojo.place('<a href="#" id="curr" class="selected">'+login+'</a>', secondaryNav);
	dojo.connect(allLink, "onclick", function(){
		navListUsers();
	});
	
	dijit.byId("contentDiv").setContent('<div dojoType="ion.UserInfo" username="'+login+'"></div>');
}

function navCloneCircuit(){
	wizardCleanup();
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', 'selected');
	dojo.attr(dojo.byId('view'), 'class', '');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	dojo.place('<a href="#" id="expressStepLink">clone</a>', secondaryNav);
	dojo.place('<a href="#" id="summStepLink">create circuit</a>', secondaryNav);
	
	dijit.byId("contentDiv").setContent('<div dojoType="ion.CircuitWizard" express="true"></div>');
}

function navUserProfile(){
	hideErrorDiv();
	
	//update primary nav
	dojo.attr(dojo.byId('new'), 'class', '');
	dojo.attr(dojo.byId('view'), 'class', '');
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	dojo.query('*', secondaryNav).forEach(dojo.destroy);
	dojo.place('<a href="#" class="selected">my profile</a>', secondaryNav);
	
	dijit.byId("contentDiv").setContent('<div dojoType="ion.UserProfile"></div>');
}

function navAbout(){
	hideErrorDiv();
	
	//update primary nav
	var primaryNav = dojo.byId("primary");
	dojo.query('a', primaryNav).forEach(function(node){
		dojo.attr(node, 'class', '');
	});
	
	//update secondary nav
	var secondaryNav = dojo.byId("secondary");
	if (secondaryNav != null) {
		dojo.query('*', secondaryNav).forEach(dojo.destroy);
	}
	dijit.byId("contentDiv").setHref("forms/about.html");
	
	//DEBUG. TBD remove
	//dialogAlert('DEBUG', '4');
}

//DEBUG AID. TBD Remove/comment out
function dialogAlert(txtTitle, txtContent) {

	var thisdialog = new dijit.Dialog({ title: txtTitle, content: txtContent });
	dojo.body().appendChild(thisdialog.domNode);
	thisdialog.startup();
	thisdialog.show();
}

dojo.declare("ion.CircuitManager", null, {
	queryURL: 'servlet/QueryReservation',
	cancelURL: 'servlet/CancelReservation',
	statusURL: 'servlet/QueryReservationStatus',
	verifyDialog: '',
	cancellingDialog: '',
	verifyYesButton: '',
	verifyNoButton: '',
	successFunction: null,
	constructor: function(args){
		dojo.mixin(this, args);
		this._cancelEvents = [];
	},
	cancelReservation: function(gri, verified){
		if (!verified) {
			for (var i = 0; i < this._cancelEvents.length; i++) {
				dojo.disconnect(this._cancelEvents[i]);
			}
			this._cancelEvents.push(dojo.connect(dojo.byId(this.verifyYesButton), "onclick", 
				dojo.hitch(
					this, 
					this.cancelReservation,
					gri,
					true
				)
			));
			this._cancelEvents.push(dojo.connect(dojo.byId(this.verifyNoButton), "onclick",
				dojo.hitch(this, function(){
					dijit.byId(this.verifyDialog).hide();
				})
			));
			dijit.byId(this.verifyDialog).show();
		}else {
			dijit.byId(this.verifyDialog).hide();
			//sleep for a bit or else the next dialog won't display correctly
			setTimeout(dojo.hitch(this, this.sendCancelReservation, gri), 500);
		}
	},
	sendCancelReservation: function(gri){
		dijit.byId(this.cancellingDialog).show();
		dojo.xhrPost({
			url: this.cancelURL,
			content: {
				"gri": [gri],
			},
			handleAs: "json",
			timeout: "30000",
			load: dojo.hitch(this, this.handleCancelReservation),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleCancelReservation: function(response, ioArgs){
		if(response.success){
			this.queryReservationStatus(response.gri);
		}else{
			this.handleError(response, ioArgs);
		}
	},
	queryReservationStatus: function(gri){
		dojo.xhrPost({
			url: this.statusURL,
			content: {
				"gri": gri,
			},
			handleAs: "json",
			timeout: "30000",
			load: dojo.hitch(this, this.handleQueryReservationStatus),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleQueryReservationStatus: function(response, ioArgs){
		if(response.success){
			//if((response.localStatusReplace & 8) == 8 ||
			//commented above for porting
			if(response.statusReplace == "CANCELLING" ||
				response.statusReplace == "CANCELLED"){
				dijit.byId(this.cancellingDialog).hide();
				if (this.successFunction != null) {
					this.successFunction();
				}
			}else{
				setTimeout(
					dojo.hitch(this, this.queryReservationStatus, 
						response.griReplace), 
					5000
				);
			}
		}else{
			this.handleError(response, ioArgs);
		}
	},
	queryAndCloneReservation: function(gri){
		dojo.xhrPost({
			url: this.queryURL,
			content: {
				"gri": gri,
			},
			handleAs: "json",
			timeout: "30000",
			load: dojo.hitch(this, this.cloneReservation),
			error: dojo.hitch(this, this.handleError)
		});
	},
	cloneReservation: function(reservation){
		this._cloneWizSubcribe = dojo.subscribe("wizStepLoaded",
			dojo.hitch(this, function(){
				if (this._cloneWizSubcribe != null) {
					dojo.unsubscribe(this._cloneWizSubcribe);
					this._cloneWizSubcribe = null;
				}
                                persistent = false;
				if (reservation.descriptionReplace.search(" \[PERSISTENT\]")) {
				   reservation.descriptionReplace = reservation.descriptionReplace.replace(" \[PERSISTENT\]", "");
                                   persistent = true;
                                }
				else {
				}
				dojo.byId('tmpSource').value = reservation.sourceNameReplace;
				dojo.byId('tmpDescription').value = reservation.descriptionReplace;
				dojo.byId('tmpPersistent').value = persistent;
				dojo.byId('tmpDestination').value = reservation.destinationNameReplace;
				dijit.byId('tmpBandwidth').setValue(reservation.bandwidthReplace);
				dijit.byId('tmpSrcVlan').setValue(reservation.srcVlanReplace);
				dijit.byId('tmpDestVlan').setValue(reservation.destVlanReplace);
				dojo.byId('tmpTaggedSrcVlan').selectedIndex = (reservation.srcTaggedReplace=="true"?0:1);
				dojo.byId('tmpTaggedDestVlan').selectedIndex = (reservation.destTaggedReplace=="true"?0:1);
				dijit.byId('wizPersistent').setChecked(persistent);
			}));
		navCloneCircuit();
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		dijit.byId(this.cancellingDialog).hide();
	},
});

dojo.declare("ion.CircuitList", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/circuitList.html"),
	templateString: null,
	widgetsInTemplate: true,
	serviceURL: 'servlet/ListReservations',
	cancelURL: 'servlet/CancelReservation',
	statusURL: 'servlet/QueryReservationStatus',
	page: 0,
	resultsPerPage: 10,
	id: 'circuitList',
	favIcon: 'images/favorite.png',
	prevPageIcon: 'images/previous.png',
	nextPageIcon: 'images/next.png', 
	sortAscIcon: 'images/sort_asc.png',
	sortDescIcon: 'images/sort_desc.png',
	nextPageIcon: 'images/next.png',
	viewEditButton: '',
	cancelButton: '',
	cloneButton: '',
	maxColChars: 20,
	statuses: [],
	currStatuses: ['ACTIVE', 'PENDING', 'INCREATE', 'INSETUP', 'INTEARDOWN', 'INMODIFY'],
	histStatuses: ['FINISHED', 'CANCELLED', 'FAILED'],
	//cancelStatuses: ['ACTIVE', 'PENDING'],
	cancelStatuses: ['ACTIVE', 'PENDING', 'CANCELLING'], //added CANCELLING status
	sortBy: "startTime desc",
	headerLabels: ['ID', 'Fav', 'Description', 'Creator', 'Start', 'End', 'Status'],
	headerFieldNames: ['globalReservationId', 'favorite', 'description', 'login', 'startTime', 'endTime', 'status'],
	postCreate: function(){
		if (this.statuses.length == 0) {
			this.statuses = this.currStatuses;
		}
		
		//init private vars
		this._navEvents = [];
		this._cancelEvents = [];
		this._timeoutJobs = [];
		this._circuitMgr = new ion.CircuitManager({
			cancelURL: this.cancelURL,
			statusURL: this.statusURL,
			verifyDialog: this.id + '_cancelVerifyDialog',
			cancellingDialog: this.id + '_cancellingDialog',
			verifyYesButton:this.id+"_circuitListYesCancel",
			verifyNoButton: this.id+"_circuitListNoCancel",
			successFunction: dojo.hitch(this, this.listReservations)
		});
		
		//connect numResults menu
		dojo.connect(dojo.byId(this.id+"_resultsPerPage_sel"), "onchange", dojo.hitch(this, function(){
			var self = dojo.byId(this.id+"_resultsPerPage_sel");
			this.resultsPerPage = self.options[self.selectedIndex].value;
			this.listReservations();
		}));
		
		//connect refresh button
		dojo.connect(dojo.byId(this.id+"_refreshButton"), "onclick", dojo.hitch(this, this.listReservations));

		this.listReservations();
	},
	listReservations: function(){
		hideErrorDiv();
		dijit.byId(this.id+"_loadingDialog").show();
		this.selectStatusFilter();
		dojo.xhrPost(
			{
				url: this.serviceURL,
				content: {
					"statuses":this.statuses, 
					"page":this.page, 
					"sortBy":this.sortBy,
					"resultsPerPage": this.resultsPerPage
				},
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handleListReservations),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handleListReservations: function(response, ioArgs){
		var tbody = dojo.byId(this.id + "_list_table_body");
		if(tbody == null){
			return;
		}
		
		//create thead
		var sortByParts = this.sortBy.split(" ");
		var headTr = dojo.byId(this.id+'_list_table_head');
		dojo.query('th', headTr).forEach(dojo.destroy);
		for(var i = 0; i < this.headerFieldNames.length; i++){
			var th = dojo.create('th', null, headTr);
			var headerAnchor = dojo.place('<a href="#">'+this.headerLabels[i]+'</a>', th);
			var newSortBy = this.headerFieldNames[i]+' ';
			if(this.headerFieldNames[i] == sortByParts[0]){
				var iconSrc = null;
				if(sortByParts[1] == 'asc'){
					newSortBy += 'desc';
					iconSrc = this.sortAscIcon;
				}else {
					newSortBy += 'asc';
					iconSrc = this.sortDescIcon;
				}
				var sortIcon = dojo.create("img", {'src':iconSrc}, th);
				dojo.connect(sortIcon, "onclick", dojo.hitch(this, 
					this.sortReservations, newSortBy));
			}else{
				newSortBy += sortByParts[1];
			}
			
			dojo.connect(headerAnchor, "onclick", dojo.hitch(this, 
				this.sortReservations, newSortBy));
		}
		//add buton head
		dojo.create('th', null, headTr);
		
		//create tbody
		dojo.query("tr", tbody).forEach(dojo.destroy);
		if (response.resvData) {
			for(var i = 0; i < response.resvData.length; i++){
				var resvDatum = response.resvData[i];
				var trClass = "circuitList"+((((i+1)%2) == 0) ?"Even":"Odd")+"Row";
				var tr = dojo.create('tr', {"class":trClass}, tbody);
				this.createTableCol(formatGRI(resvDatum.gri), tr);
				this.createTableImageCol((resvDatum.favorite ? this.favIcon : null), tr);
				this.createTableCol(resvDatum.description, tr);
				this.createTableCol(resvDatum.user, tr);
				this.createTableCol(formatDate(resvDatum.startTime), tr);
				this.createTableCol(formatDate(resvDatum.endTime), tr);
				//this.createTableNodeCol(formatStatus(resvDatum.status,resvDatum.localStatus), tr);
				//commented above to replace with below
				this.createTableNodeCol(formatStatus(resvDatum.status), tr);
				//add buttons
				var buttonCol = dojo.create('td', null, tr);
				this.createButton(
						buttonCol, 
						this.viewEditButton, 
						"View/Edit", 
						dojo.hitch(this, navQueryCircuit, resvDatum.gri)
						);
				this.createButton(
						buttonCol, 
						this.cloneButton, 
						"Clone", 
						dojo.hitch(this._circuitMgr, 
							this._circuitMgr.queryAndCloneReservation, resvDatum.gri)
						);
				if(dojo.indexOf(this.cancelStatuses, resvDatum.status) != -1){
					this.createButton(
							buttonCol, 
							this.CancelButton, 
							"Cancel", 
							dojo.hitch(this._circuitMgr, this._circuitMgr.cancelReservation, resvDatum.gri, false)
							);
				}else{
					dojo.place(dojo.doc.createTextNode("Cancel "), buttonCol);
				}
			}
		}

		//handle case where there are no reservations
		if(response.resvData == null || response.resvData.length == 0){
			var tr = dojo.create('tr', {"class":trClass}, tbody);
			dojo.place('<td colspan="'+this.headerFieldNames.length+
				'" class="circuitListNoResv">No reservations to display</td>', tr);
		}
		
		if (!response.success && response.status != null) {
			showErrorDiv(response.status);
		}

		//add page navigation
		var pageNavDiv = dojo.byId(this.id+"_pageNav");
		pageNavDiv.innerHTML = "";
		if(response.page > 0){
			var prevDiv = dojo.create('div', {'class':'circuitListPageNav_prev'}, pageNavDiv);
			if(this.prevPageIcon != null && this.prevPageIcon != ''){
				dojo.create('img', {'src':this.prevPageIcon}, prevDiv);
			}
			var prevAnchor = dojo.place('<a href="#">Previous</a>', prevDiv);
			dojo.connect(prevDiv, "onclick", dojo.hitch(this, function(){
				this.page = this.page - 1;
				this.listReservations();
			}));
		}
		if(response.hasNextPage){
			var nextDiv = dojo.create('div', {'class':'circuitListPageNav_next'}, pageNavDiv);
			var nextAnchor = dojo.place('<a href="#">Next</a>', nextDiv);
			dojo.connect(nextDiv, "onclick", dojo.hitch(this, function(){
				this.page = this.page + 1;
				this.listReservations();
			}));
			if(this.nextPageIcon != null && this.nextPageIcon != ''){
				dojo.create('img', {'src':this.nextPageIcon}, nextDiv);
			}
		}
		
		dijit.byId(this.id+"_loadingDialog").hide();
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		dijit.byId(this.id+"_loadingDialog").hide();
		dijit.byId(this.id + '_cancellingDialog').hide();
	},
	createTableCol: function(datum, tr){
		var td = dojo.create('td', null, tr);
		var colStr = datum.substr(0, this.maxColChars);
		if(datum.length > this.maxColChars){
			colStr = datum.substr(0, this.maxColChars-3)+"...";
		}else{
			colStr = datum;
		}
	 	dojo.place(dojo.doc.createTextNode(colStr), td);
	},
	createTableImageCol: function(img, tr){
		var td = dojo.create('td', null, tr);
	 	if (img != null) {
			dojo.place('<img src="' + img + '">', td);
		}
	},
	createTableNodeCol: function(node, tr){
		var td = dojo.create('td', null, tr);
	 	if (node != null) {
			dojo.place(node, td);
		}
	},
	selectStatusFilter: function(){
		var curAnchor = dojo.byId('current');
		var histAnchor = dojo.byId('history');
		
		//clear old event(s)
		if(this._navEvents.length > 0){
			for(var i = 0; i < this._navEvents.length; i++){
				dojo.disconnect(this._navEvents[i]);
			}
			this._navEvents = [];
		}
		
		//Check current
		if(!this._compareArrays(this.statuses, this.currStatuses)){
			dojo.attr(curAnchor, 'class', '');
			this._navEvents.push(dojo.connect(curAnchor, "onclick", dojo.hitch(this, function(){
				this.page = 0;
				this.statuses = this.currStatuses;
				this.listReservations();
			})));
		}else{
			dojo.attr(curAnchor, 'class', 'selected');
		}
		
		//check history
		if(!this._compareArrays(this.statuses, this.histStatuses)){
			dojo.attr(histAnchor, 'class', '');
			this._navEvents.push(dojo.connect(histAnchor, "onclick", dojo.hitch(this, function(){
				this.page = 0;
				this.statuses = this.histStatuses;
				this.listReservations();
			})));
		}else{
			dojo.attr(histAnchor, 'class', 'selected')
		}
	},
	_compareArrays: function(arr1,arr2){
		if(arr1 == arr2){
			return true;
		}
		if(arr1 == null || arr2 == null){
			return false;
		}
		if(arr1.length != arr2.length){
			return false;
		}
		arr1.sort();
		arr2.sort();
		for(var i = 0; i < arr1.length; i++){
			if(arr1[i] != arr2[i]){
				return false;
			}
		}
		return true;
	},
	sortReservations: function(newSortBy){
		this.sortBy = newSortBy;
		this.listReservations();
	},
	createButton: function(buttonCol, img, text, action){
		var buttonNode = null;
		if(img != null && img != ''){
			buttonNode = dojo.create('img', {'src':img}, buttonCol);
		}else{
			buttonNode = dojo.place('<a href="#">'+text+'</a>', buttonCol);
			dojo.place(dojo.doc.createTextNode(' '), buttonCol);
		}
		dojo.connect(buttonNode, "onclick", action);
	}
});

dojo.declare("ion.CircuitInfo", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/circuitInfo.html"),
	templateString: null,
	widgetsInTemplate: true,
	measurementURL: 'servlet/QueryCircuitStatistics',
	serviceURL: 'servlet/QueryReservation',
	statusURL: 'servlet/QueryReservationStatus',
	cancelURL: 'servlet/CancelReservation',
	modifyURL: 'servlet/ModifyReservation',
	favMgrUrl: 'servlet/FavoritesManager',
	gri: '',
	updateInterval: 10000,
	lastQuery: null,
	infoFields: ['gri','source', 'destination','modifyStartSeconds', 
		'modifyEndSeconds', 'bandwidth', 'description', 'srcVlan', 
		'destVlan', 'srcTagged', 'destTagged', 'user', 'status',
		//'localStatus', 'rawPath', 'rawInterPath', 'favorite'],
		//commented above for replacing with below
		'rawPath', 'rawInterPath', 'favorite'],
	postCreate: function(){
		if (this.gri != '') {
			this._init();
		}
	},
	_init: function(){
		//init private variables
		this._circuitMgr = new ion.CircuitManager({
			cancelURL: this.cancelURL,
			statusURL: this.statusURL,
			verifyDialog: 'info_' + this.gri + '_cancelVerifyDialog',
			cancellingDialog: 'info_' + this.gri + '_cancellingDialog',
			verifyYesButton:'info_' + this.gri+"_circuitListYesCancel",
			verifyNoButton: 'info_' + this.gri+"_circuitListNoCancel",
			successFunction: dojo.hitch(this, this.queryReservation)
		});
		
		//connect cancel
		dojo.connect(dojo.byId('info_' + this.gri + '_cancelButton'), 'onclick', 
			dojo.hitch(this._circuitMgr, this._circuitMgr.cancelReservation, this.gri, false));
		
		//connect refresh
		dojo.connect(dojo.byId('info_' + this.gri + '_refreshButton'), 'onclick', 
			dojo.hitch(this, this.queryReservation));
		
		this.queryReservation();
	},
	queryReservation: function(){
		hideErrorDiv();
		if(this._saveChangesEvent != null){
			dojo.disconnect(this._saveChangesEvent);
			this._saveChangesEvent = null;
		}
		dojo.xhrPost({
			url: this.serviceURL,
			content: {"gri":this.gri},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleQueryReservation),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleQueryReservation: function(response, ioArgs){
//TBD remove debug
		if(!response.success){
			this.handleError(response, ioArgs);
		}
		for(var i=0; i < this.infoFields.length; i++){
			var infoFieldName = 'info_'+this.gri+'_'+this.infoFields[i];
console.debug("--info:"+ this.infoFields[i]);
			if(this.infoFields[i] == "modifyStartSeconds" || this.infoFields[i] == "modifyEndSeconds"){
				secondsToWidget(eval("response."+this.infoFields[i]), infoFieldName+'_date',  infoFieldName+'_time');
console.debug("modifyStartSeconds" + this.infoFields[i]);
			}else if(this.infoFields[i] == "srcTagged" || this.infoFields[i] == "destTagged"){
				if(eval("response."+this.infoFields[i]+'Replace') == 'true'){
					dojo.byId(infoFieldName).innerHTML = "Tagged";
				}else{
					dojo.byId(infoFieldName).innerHTML = "Untagged";
				}
console.debug("src/DestTagged" + this.infoFields[i]);
			}else if(this.infoFields[i] == "srcVlan" || this.infoFields[i] == "destVlan"){
console.debug("src/DestVlan" + this.infoFields[i]);
				if(eval("response."+this.infoFields[i]+'Replace').toLowerCase() == "unknown"){
					dojo.byId(infoFieldName).innerHTML = "-";
				}else{
					dojo.byId(infoFieldName).innerHTML = eval("response."+this.infoFields[i]+'Replace');
				}
console.debug("src/DestVlan Done");
			}else if(this.infoFields[i] == "bandwidth"){
				dojo.byId(infoFieldName).innerHTML = formatBandwidth(response.bandwidthReplace);
console.debug("bandwidth" + this.infoFields[i]);

			}else if(this.infoFields[i] == "status"){
				dojo.byId(infoFieldName).innerHTML = formatStatus(
						/* commented below for porting with below
						response.statusReplace,
						response.localStatusReplace */
						response.statusReplace
					);
console.debug("status" + this.infoFields[i]);
	}else if (this.infoFields[i] == "rawInterPath") {
console.debug("started rawInterPath" + this.infoFields[i]);

				//dojo.byId(infoFieldName).innerHTML = outputInterPath(eval("response." + this.infoFields[i]));
				//Evaluate response without calling local method to get domains
				dojo.byId(infoFieldName).innerHTML = eval("response." + this.infoFields[i]);
			}else if(this.infoFields[i] == "rawPath"){
			    gmaps_init();
			    display_map(response.rawPath);
			}else if(this.infoFields[i] == "favorite"){
				dijit.byId('favoriteCheck').setChecked(response.favorite);
				this._favState = response.favorite;
			}else if (this.infoFields[i] == "source" || this.infoFields[i] == "destination") {
				var name = eval("response." + this.infoFields[i] + 'NameReplace');
				if(name != null){
					dojo.byId(infoFieldName).innerHTML = name;
				}else{
					dojo.byId(infoFieldName).innerHTML = formatURN(eval("response." + this.infoFields[i] + 'Replace'), 4);
				}
			}else if(this.infoFields[i] == 'gri'){
				//new for porting . Check to see if griReplace is null to avoid JS error
				if (response.griReplace != null) {
					dojo.byId(infoFieldName).innerHTML = formatGRI(response.griReplace);
				}
			}else if (dojo.byId(infoFieldName) != null) {
				dojo.byId(infoFieldName).innerHTML = eval("response." + this.infoFields[i] + 'Replace');
			}
		}
		printDurationBySecs('info_'+this.gri+'_duration', response.modifyStartSeconds, response.modifyEndSeconds);
		this.lastQuery = response;
		if(this.updateInterval > 0){
			setTimeout(dojo.hitch(this, this.queryReservationStatus), this.updateInterval);
		}
		console.debug("Duration by secs complete");	
		//print any error messages
		if(response.statusReplace == "FAILED"){
			showErrorDiv(response.status);
		}
			
		//set modify values
		this.bandwidth = response.bandwidthReplace;
		this.description = response.descriptionReplace;
		console.debug("Bandwidth and description set" + this.bandwidth +","+ this.description);
		//add clone connection
		if(this._cloneEvent != null){
			dojo.disconnect(this._cloneEvent);
			this._cloneEvent = null;
		}
		this._cloneEvent = dojo.connect(dojo.byId('info_'+this.gri+'_cloneButton'), 'onclick',
			dojo.hitch(this._circuitMgr, this._circuitMgr.cloneReservation, response));
		console.debug("Clone event done");	
		//add start and end time connections
		//this._enableStartEnd(response.statusReplace, response.localStatusReplace);
		this._enableStartEnd(response.statusReplace); //replaced above with this
		dojo.connect(dijit.byId('info_'+this.gri+'_modifyStartSeconds_date'), 'onChange', 
			dojo.hitch(this, this._dateOnChange));
		dojo.connect(dijit.byId('info_'+this.gri+'_modifyStartSeconds_time'), 'onChange', 
			dojo.hitch(this, this._dateOnChange));
		dojo.connect(dijit.byId('info_'+this.gri+'_modifyEndSeconds_date'), 'onChange', 
			dojo.hitch(this, this._dateOnChange));
		dojo.connect(dijit.byId('info_'+this.gri+'_modifyEndSeconds_time'), 'onChange', 
			dojo.hitch(this, this._dateOnChange));
		console.debug("End dates section");
		//add favorite connection
		if(this._favEvent != null){
			dojo.disconnect(this._favEvent);
			this._favEvent = null;
		}
		console.debug("End favorite event");
		this._favEvent = dojo.connect(
			dijit.byId('info_'+this.gri+'_saveButton'), 
			"onClick", 
			dojo.hitch(
				this, 
				this.manageFavorites,
				"favoriteCheck"
			)
		);
		console.debug("Manage favs end");

		this.queryReservationUtilization();
	},
	queryReservationUtilization: function(){
		dojo.xhrPost({
			url: this.measurementURL,
			content: {"gri":this.gri, "locationId":"ingress", "measurementType": "utilization" },
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleQueryReservationUtilization, "ingress"),
			error: dojo.hitch(this, this.handleQueryReservationUtilizationError, "ingress")
		});
		dojo.xhrPost({
			url: this.measurementURL,
			content: {"gri":this.gri, "locationId":"egress", "measurementType": "utilization" },
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleQueryReservationUtilization, "egress"),
			error: dojo.hitch(this, this.handleQueryReservationUtilizationError, "egress")
		});
	},
	handleQueryReservationUtilizationError: function(direction, response, ioArgs){
		   try {
			   if (direction != null) {
				   console.log("got direction: "+direction);
				   // display an empty chart
				   var data = new google.visualization.DataTable();

				   data.addColumn('datetime', 'Time');
				   data.addColumn('number', "In");
				   data.addColumn('number', "Out");

				   var chart_div_name = direction+'_chart_div';
				   var chart_div = dojo.byId(chart_div_name);

                                   // get rid of existing charts
				   dojo.query('*', chart_div).forEach(dojo.destroy);

				   var direction_desc = (direction == "ingress")?"Ingress":"Egress";

				   var formatter = new google.visualization.DateFormat({formatType: 'short'});
				   formatter.format(data, 0);

				   var chart = new google.visualization.AnnotatedTimeLine(chart_div);
				   chart.draw(data, {width: 400, height: 180, legend: 'bottom', max: this.bandwidth, displayAnnotations: false, displayZoomButtons: false, allValuesSuffix: "Mbps", fill: 50 });
			   }
		   } catch (e) {
			   console.log("Error: "+e.message);
		   }

		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
	},
	handleQueryReservationUtilization: function (direction, response, io_args) {
		   if (!response.success) {
			handleQueryReservationError(direction, response, io_args);
			return;
		   }

		   try {
			   var direction = response.locationId;
			   var store = response.data;

			   var data = new google.visualization.DataTable();

			   data.addColumn('datetime', 'Time');
			   data.addColumn('number', "In");
			   data.addColumn('number', "Out");

			   //console.log("draw_utilization_chart(): data table");

			   //console.log("draw_utilization_chart(): store length: "+store.length);

			   for(var i = 0; i < store.length; i++) {
				   //console.log("draw_utilization_chart(): handling store "+i);

				   var ts = new Date();
				   ts.setTime(store[i]["ts"] * 1000);

				   //console.log("draw_utilization_chart(): allocated time: "+store[i]["ts"]);

				   //console.log("draw_utilization_chart(): row(in): "+store[i]["in"]);
				   //console.log("draw_utilization_chart(): row(out): "+store[i]["out"]);

				   var i = data.addRow();

				   //console.log("draw_utilization_chart(): Added blank row");

				   data.setCell(i, 0, ts);
				   //console.log("draw_utilization_chart(): Added ts");
				   data.setCell(i, 1, store[i]["in"]*8/1000/1000);
				   //console.log("draw_utilization_chart(): Added in");
				   data.setCell(i, 2, store[i]["out"]*8/1000/1000);
				   //console.log("draw_utilization_chart(): Added out");

				   //console.log("Adding "+store[i]["ts"]);
			   }

			   chart_div_name = direction+'_chart_div';
			   //console.log("chart_div_name: "+chart_div_name);

			   chart_div = dojo.byId(chart_div_name);

			   if (chart_div == null) {
				   //console.log("Couldn't find what we wanted");
			   }

			   var direction_desc = (direction == "ingress")?"Ingress":"Egress";

                           var formatter = new google.visualization.DateFormat({formatType: 'short'});
			   formatter.format(data, 0);

                           // get rid of existing charts
			   dojo.query('*', chart_div).forEach(dojo.destroy);

//			   var chart = new google.visualization.AreaChart(chart_div);
//			   chart.draw(data, {width: 400, height: 180, legend: 'bottom', title: direction_desc+' Circuit Utilization (Mbps)', max: this.bandwidth });
			   var chart = new google.visualization.AnnotatedTimeLine(chart_div);
			   chart.draw(data, {width: 400, height: 180, legend: 'bottom', max: this.bandwidth, displayAnnotations: false, displayZoomButtons: false, allValuesSuffix: "Mbps", fill: 50 });

			   //console.log("Drawing: "+direction);
		   } catch (e) {
			   console.log("Error: "+e.message);
		   }
	},
	queryReservationStatus: function(){
		dojo.xhrPost({
			url: this.statusURL,
			content: {"gri":this.gri},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleQueryReservationStatus),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleQueryReservationStatus: function(response,ioArgs){
		if (dojo.byId('info_' + this.gri + '_status') != null && response.griReplace == this.gri) {
			dojo.byId('info_' + this.gri + '_status').innerHTML = formatStatus(response.statusReplace);
			//this._enableStartEnd(response.statusReplace, response.localStatusReplace);
			//commented above with below for porting
			this._enableStartEnd(response.statusReplace); 
			//if inmodify still want to set timeout
			if(response.statusReplace == "INMODIFY"){
				dijit.byId('info_'+this.gri+'_modifyingDialog').hide();
			}else if(response.statusReplace == "FAILED"){
				showErrorDiv(response.status);
			}
			
			if(this.updateInterval > 0 && (response.statusReplace == "PENDING" || response.statusReplace.match(/^IN/))){
				setTimeout(dojo.hitch(this, this.queryReservationStatus), this.updateInterval);
			}else{
				dijit.byId('info_'+this.gri+'_modifyingDialog').hide();
			}
		}
	},
	manageFavorites: function(favCheckId){
		var isFav = dijit.byId(favCheckId).checked;
		if(this._favState == isFav){
			//no op
			return;
		}

		dojo.xhrPost({
			url: this.favMgrUrl,
			content: {"gri":this.gri, "favorite": isFav},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleManageFavorite),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleManageFavorite: function(response,ioArgs){
		if (!response.success) {
			this.handleError(response, ioArgs);
		}else{
			this._favState = (!this._favState);
			dijit.byId('info_'+this.gri+'_saveDialog').show();
		}
	},
	_closeFavSave: function(){
		dijit.byId('info_'+this.gri+'_saveDialog').hide();
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		dijit.byId('info_'+this.gri+'_modifyingDialog').hide();
	},
	modifyReservation: function(){
		dijit.byId('info_'+this.gri+'_modifyingDialog').show();
		var startSeconds = widgetToSeconds('info_'+this.gri+'_modifyStartSeconds_date',
			'info_'+this.gri+'_modifyStartSeconds_time');
		var endSeconds = widgetToSeconds('info_'+this.gri+'_modifyEndSeconds_date',
			'info_'+this.gri+'_modifyEndSeconds_time');
		dojo.xhrPost({
			url: this.modifyURL,
			content: {
				"gri": this.gri, 
				"modifyBandwidth": this.bandwidth,
				"modifyDescription": this.description,
				"modifyStartSeconds": startSeconds,
				"modifyEndSeconds": endSeconds
			},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleModifyReservation),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleModifyReservation: function(response, ioArgs){
		if(response.success){
			if(this._saveChangesEvent != null){
				dojo.disconnect(this._saveChangesEvent);
				this._saveChangesEvent = null;
			}
			this.queryReservationStatus();
		}else{
			this.handleError(response, ioArgs);
		}
	},
	setGRI: function(gri){
		this.gri = gri;
		this._init();
	},
	//_enableStartEnd: function(status, localStatus){
	//replacing above with below
	_enableStartEnd: function(status){
		if((status == "PENDING" || status == "ACTIVE") && ( status != "CANCELLING")){
			dijit.byId('info_'+this.gri+'_modifyStartSeconds_date').setDisabled(false);
			dijit.byId('info_'+this.gri+'_modifyStartSeconds_time').setDisabled(false);
			dijit.byId('info_'+this.gri+'_modifyEndSeconds_date').setDisabled(false);
			dijit.byId('info_'+this.gri+'_modifyEndSeconds_time').setDisabled(false);
		}else{
			dijit.byId('info_'+this.gri+'_modifyStartSeconds_date').setDisabled(true);
			dijit.byId('info_'+this.gri+'_modifyStartSeconds_time').setDisabled(true);
			dijit.byId('info_'+this.gri+'_modifyEndSeconds_date').setDisabled(true);
			dijit.byId('info_'+this.gri+'_modifyEndSeconds_time').setDisabled(true);
		}
	},
	_dateOnChange: function(){
		if(this._saveChangesEvent == null){
			this._saveChangesEvent =
				dojo.connect(dojo.byId('info_'+this.gri+'_saveButton'), 'onclick', 
				dojo.hitch(this, this.modifyReservation));
		}
		var start = widgetToSeconds('info_'+this.gri+'_modifyStartSeconds_date', 
			'info_'+this.gri+'_modifyStartSeconds_time');
		var end = widgetToSeconds('info_'+this.gri+'_modifyEndSeconds_date', 
			'info_'+this.gri+'_modifyEndSeconds_time');
		printDurationBySecs('info_'+this.gri+'_duration', start, end);
	}
});

dojo.declare("ion.CircuitWizard", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/circuitWizard.html"),
	templateString: null,
	widgetsInTemplate: true,
	serviceURL: 'servlet/CreateReservation',
	statusURL: 'servlet/QueryReservationStatus',
	sourceFormUrl: dojo.moduleUrl("ion", '../forms/create1_source.html'),
	destFormUrl: dojo.moduleUrl("ion", '../forms/create2_dest.html'),
	timeFormUrl: dojo.moduleUrl("ion", '../forms/create3_time.html'),
	bandwidthFormUrl: dojo.moduleUrl("ion", '../forms/create4_bandwidth.html'),
	vlanFormUrl: dojo.moduleUrl("ion", '../forms/create5_vlan.html'),
	summaryFormUrl: dojo.moduleUrl("ion", '../forms/create6_summary.html'),
	expressFormUrl: dojo.moduleUrl("ion", '../forms/create_express.html'),
	express: false,
	steps: [],
	events: [],
	formFields: ['source', 'destination','startSeconds', 
		'endSeconds', 'bandwidth', 'description', 'srcVlan', 
		'persistentCircuit', 'destVlan', 'taggedSrcVlan', 'taggedDestVlan'],
	postCreate: function(){
		this._visitedSteps = [];
		this.steps = [];
		this.steps.push(new ion.CircuitWizardStep({
			url: this.sourceFormUrl,
			navNode: 'sourceStepLink',
			fields: ["source", "description"],
			localFields: ["tmpSource", "tmpDescription"],
			textFields: ["tmpSource", "tmpDescription"],
			textLabels: ["Source", "Description"],
			
		}));
		this.steps.push(new ion.CircuitWizardStep({
			url: this.destFormUrl,
			navNode: 'destStepLink',
			fields: ["destination"],
			localFields: ["tmpDestination"],
			textFields: ["tmpDestination"],
			textLabels: ["Destination"]
		}));
		this.steps.push(new ion.CircuitWizardStep({
			url: this.timeFormUrl,
			navNode: 'timeStepLink',
			fields: ["startSeconds", "endSeconds", "persistentCircuit"],
			localFields: ["tmpStartSeconds", "tmpEndSeconds", "tmpPersistent"],
			dijitFields: ["startDate", "startTime", "endDate", "endTime"],
			dijitLabels: ["Start Date", "Start Time", "End Date", "End Time", "Persistent Circuit"]
		}));
		this.steps.push(new ion.CircuitWizardStep({
			url: this.bandwidthFormUrl,
			navNode: 'bandwidthStepLink',
			fields: ["bandwidth"],
			localFields: ["tmpBandwidth"],
			textFields: ["tmpBandwidth"],
			textLabels:  ["bandwidth"]
		}));
		this.steps.push(new ion.CircuitWizardStep({
			url: this.vlanFormUrl,
			navNode: 'vlanStepLink',
			fields: ["srcVlan", "destVlan", "taggedSrcVlan", "taggedDestVlan"],
			localFields: ["tmpSrcVlan", "tmpDestVlan", "tmpTaggedSrcVlan", "tmpTaggedDestVlan"],
			dijitFields: ["tmpSrcVlan", "tmpDestVlan"],
			dijitLabels: ["source VLAN", "destination VLAN"]
		}));
		
		if(this.express){
			//if express merge all the other steps 
			var fields = [];
			var localFields = [];
			var dijitFields = [];
			var dijitLabels = [];
			var textFields = [];
			var textLabels = [];
			for(var i = 0; i < this.steps.length; i++){
				fields = fields.concat(this.steps[i].fields);
				localFields = localFields.concat(this.steps[i].localFields);
				dijitFields = dijitFields.concat(this.steps[i].dijitFields);
				dijitLabels = dijitLabels.concat(this.steps[i].dijitLabels);
				textFields = textFields.concat(this.steps[i].textFields);
				textLabels = textLabels.concat(this.steps[i].textLabels);
			}
			this.steps = [];
			this.steps.push(new ion.CircuitWizardStep({
				url: this.expressFormUrl,
				navNode: 'expressStepLink',
				fields: fields,
				localFields: localFields,
				dijitFields: dijitFields,
				dijitLabels: dijitLabels,
				textFields: textFields,
				textLabels: textLabels
			}));
		}
		
		//finally add the summary
		this.steps.push(new ion.CircuitWizardStep({
			url: this.summaryFormUrl,
			navNode: 'summStepLink',
			fields: [],
			localFields: []
		}));
		
		//Load first step
		this.loadStep(0);
	},
	loadStep: function(stepNum){
		if(this.currentStepNum != null){
			var currentStep = this.steps[this.currentStepNum];
			if(!currentStep.validate()){
				return;
			}
			for(var i = 0; i < currentStep.localFields.length; i++){
				if (dojo.byId(currentStep.localFields[i]) != null) {
					dojo.byId(currentStep.fields[i]).value = dojo.byId(currentStep.localFields[i]).value;
				}else{
					dojo.byId(currentStep.fields[i]).value = dijit.byId(currentStep.localFields[i]).getValue();
				}
			}
		}
		//save step as visited 
		if(dojo.indexOf(this._visitedSteps, stepNum) == -1){
			this._visitedSteps.push(stepNum);	
		}
		
		//disconnect old events
		while(this.events.length != 0){
			dojo.disconnect(this.events.pop());
		}
		
		//adjust navigation
		for(var i = 0; i < this.steps.length; i++){
			if(this.steps[i].navNode == null){
				continue;
			}
			if (i > stepNum && dojo.indexOf(this._visitedSteps, i) == -1){
				dojo.attr(dojo.byId(this.steps[i].navNode), 'class', 'unvisited');
			}else if(i < stepNum || (i > stepNum && dojo.indexOf(this._visitedSteps, i) != -1)){
				dojo.attr(dojo.byId(this.steps[i].navNode), 'class', '');
				this.events.push(dojo.connect(dojo.byId(this.steps[i].navNode), 'onclick', dojo.hitch(this, this.loadStep, i)));
			}else{
				dojo.attr(dojo.byId(this.steps[i].navNode), 'class', 'selected');
			}
		}
		
		var step = this.steps[stepNum];
		this.wizDisplayDiv.setHref(step.url);
		this.events.push(dojo.connect(this.wizDisplayDiv, "onDownloadEnd",function(){
			dojo.publish("wizStepLoaded", [])
		}));
		if(stepNum != 0){	
			this.events.push(dojo.connect(this.wizDisplayDiv, "onDownloadEnd", dojo.hitch(this, function(){
				if(dojo.byId(step.backButton) != null){
					dojo.connect(dojo.byId(step.backButton), "onclick", 
						dojo.hitch(this, this.loadStep, stepNum-1));
				}
			})));
		}
		if(stepNum < (this.steps.length-1)){
			this.events.push(dojo.connect(this.wizDisplayDiv, "onDownloadEnd", dojo.hitch(this, function(){
				if(dojo.byId(step.nextButton) != null){
					dojo.connect(dojo.byId(step.nextButton), "onclick", 
						dojo.hitch(this, this.loadStep, stepNum+1));
				}
			})));
		}else{
			//print summary and get ready to reserve the circuit
			this.events.push(dojo.connect(this.wizDisplayDiv, "onDownloadEnd", dojo.hitch(this, function(){
				if(dojo.byId(step.reserveButton) != null){
					dojo.connect(dojo.byId(step.reserveButton), "onclick", 
						dojo.hitch(this, this.createReservation));
				}
				
				for(var i=0; i < this.formFields.length; i++){
					if(this.formFields[i] == "startSeconds" || this.formFields[i] == "endSeconds"){
						secondsToSpan(dojo.byId(this.formFields[i]).value, 'wizSum_'+this.formFields[i]+'_date', 'wizSum_'+this.formFields[i]+'_time');
					}else if(this.formFields[i] == "bandwidth"){
						dojo.byId('wizSum_'+this.formFields[i]).innerHTML = formatBandwidth(eval("this."+this.formFields[i]+'.value'));
					}else if(dojo.byId('wizSum_'+this.formFields[i]) != null){
						dojo.byId('wizSum_'+this.formFields[i]).innerHTML = eval("this."+this.formFields[i]+'.value');
					}
				}
				printDuration();
			})));
		}
		
		this.currentStepNum = stepNum;
	},
	createReservation: function(){
		dijit.byId("wizReservingDialog").show();
                if (dojo.byId("persistentCircuit").value == "true") {
                    dojo.byId("description").value = dojo.byId("description").value + " [PERSISTENT]";
                }
		dojo.xhrPost({
			url: this.serviceURL,
			form: this.formNode,
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleCreateReservation),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleCreateReservation: function(response, ioArgs){
		if(response.success){
			this.queryReservationStatus(response.gri);
		}else{
			this.handleError(response, ioArgs);
		}
	},
	handleError: function(response, ioArgs){
		dijit.byId("wizReservingDialog").hide();
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
	},
	queryReservationStatus: function(gri){
		dojo.xhrPost({
			url: this.statusURL,
			content: {"gri":gri},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleQueryReservationStatus),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleQueryReservationStatus: function(response,ioArgs){
		if(response.statusReplace == "ACCEPTED" || response.statusReplace == "INCREATE"){
			setTimeout(dojo.hitch(this, this.queryReservationStatus, response.griReplace), 10000);
		}else{
			dijit.byId("wizReservingDialog").hide();
			navQueryCircuit(response.griReplace);
		}
	}
});

dojo.declare("ion.CircuitWizardStep", null, {
	url: '',
	fields: [],
	localFields: [],
	nextButton: 'wizNextButton',
	backButton: 'wizBackButton',
	reserveButton: 'wizReserveButton',
	preprocess: [],
	dijitFields: [],
	dijitLabels: [],
	textFields: [],
	textLabels: [],
	navNode: '',
	constructor: function(args){
		dojo.mixin(this, args);
	},
	validate: function(){
		//Check fields validated by dijit
		for(var i = 0; i < this.dijitFields.length; i++){
			if(!dijit.byId(this.dijitFields[i]).isValid()){
				showErrorDiv("Please enter a valid " + this.dijitLabels[i]);
				return false;
			}
		}
		
		//Check required text fields
		for(var i = 0; i < this.textFields.length; i++){
			hideErrorDiv();
			if(dijit.byId(this.textFields[i]) != null){
				if(dijit.byId(this.textFields[i]).getValue().match(/\w/) == null){
					showErrorDiv("Please enter a valid " + this.textLabels[i]);
					return false;
				}
			}else if(dojo.byId(this.textFields[i]).value.match(/\w/) == null){
				showErrorDiv("Please enter a valid " + this.textLabels[i]);
				return false;
			}
		}
		
		return true;
	}
});

dojo.declare("ion.LoginWidget", dijit._Widget, {
	loginFormURL: dojo.moduleUrl("ion", "../forms/templates/loginForm.html"),
	splashPageURL: dojo.moduleUrl("ion", "../forms/splashPage.html"),
	navUrl: dojo.moduleUrl("ion", "../forms/nav.html"),
	verifyURL: '',
	loginURL: '',
	logoutURL: '',
	loginFormId: 'loginForm',
	loginButtonId: 'loginButton',
	contentDiv: 'contentDiv',
	navDiv: 'navDiv',
	admin: false,
	postCreate: function(){
		//connect some initial events
		this._loginButtonEvent = null;
		this._loginFormEvent = null;
		this._signInTabEvent = null;
		dojo.connect(dojo.byId('topSignIn'), "onclick", dojo.hitch(this, this.loadLoginForm));
		//verify session
		this.verifySession();
	},
	verifySession: function(){
		dojo.xhrPost({
			url: this.verifyURL,
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleVerifySess),
			error: dojo.hitch(this, this.handleError)
		});
	},
	login: function(){
		hideErrorDiv();
		dojo.xhrPost({
			url: this.loginURL,
			form: this.loginFormId,
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleLogin),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleVerifySess: function(response, ioArgs){
		if(response.success && this.admin){
			this.loadAdminNav();
			navListUsers();
		}else if(response.success){
			this.loadNav();
			navViewCircuits();
		}else{
			this.loadLoginForm();
		}
	},
	handleLogin: function(response, ioArgs){
		if(response.success && this.admin){
		    this.loadAdminNav();
			navListUsers();
		}else if(response.success){
			this.loadNav();
			navViewCircuits();
		}else {
			this.handleError(response, ioArgs);
		}
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
	},
	loadNav: function(){
		//top-nav
		var topNav = dojo.byId('top-nav');
		dojo.destroy(dojo.byId('topRegister'));
		dojo.destroy(dojo.byId('topSignIn'));
		if(this._loginButtonEvent != null){
			dojo.disconnect(this._loginButtonEvent);
			this._loginButtonEvent = null;
		}
		if(this._loginFormEvent != null){
			dojo.disconnect(this._loginFormEvent);
			this._loginFormEvent = null;
		}
		var topMyProfile = dojo.place('<a href="#" class="cen" id="topMyProfile">my profile</a>', topNav);
		var topSignOut = dojo.place('<a href="#" class="right-end" id="topSignOut">sign out</a>', topNav);
		dojo.connect(topSignOut, "onclick", dojo.hitch(this, this.logout));
		dojo.connect(topMyProfile, 'onclick', navUserProfile);
		
		//primary-nav
		var primaryNav = dojo.byId('primary');
		dojo.query('*', primaryNav).forEach(dojo.destroy);
		var newLink = dojo.place('<a href="#" id="new">Reserve Circuit</a>', primaryNav);
		var viewLink = dojo.place('<a href="#" id="view">View Circuits</a>', primaryNav);
		dojo.connect(newLink, "onclick", navReserveCircuit);
		dojo.connect(viewLink, "onclick", function(){
			navViewCircuits();
		});
		
		//secondary nav
		dojo.create('div', {'id':'secondary'}, dojo.byId('main-nav'));
	},
	loadAdminNav: function(){
		//top-nav
		var topNav = dojo.byId('top-nav');
		dojo.destroy(dojo.byId('topRegister'));
		dojo.destroy(dojo.byId('topSignIn'));
		if(this._loginButtonEvent != null){
			dojo.disconnect(this._loginButtonEvent);
			this._loginButtonEvent = null;
		}
		if(this._loginFormEvent != null){
			dojo.disconnect(this._loginFormEvent);
			this._loginFormEvent = null;
		}
		var topMyProfile = dojo.place('<a href="#" class="cen" id="topMyProfile">my profile</a>', topNav);
		var topSignOut = dojo.place('<a href="#" class="right-end" id="topSignOut">sign out</a>', topNav);
		dojo.connect(topSignOut, "onclick", dojo.hitch(this, this.logout));
		dojo.connect(topMyProfile, 'onclick', navUserProfile);
		
		//primary-nav
		var primaryNav = dojo.byId('primary');
		dojo.query('*', primaryNav).forEach(dojo.destroy);
		var newLink = dojo.place('<a href="#" id="new">Add User</a>', primaryNav);
		var viewLink = dojo.place('<a href="#" id="view">View Users</a>', primaryNav);
		dojo.connect(newLink, "onclick", navAddUser);
		dojo.connect(viewLink, "onclick", navListUsers);
		
		//secondary nav
		dojo.create('div', {'id':'secondary'}, dojo.byId('main-nav'));
	},
	loadLoginForm: function(){
		var tabCount = 0;
		var tabs = dojo.query('.selected', dojo.byId('primary'));
		console.log("tabs: " + tabs.length);
		if(tabs == null || tabs.length == 0){
			dojo.query('a', dojo.byId('primary')).forEach(dojo.hitch(this, function(tab){
				dojo.attr(tab, 'class', 'selected');
			}));
		}
		
		if(this._signInTabEvent == null){
			dojo.query('a', dojo.byId('primary')).forEach(dojo.hitch(this, function(tab){
				this._signInTabEvent = dojo.connect(tab, "onclick", dojo.hitch(this, this.loadLoginForm));
			}));
		}
		
		if(dojo.byId('secondary') != null){
			dojo.destroy(dojo.byId('secondary'));
		}
		hideErrorDiv();
		dijit.byId(this.contentDiv).setHref(this.loginFormURL);
		dojo.connect(dijit.byId(this.contentDiv), "onDownloadEnd", dojo.hitch(this, function(){
			if(dojo.byId(this.loginButtonId) != null){
				if(this._loginButtonEvent != null){
					dojo.disconnect(this._loginButtonEvent);
					this._loginButtonEvent = null;
				}
				if(this._loginFormEvent != null){
					dojo.disconnect(this._loginFormEvent);
					this._loginFormEvent = null;
				}
				this._loginButtonEvent = dojo.connect(dojo.byId(this.loginButtonId), 'onclick', 
					dojo.hitch(this, this.login));
				this._loginFormEvent = dojo.connect(dojo.byId(this.loginFormId), "onsubmit", dojo.hitch(this, function(evt){
					dojo.stopEvent(evt);
					this.login();
				}));
			}
		}));
	},
	logout: function(){
		dojo.xhrPost({
			url: this.logoutURL,
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.handleLogout),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleLogout: function(response, ioArgs){
		//check success
		if(!response.success){
			this.handleError(response, ioArgs);
		}
		
		//top-nav
		var topNav = dojo.byId('top-nav');
		dojo.destroy(dojo.byId('topMyProfile'));
		dojo.destroy(dojo.byId('topSignOut'));
		var regLink = dojo.place('<a href="#" class="cen" id="topRegister">register</a>', topNav);
		var signInLink = dojo.place('<a href="#" class="right-end" id="topSignIn">sign in</a>', topNav);
		dojo.connect(signInLink, "onclick", dojo.hitch(this, this.loadLoginForm));
		
		//primary-nav
		var primaryNav = dojo.byId('primary');
		dojo.query('*', primaryNav).forEach(dojo.destroy);
		var signInTab = dojo.place('<a href="#" class="selected">Sign In</a>', primaryNav);
		dojo.connect(signInTab, "onclick", dojo.hitch(this, this.loadLoginForm));
		
		//secondary nav
		var secondaryNav = dojo.byId('secondary');
		dojo.query('*', secondaryNav).forEach(dojo.destroy);
		
		//set content div
		this.loadLoginForm();
	}
});

dojo.declare("ion.EndpointComboBox", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/endpointComboBox.html"),
	templateString: null,
	widgetsInTemplate: true,
	url: '',
	inputId: '',
	storeType: 'dojo.data.ItemFileReadStore',
	searchAttr: 'name'
});

dojo.declare("ion.EndpointBrowser", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/endpointBrowser.html"),
	templateString: null,
	url: '',
	target: '',
	folderIcon: '',
	closeIcon: '',
	nextPageIcon: '', 
	prevPageIcon: '',
	initialCategory: '',
	categories: ["institution","keywords","name"],
	categoryLabels: ["Organization","Keyword","Name"],
	resultsPerPage: 5,
	postCreate: function(){	
	    //init
	    this._catLinks = [];
	    
		//set custom icons
		if(this.nextPageIcon==null || this.nextPageIcon==''){
			this.nextPageElem = dojo.doc.createTextNode("Next");
		}else{
			this.nextPageElem = dojo.create("img", {"src":this.nextPageIcon});
		}
		if(this.prevPageIcon==null || this.prevPageIcon==''){
			this.prevPageElem = dojo.doc.createTextNode("Previous");
		}else{
			this.prevPageElem = dojo.create("img", {"src":this.prevPageIcon});
		}
		
		//Build category list and browse menu
		for(var i=0; i< this.categories.length; i++){
			var catLabel = this.categories[i];
			if(this.categoryLabels != null && i < this.categoryLabels.length){
				catLabel = this.categoryLabels[i];
			}
			var catLinkDiv = dojo.place("<div class=\"epbBrowseByLink\"></div>", this.browseMenuDiv);
			var catLink = dojo.place("<a href=\"#\">" + catLabel + "</a>", catLinkDiv);
			dojo.connect(catLink, "onclick", dojo.hitch(this,
					this.endpointBrowse, this.categories[i], 0, 
						this.resultsPerPage, 0, null, null));
			dojo.place("<option value=\"" + this.categories[i] + "\">" + 
				catLabel + "</option>", this.searchCatMenu);
			if(this.categories[i] == this.initialCategory){
			    dojo.attr(catLink, 'class', 'selected');
			}
			this._catLinks.push(catLink);
		}
		
		//initially show a category
		if(this.initialCategory != null && this.initialCategory != ''){
			this.endpointBrowse(this.initialCategory, 0, this.resultsPerPage, 0, null, null);
		}
	},
	endpointSearch: function(evt){
		dojo.xhrPost({
			url: this.url,
			form: this.searchForm,
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.printSearchResults),
			error: dojo.hitch(this, this.handleSearchError)
		});
		//for form submits
		dojo.stopEvent(evt);
		return false;
	},
	endpointBrowse: function(cat, page, pageResults, rev, query, catVal){
		dojo.xhrPost({
			url: this.url,
			content: {"cat": cat, "page":page, 
				"pageResults":pageResults, "reverse":rev, 
				"query": query, "catVal":catVal},
			handleAs: "json",
			timeout: 30000,
			load: dojo.hitch(this, this.printSearchResults),
			error: dojo.hitch(this, this.handleSearchError)
		});
	},
	printSearchResults: function (response, ioArgs){
		this.resultsDiv.innerHTML = "";
		this.resultInfo.innerHTML = "";
		
		//check for application errors from server
		if(!response.success){
			this.handleSearchError(response, ioArgs);
		}
		//print info about results
		var resultStart = response.page * response.pageResults + 1;
		var resultEnd = (resultStart + response.pageResults - 1);
		var infoHTML = "";
		if (response.totalResults != 0) {
			infoHTML = "Displaying results " + resultStart + "-";
			if (response.totalResults > resultEnd) {
				infoHTML += resultEnd;
			}else{
				infoHTML += response.totalResults;
			}
			infoHTML += " of " + response.totalResults;
		}
		this.resultInfo.innerHTML = infoHTML;
		
		//create containers
		var showResultsDiv = dojo.create("div", {"class":"epbDisplayResultsDiv"},
			this.resultsDiv);
		var resultsTable = dojo.create("table", {"class":"epbResultsTable"},
			showResultsDiv);
		//add category
		var catTr= dojo.create("tr", null, resultsTable)
		var catTd = dojo.create("td",{"class":"epbResultsCatLabel"}, catTr);
		var catIndex = dojo.indexOf(this.categories, response.cat);
		if (response.query != null) {
			dojo.place(dojo.doc.createTextNode("Search results for '"+response.query+"'"), catTd);
		}else if (this.categoryLabels != null && catIndex != -1 && 
				catIndex < this.categoryLabels.length) {
				
		    //change browse menu
		    for(var j = 0; j < this._catLinks.length; j++){
		        if(j == catIndex){
		            dojo.attr(this._catLinks[j], 'class', 'selected');
		        }else{
		            dojo.attr(this._catLinks[j], 'class', '');
		        }
		    }
			/*var catLink = dojo.place("<a href='#'>"+this.categoryLabels[catIndex]+"</a>", catTd);
			dojo.connect(catLink, "onclick", dojo.hitch(this,
					this.endpointBrowse, response.cat, 0, 
					this.resultsPerPage, response.reverse, null, null));*/
		}else{
			var catLink = dojo.place("<a href='#'>"+response.cat+"</a>", catTd);
			dojo.connect(catLink, "onclick", dojo.hitch(this,
					this.endpointBrowse, response.cat, 0, 
					this.resultsPerPage, response.reverse, null, null));
		}
		if(response.catVal != null){
			dojo.place(dojo.doc.createTextNode(response.catVal), catTd);
		}
		
		//add result rows
		for(var i =0; i < response.results.length; i++){
			var tr= dojo.create("tr", null, resultsTable)
			var td = dojo.create("td",null, tr);
			if(response.catVal == null && response.query == null && 
					response.cat != "name"){
			    //category selection
				if (this.folderIcon != null && this.folderIcon != '') {
					dojo.create("img", {"src":this.folderIcon}, td);
				}
				var resultLink = dojo.place("<a href=\"#\">" + 
					response.results[i].value + "(" + 
					response.results[i].subCount+ ")</a>", td);
				dojo.connect(resultLink, "onclick", 
					dojo.hitch(this, this.endpointBrowse, response.cat, 
						0, response.pageResults, response.reverse, 
						null, response.results[i].value
					)
				);
			}else{
				//selectable endpoint
				var resultLink = dojo.place("<a href=\"#\">" + 
					response.results[i].value + "</a>", td);
				dojo.connect(resultLink, "onclick", 
					dojo.hitch(this, this.selectEndpoint, 
						response.results[i].value
					)
				);
			}
		}
		//if no results
		if(response.results.length == 0){
			var tr= dojo.create("tr", null, resultsTable)
			dojo.place("<td>No results match search</td>", tr);
		}
		
		//print page navigation
		var resultsPageNav = dojo.create("div", 
			{"class":"epbResultsPageNav"}, this.resultsDiv);
		if(response.page != 0){
			var prevLink = dojo.place("<a href=\"#\"></a>", 
				resultsPageNav);
			dojo.place(this.prevPageElem, prevLink);
			dojo.connect(prevLink, "onclick", 
				dojo.hitch(this, this.endpointBrowse, response.cat, 
					(response.page-1), response.pageResults, response.reverse,
					response.query, response.catVal)
			);
			dojo.place(dojo.doc.createTextNode(" "), resultsPageNav);
		}
		for(var i = 1; i <= response.totalPages;i++){
			if((i - 1) == response.page) {
				dojo.place(dojo.doc.createTextNode(i + " "), resultsPageNav);
			}else{
				var pageLink = dojo.place("<a href=\"#\">" + i + "</a>", 
					resultsPageNav);
				dojo.connect(pageLink, "onclick", dojo.hitch(this, 
					this.endpointBrowse, response.cat, (i - 1), 
					response.pageResults, response.reverse,
					response.query, response.catVal));
				dojo.place(dojo.doc.createTextNode(" "), resultsPageNav);
			}
		}
		if(response.page != (response.totalPages - 1) && response.results.length != 0){
			var nextLink = dojo.place("<a href=\"#\"></a>", resultsPageNav);
			dojo.place(this.nextPageElem, nextLink);
			dojo.connect(nextLink, "onclick", 
				dojo.hitch(this, this.endpointBrowse, response.cat, 
					(response.page+1), response.pageResults, response.reverse,
					response.query, response.catVal)
			);
		}
	},
	handleSearchError: function(response, ioArgs){
		if(response.message != null){
			//generic error since 500 errors are confusing
			showErrorDiv("There was an error contacting the " + 
				"service that allows you to browse endpoints. This is a " +
				"server error so please contact the server administrator.");
		}else if(response.status != null){
			//server generated error so should be nicer
			showErrorDiv(response.status);
		}
	},
	closeWindow: function(){
		this.windowDiv.style.visibility = "hidden";
		this.windowDiv.style.display = "none";
	},
	openWindow: function(){
		var coords = dojo.coords(this.browseButton);
		this.windowDiv.style.top = coords.y+"px";
		this.windowDiv.style.left = coords.x+"px";	
		this.windowDiv.style.visibility = "visible";
		this.windowDiv.style.display = "inline";
	},
	selectEndpoint: function(endpointName){
		dijit.byId(this.target).setValue(endpointName);
	},
	setURL: function(newUrl){
		this.url = newUrl;
	},
	setResultsDiv: function(divId){
		this.resultsDiv = dojo.byId(divId);
	},
	setTarget: function(fieldId){
		this.target = dijit.byId(fieldId);
	},
	setFolderIcon: function(imgAttrs){
		this.folderIcon = imgAttrs;
	},
	setNextPageElem: function(elem){
		this.nextPageElem = elem;
	},
	setPrevPageElem: function(elem){
		this.prevPageElem = elem;
	}
});

dojo.declare("ion.ModalBox", dijit.Dialog, {
	postCreate: function(){
    	this.inherited(arguments);
        this.closeButtonNode.style.display = "none";
	}
  }
);

dojo.declare("ion.Button", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/ionButton.html"),
	templateString: null,
	label: ''
});

function initTimeFields(duration, startDateId, startTimeId, endDateId, endTimeId){
	var now = new Date();
	dijit.byId(startDateId).setValue(now);
	var hour = now.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    var minute = now.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
	dijit.byId(startTimeId).setValue(hour + ":" + minute);
	
	now.setSeconds(now.getSeconds() + duration);
	dijit.byId(endDateId).setValue(now);
	hour = now.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    minute = now.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
	dijit.byId(endTimeId).setValue(hour + ":" + minute);
}

function printDuration(){
	var start = 0;
	if(dijit.byId('startDate') != null){
		start = widgetToSeconds('startDate', 'startTime');
	}else{
		start = dojo.byId('startSeconds').value;
	}
	var end = 0;
	if(dijit.byId('endDate') != null){
		end = widgetToSeconds('endDate', 'endTime');
	}else{
		end = dojo.byId('endSeconds').value;
	}
	
	printDurationBySecs('wizDuration', start, end);
}

function printDurationBySecs(outputField, start, end){
	if(!(isNaN(start) || isNaN(end))){
		var duration = end - start;
		var durationString = "";
		while(duration > 0){
			if(duration >= 31536000){
				var quo = Math.floor(duration/31536000);
				durationString += (Math.floor(duration/31536000) + " year");
				durationString += (quo > 1 ? "s " : " ");
				duration %= 31536000;
			}else if(duration >= 86400){
				var quo = Math.floor(duration/86400);
				durationString += (Math.floor(duration/86400) + " day");
				durationString += (quo > 1 ? "s " : " ");
				duration %= 86400;
			}else if(duration >= 3600){
				var quo = Math.floor(duration/3600);
				durationString += (quo + " hour");
				durationString += (quo > 1 ? "s " : " ");
				duration %= 3600;
			}else if(duration >= 60){
				var quo = Math.floor(duration/60);
				durationString += (quo + " minute");
				durationString += (quo > 1 ? "s " : " ");
				duration %= 60;
			}else{
				durationString += duration + " second";
				durationString += (duration > 1 ? "s " : " ");
				duration = 0;
			}
		}
		dojo.byId(outputField).innerHTML = "";
		dojo.create(dojo.doc.createTextNode(durationString), null, dojo.byId(outputField));
	}
}

function widgetToSeconds(dateId, timeId){
	var dateWidget = dijit.byId(dateId);
	var timeWidget = dijit.byId(timeId);
	if(!dateWidget.isValid() || !timeWidget.isValid()){
		return '';
	}
	var seconds = Date.parse(dateWidget.getDisplayedValue() + " " + timeWidget.getValue())/1000;
	return seconds;
}

function secondsToWidget(seconds, dateId, timeId) {
	if (seconds == "") {
		return;
	}
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
}

function secondsToSpan(seconds, dateId, timeId) {
	if (seconds == "") {
		return;
	}
    var jsDate = new Date(seconds*1000);
    var dateWidget = dojo.byId(dateId);
    dateWidget.innerHTML = (jsDate.getMonth()+1)+'/'+ jsDate.getDate()+'/'+ jsDate.getFullYear();
    var hour = jsDate.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    var minute = jsDate.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
    var formattedTime = hour + ":" + minute; 
    var timeWidget = dojo.byId(timeId);
    timeWidget.innerHTML = formattedTime;
}

function formatBandwidth(fieldVal){
	if(fieldVal > 1000){
		fieldVal /= 1000;
		fieldVal += " Gbps";
	}else{
		fieldVal += " Mbps";
	}
	return fieldVal;
}

//function formatStatus(status, localStatus){
//commenting above with below
function formatStatus(status){
	//if ((localStatus & 8) == 8) {
	//replacing above with below
	if (status == 'CANCELLING'){
		status = '<span class="cancellingStatus">CANCELLING...</span>';
	}else if(status == 'PENDING'){
		status = '<span class="reservedStatus">RESERVED</span>';
	}else if(status == 'INSETUP'){
		status = '<span class="buildingStatus">BUILDING...</span>';
	}else if(status == 'INTEARDOWN'){
		status = '<span class="deletingStatus">DELETING...</span>';
	}else if(status == 'INMODIFY'){
		status = '<span class="modifyingStatus">MODIFYING...</span>';
	}else{
		status = ('<span class="'+status.toLowerCase()+'Status">'+status+'</span>');
	}
	
	return status;
}

function outputInterPath(path){
	if(path == null || path.length == 0){
		return "";
	}
	var pathString = '<table class="pathTable">';
	var domainCount = 0;
	var lastURN = "";
	for(var i = 0; i < path.length; i++){
	    var urn = formatURN(path[i], 1);
	    if(urn == lastURN){
	        continue;
	    }
	    lastURN = urn;
		pathString += '<tr class="pathRow' +((domainCount%2)==1?'Even':'Odd')  + '"><td>';
		pathString += urn;
		pathString += '</td></tr>';
		domainCount++;
	}
	pathString += '</table>';
	
	return pathString;
}

function formatURN(urn, maxParts){
	if(!urn.match(/^urn:ogf:network/)){
		return urn;
	}
	urn = urn.replace("urn:ogf:network:", "");
	urn = urn.replace("domain=", "");
	urn = urn.replace("node=", "");
	urn = urn.replace("port=", "");
	urn = urn.replace("link=", "");
	var urnParts = urn.split(":");
	if(urnParts == null || urnParts.length == 0){
		return urn;
	}
	var newUrn = "Domain: " + urnParts[0];
	if(urnParts.length < 2 || maxParts < 2){
		return newUrn;
	}
	newUrn += ", Node: " + urnParts[1];
	if(urnParts.length < 3 || maxParts < 3){
		return newUrn;
	}
	newUrn += ", Port: " + urnParts[2];

	return newUrn;
}

function formatGRI(gri){
	return gri.replace(/.+\-/, '');	
}

function formatDate(date){
	var jsDate = new Date(date*1000);
	var month = jsDate.getMonth()+1;
	month = (month > 9 ? '' : '0') + month;
	var day = jsDate.getDate();
	day = (day > 9 ? '' : '0') + day;
	var hour = jsDate.getHours();
    hour = (hour > 9 ? '' : '0') + hour;
    var minute = jsDate.getMinutes();
    minute = (minute > 9 ? '' : '0') + minute;
	return month+'/'+day+'/'+jsDate.getFullYear()+' '+hour+':'+minute;
}

function defaultErrorMsg(){
	return "Problem with ION. Please contact the server administrator.";
}

function wizardCleanup(){
	//clean out old form elements to prevent dojo error
	dijit.registry.forEach(function(w){
		if(w.id != null && (w.id.match(/^tmp/) || w.id.match(/^wiz/)
			|| w.id == 'startDate' || w.id == 'startTime' 
			|| w.id == 'endDate') || w.id == 'endTime'){
			w.destroyRecursive();
		}
	});
}

function showErrorDiv(msg){
	hideErrorDiv();
	dojo.place('<div id="errorDiv"><div class="errorTop">&nbsp;</div>'+msg+'</div>', dojo.byId('content'), "first");
}

function hideErrorDiv(){
	if(dojo.byId('errorDiv') != null){
		dojo.destroy(dojo.byId('errorDiv'));
	}
}

dojo.declare('ion.UserProfile', [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/userProfile.html"),
	templateString: null,
	widgetsInTemplate: true,
	queryURL: 'servlet/UserQuery',
	modifyURL: 'servlet/UserModify',
	user: '',
	postCreate: function(){
		this.queryUser();
	},
	queryUser: function(){
		dojo.xhrPost({
			url: this.queryURL,
			timeout: 30000,
			handleAs: 'json',
			load: dojo.hitch(this, this.handleUserQuery),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleUserQuery: function(response, ioArgs){
		//new check for displaying correct value
                if(!response.success){
			this.handleError(response,ioArgs);
			return;
		}
		this.userField.innerHTML = response.profileName;
		this.passwdField.value = response.password;
		this.passwdConfirmnField.value = response.passwordConfirmation;
		this.firstNameField.value = response.firstName;
		this.lastNameField.value = response.lastName;
		var inst = "";
		for(var i = 0; i < response.institutionMenu.length; i++){
			if(response.institutionMenu[i] == 'true'){
				this.orgField.innerHTML = inst;
				//institutionNameField is submitted with modify form
				this.institutionNameField.value = inst;
				break;
			}
			inst = response.institutionMenu[i];
		}
		
		var hasAttr=false;
		for(var i = 1; i < response.attributeNameMenu.length; i+=2){
			if(response.attributeNameMenu[i] == 'true'){
				hasAttr = true;
				dojo.create("input",{'name':'attributeName','value':response.attributeNameMenu[i-1],'type':'hidden'}, this.formNode);
			}
		}
		if(!hasAttr){
			dojo.create("input",{'name':'attributeName','value':'None','type':'hidden'}, this.formNode);
		}
		this.emailField.value = "";
		if (response.emailPrimary != null) {
			this.emailField.value = response.emailPrimary;
		}

		this.phoneField.value = "";
		if (response.phonePrimary != null) {
			this.phoneField.value = response.phonePrimary;
		}

		this.certSubjectField.value = "";
		if (response.certSubject != null) {
			this.certSubjectField.value = response.certSubject;
		}

		this.phoneSecondaryField.value = "";
		if (response.phoneSecondary != null) {
			this.phoneSecondaryField.value = response.phoneSecondary;
		}

		this.descriptionField.value = "";
		if (response.description != null) {
			this.descriptionField.value = response.description;
		}

		this.emailSecondaryField.value = "";
		if (response.emailSecondary != null) {
			this.emailSecondaryField.value = response.emailSecondary;
		}
	},
	modifyUser: function(response, ioArgs){
		hideErrorDiv();
		if(!this._validateModify()){
			return;
		}
		this.modDialog.show();
		dojo.xhrPost({
			url: this.modifyURL,
			form: this.formNode,
			timeout: 30000,
			handleAs: 'json',
			load: dojo.hitch(this, this.handleUserModify),
			error: dojo.hitch(this, this.handleError)
		});
	},
	handleUserModify: function(response, ioArgs){
		if(!response.success){
			this.handleError(response, ioArgs);
			return;
		}
		this.modDialog.hide();
		setTimeout(dojo.hitch(this, function(){
			this.modDoneDialog.show();
		}), 500);
	},
	closeDoneDialog: function(){
		this.modDoneDialog.hide();
	},
	_validateModify: function(){
		if(!this._emptyCheck(this.passwdField.value, "a password")){return false;}
		if(!this._emptyCheck(this.passwdConfirmnField.value, "a password confirmation")){return false;}
		if(!this._emptyCheck(this.firstNameField.value, "your first name")){return false;}
		if(!this._emptyCheck(this.lastNameField.value, "your last name")){return false;}
		if(!this._emptyCheck(this.emailField.value, "your email")){return false;}
		if(!this._emptyCheck(this.phoneField.value, "your phone")){return false;}
		if(this.passwdField.value != this.passwdConfirmnField.value){
			showErrorDiv("Password fields do not match");
			return false;
		}
		return true;
	},
	_emptyCheck: function(val, name){
		if(val == null ||
			val.replace(/\s+/,'') == ''){
			showErrorDiv("Please enter " + name);
			return false;
		}
		return true;
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		this.modDialog.hide();
	}
});


/** GMaps functions **/
var topology;
var map;
var gmaps_initialized = false;

function gmaps_init() {
    map = new GMap2(document.getElementById("map_div"));
    map.setCenter(new GLatLng(38, -97), 3);
    map.addControl(new GLargeMapControl());
    gmaps_initialized = true;
    // This is a trick to make sure when the google map resizes, that everything comes out properly
    map._lastCenter=map.getCenter(); 
    GEvent.addListener(map, 'moveend', function() { map._lastCenter=map.getCenter(); }); 
    GEvent.addListener(map, 'resize', function() { map.setCenter(map._lastCenter); }); 

    init_topology();
}

function init_topology() {
    dojo.xhrGet ({ 
        url: "servlet/NodeInfo",
        timeout: 15000,
        handleAs: "json",
        load: handle_init_topology_response,
        sync: true,
        error: function (response, io_args) { alert("Problem loading domain topology") },
    });
}

function handle_init_topology_response(response, io_args) {
    console.log("handle_init_topology_response()");
    topology = response;
}

function display_map(path){
    console.log("start");
    if(path == null || path.length == 0){
		return;
	}
	var lastNode = "";
	var location_src = "";
	var color = "#0000FF";
	
	console.log("display_map.1");
	for(var i = 0; i < path.length; i++){
		if(!path[i].match(/^urn:ogf:network/)){
		    continue;
	    }
	    console.log("display_map.2");
	    path[i] = path[i].replace("urn:ogf:network:", "");
	    path[i] = path[i].replace("node=", "");
	    var urnParts = path[i].split(":");
	    if(urnParts == null || urnParts.length < 2){
		    continue;
	    }
	    var topoElem = topology[urnParts[1]];
	    if(topoElem == null){
		console.log("Didn't find "+urnParts[1]);
		if (lastNode != "") {
			topology[lastNode]["egress"] = 1;
			lastNode = "";
		}
	        continue;
	    }

	    if (!lastNode) {
		topoElem["ingress"] = 1;
	    }

	    var location = new GLatLng(topoElem["latitude"],topoElem["longitude"]);
	    if(lastNode == urnParts[1]){
	        location_src = location;
	        continue;
	    }
	    
            var marker = new GMarker(location);
            marker.value = topoElem;
            map.addOverlay(marker);
	    lastNode = urnParts[1];
	    
	    GEvent.addListener(marker, "click", function(latlng) {
                var topoElem = this.value;
                var host_html = "";
                host_html += "Switch: " + topoElem["name"] + "<br>";
		if (topoElem["ingress"]) {
	                host_html += "Ingress Point";
		}
		if (topoElem["egress"]) {
	                host_html += "Egress Point";
		}
                var tab1 = new GInfoWindowTab("Host Information", host_html);
                var tabs = [tab1];
                map.openInfoWindowTabsHtml(latlng, tabs);
            });

            console.log("1");
            if(location_src == ""){
                location_src = location;
                console.log("2");
            }else{
                var line = new GPolyline([ location_src, location ], color, 3);
                map.addOverlay(line);
                location_src = "";
                console.log("3");
            }

            console.log("4");
	} 

	if (lastNode != "") {
		topology[lastNode]["egress"] = 1;
		lastNode = "";
	}
}

function get_persistent_end_time(start_time) {
	return start_time + 2*365*24*60*60; // 2 years
}
