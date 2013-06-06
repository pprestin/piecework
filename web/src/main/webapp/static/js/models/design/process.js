define([ 'chaplin', 'models/base/model', 'models/design/interactions', 'models/design/screens' ],
		function(Chaplin, Model, Interactions, Screens) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				processDefinitionLabel: "",
				processSummary : "",
				participantSummary: "",
				ordinal: 0,
				interactions: new Interactions(),
				created: new Date()
			};
		},
		idAttribute: 'processDefinitionKey',
		parse: function(response, options) {
			if (response == null)
				return;
			
//			var interactions = response["interactions"];
//			if (interactions === undefined) {
//				response["interactions"] = new Interactions();
//			} else if (interactions instanceof Array) {
//				for (var i=0;i<interactions.length;i++) {
//					interactions[i].screens = new Screens(interactions[i].screens);
//				}
//				
//				response["interactions"] = new Interactions(interactions);
//			}
			
			return response;
		},
		urlRoot: 'process',
		url: function() {
			var uri = this.get("uri");
			if (uri == null)
				return Model.__super__.url.apply(this);
			return uri;
		},
		isNew: function() {
		      return this.id == null || this.id == '';
		},
	});
	return Process;
});