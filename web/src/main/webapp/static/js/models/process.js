define([ 'chaplin', 'models/base/model', 'models/screens' ], function(Chaplin, Model, Screens) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				processLabel: "",
				processSummary : "",
				participantSummary: "",
				ordinal: 0,
				screens: new Screens(),
				created: new Date()
			};
		},
		idAttribute: 'processDefinitionKey',
		urlRoot: 'secure/v1/process',
		isNew: function() {
		      return this.id == null || this.id == '';
		},
	});
	return Process;
});