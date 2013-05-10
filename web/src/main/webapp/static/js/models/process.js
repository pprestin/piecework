define([ 'chaplin', 'models/base/model', 'models/interactions' ], function(Chaplin, Model, Interactions) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				processLabel: "",
				processSummary : "",
				participantSummary: "",
				ordinal: 0,
				interactions: new Interactions(),
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