dojo.declare("ion.UserList", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/userList.html"),
	templateString: null,
	widgetsInTemplate: true,
	serviceURL: '../servlet/IONUserList',
	cancelURL: '../servlet/CancelReservation',
	page: 0,
	resultsPerPage: 10,
	id: 'userList',
	prevPageIcon: '../images/previous.png',
	nextPageIcon: '../images/next.png', 
	sortAscIcon: '../images/sort_asc.png',
	sortDescIcon: '../images/sort_desc.png',
	nextPageIcon: '../images/next.png',
	viewEditButton: '',
	cancelButton: '',
	maxColChars: 20,
	sortBy: "login asc",
	headerLabels: ['Login', 'Last Name', 'First Name', 'Organization', 'Email'],
	headerFieldNames: ['login', 'lastName', 'firstName', 'organization', 'email'],
	postCreate: function(){
		//init private vars
		this._navEvents = [];
		this._cancelEvents = [];
		this._timeoutJobs = [];

		//connect numResults menu
		dojo.connect(dojo.byId(this.id+"_resultsPerPage_sel"), "onchange", dojo.hitch(this, function(){
			var self = dojo.byId(this.id+"_resultsPerPage_sel");
			this.resultsPerPage = self.options[self.selectedIndex].value;
			this.listUsers();
		}));

		this.listUsers();
	},
	listUsers: function(){
		hideErrorDiv();
		dijit.byId(this.id+"_loadingDialog").show();
		dojo.xhrPost(
			{
				url: this.serviceURL,
				content: {
					"page":this.page, 
					"sortBy":this.sortBy,
					"resultsPerPage": this.resultsPerPage
				},
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handlelistUsers),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handlelistUsers: function(response, ioArgs){
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
		for(var i = 0; i < response.userData.length; i++){
			var userDatum = response.userData[i];
			var trClass = "userList"+((((i+1)%2) == 0) ?"Even":"Odd")+"Row";
			var tr = dojo.create('tr', {"class":trClass}, tbody);
			this.createTableCol(userDatum.login, tr);
			this.createTableCol(userDatum.lastName, tr);
			this.createTableCol(userDatum.firstName, tr);
            this.createTableCol(userDatum.organization, tr);
            this.createTableCol(userDatum.email, tr);
            
			//add buttons
			var buttonCol = dojo.create('td', null, tr);
			this.createButton(
				buttonCol, 
				this.viewEditButton, 
				"View/Edit", 
				dojo.hitch(this, navViewUser, userDatum.login)
			);
		}
		//handle case userData there are no reservations
		if(response.userData.length == 0){
			var tr = dojo.create('tr', {"class":trClass}, tbody);
			dojo.place('<td colspan="'+this.headerFieldNames.length+
				'" class="userListNoResv">No users to display</td>', tr);
		}
		
		//add page navigation
		var pageNavDiv = dojo.byId(this.id+"_pageNav");
		pageNavDiv.innerHTML = "";
		if(response.page > 0){
			var prevDiv = dojo.create('div', {'class':'userListPageNav_prev'}, pageNavDiv);
			if(this.prevPageIcon != null && this.prevPageIcon != ''){
				dojo.create('img', {'src':this.prevPageIcon}, prevDiv);
			}
			
			var prevAnchor = dojo.place('<a href="#">Previous</a>', prevDiv);
			dojo.connect(prevDiv, "onclick", dojo.hitch(this, function(){
				this.page = this.page - 1;
				this.listUsers();
			}));
		}
		
		if(response.hasNextPage){
			var nextDiv = dojo.create('div', {'class':'userListPageNav_next'}, pageNavDiv);
			var nextAnchor = dojo.place('<a href="#">Next</a>', nextDiv);
			dojo.connect(nextDiv, "onclick", dojo.hitch(this, function(){
				this.page = this.page + 1;
				this.listUsers();
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
	sortReservations: function(newSortBy){
		this.sortBy = newSortBy;
		this.listUsers();
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

dojo.declare("ion.UserInfo", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/userInfo.html"),
	templateString: null,
	widgetsInTemplate: true,
	serviceURL: '../servlet/IONUserQuery',
	deleteURL: '../servlet/IONUserDelete',
	modifyURL: '../servlet/IONUserModify',
	id: 'userInfo',
	username: '', 
	postCreate: function(){
		this.queryUser(this.username);
	},
	queryUser: function(){
		hideErrorDiv();
		dijit.byId(this.id+"_loadingDialog").show();
		dojo.xhrPost(
			{
				url: this.serviceURL,
				content: {
					"user":this.username, 
				},
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handleQueryUser),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handleQueryUser: function(response, ioArgs){
	    if ( (response.success !=undefined) && !response.success) {
		this.handleErrorString(response.status);
		return;
	    }
	    this.userSpan.innerHTML = response.login;
	    this.userField.value = response.login;
	    this.passwdField.value = "********";
	    this.passwdConfirmField.value = "********";
	    this.certSubjectField.value = response.certSubject;
	    this.firstNameField.value = response.firstName;
	    this.lastNameField.value = response.lastName;
	    this.institutionNameField.value = response.organization;
	    this.emailField.value = response.emailPrimary;
	    this.emailSecondaryField.value = response.emailSecondary;
	    this.phoneField.value = response.phonePrimary;
	    this.phoneSecondaryField.value = response.phoneSecondary;
	    this.descriptionField.value = response.description;
	    if (response.attributes != null) {
	    	for(var i = 0; i < response.attributes.length; i++){
	    		if(response.attributes[i] == "OSCARS-user"){
	    			this.userRoleField.checked = true;
	    		}else if(response.attributes[i] == "ION-administrator"){
	    			this.adminRoleField.checked = true;
	    		}else if(response.attributes[i] == "OSCARS-site-administrator"){
	    			this.engineerRoleField.checked = true;
	    		}else if(response.attributes[i] == "ION-operator"){
	    			this.operatorRoleField.checked = true;
	    		}
	    	}
	    }
	    dijit.byId(this.id+"_loadingDialog").hide();
	},
	verifyDelete: function(){
		dijit.byId(this.id+"_deleteVerifyDialog").show();
	},
	closeVerifyDelete: function(){
		dijit.byId(this.id+"_deleteVerifyDialog").hide();
	},
	deleteUser: function(){
	    hideErrorDiv();
		dijit.byId(this.id+"_deletingDialog").show();
		dojo.xhrPost(
			{
				url: this.deleteURL,
				content: {
					"user": this.username
				},
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handleUserDelete),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handleUserDelete: function(response, ioArgs){
		dijit.byId(this.id+"_deletingDialog").hide();
	    if(response.success){
	    	dijit.byId(this.id+"_deleteSuccessDialog").show();
	    }else{
	    	this.handleError(response, ioArgs);
	    }
	},
	handleDeleteSuccess: function(){
		dijit.byId(this.id+"_deleteSuccessDialog").hide();
		navListUsers();
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		dijit.byId(this.id+"_loadingDialog").hide();
		dijit.byId(this.id + '_deletingDialog').hide();
	},
	handleErrorString: function(responseStr) { 
                if(responseStr != null){
                        showErrorDiv(responseStr);
                }else{
                        showErrorDiv(defaultErrorMsg());
                }
                dijit.byId(this.id+"_loadingDialog").hide();
                dijit.byId(this.id + '_deletingDialog').hide();
        },
	modifyUser: function(){
	    hideErrorDiv();
		dijit.byId(this.id+"_modifyingDialog").show();
		dojo.xhrPost(
			{
				url: this.modifyURL,
				form: this.formNode,
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handleUserModify),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handleUserModify: function(response, ioArgs){
		dijit.byId(this.id+"_modifyingDialog").hide();
		if(response.success){
	    	dijit.byId(this.id+"_modifySuccessDialog").show();
	    }else{
	    	this.handleError(response, ioArgs);
	    }
	},
	closeModifySuccess: function(){
		dijit.byId(this.id+"_modifySuccessDialog").hide();
	}
});

dojo.declare("ion.UserAdd", [dijit._Widget, dijit._Templated], {
	templatePath: dojo.moduleUrl("ion", "../forms/templates/userAdd.html"),
	templateString: null,
	widgetsInTemplate: true,
	serviceURL: '../servlet/IONUserAdd',
	id: 'userAdd',
	username: '', 
	addUser: function(){
		hideErrorDiv();
		dijit.byId(this.id+"_loadingDialog").show();
		dojo.xhrPost(
			{
				url: this.serviceURL,
				form: this.formNode,
				handleAs: "json",
				timeout: "30000",
				load: dojo.hitch(this, this.handleUserAdd),
				error: dojo.hitch(this, this.handleError)
			}
		);
	},
	handleUserAdd: function(response, ioArgs){
	    if(response.success){
	    	navViewUser(response.login);
	    }else{
	    	this.handleError(response, ioArgs);
	    }
	},
	handleError: function(response, ioArgs){
		if(response.status != null){
			showErrorDiv(response.status);
		}else{
			showErrorDiv(defaultErrorMsg());
		}
		dijit.byId(this.id+"_loadingDialog").hide();
	},
	deleteUser: function(){
	    return;
	},
	modifyUser: function(){
	    return;
	}
});
