dojo.provide("ion");

dojo.declare("ion.DemoLoginWidget", ion.LoginWidget, {
	loginFormURL: dojo.moduleUrl("ion", "templates/demoLoginForm.html")
});

function submitEmail(){
	dojo.xhrPost({
		url: "servlet/EmailCollector",
		content: {
			"email": dojo.byId('email').value,
		},
		handleAs: "json",
		timeout: "30000",
		load: function(){return;},
		error: function(){return;},
	});
}