define([ 'chaplin', 'models/base/model', 'models/screens' ], function(Chaplin, Model, Screens) {
	'use strict';

	var Interaction = Model.extend({
		defaults : function() {
			return {
				label : "",
				ordinal: 1,
				screens: new Screens(),
				created : new Date()
			};
		},
		parse: function(response, options) {
			var screens = response["screens"];
			if (screens === undefined) {
				response["screens"] = new Screens();
			} else if (screens instanceof Array) {
				response["screens"] = new Screens(screens);
			}
			
			return response;
		},
		urlRoot: function() {
			return 'secure/v1/interaction/' + this.get('processDefinitionKey');
		},
		url: function() {
			var uri = this.get("uri");
			if (uri == null) 
				return Model.__super__.url.apply(this);
			return uri;
		},
	});
	return Interaction;
});