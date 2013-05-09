define([ 'chaplin', 'models/base/model', 'models/screens' ], function(Chaplin, Model, Screens) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
//				processDefinitionKey : "",
				processLabel: "",
				processSummary : "",
				participantSummary: "",
				screens: new Screens(),
				created : new Date()
			};
		},
		idAttribute: 'processDefinitionKey',
		urlRoot: 'secure/v1/process',
		isNew: function() {
		      return this.id == null || this.id == '';
		},
//		url: function() {
//			return this.urlRoot + '/' + this.attributes.processDefinitionKey;
//		}
	});
	return Process;
});