define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Field = Model.extend({
		defaults : function() {
			return {
				name : "",
				type: "",
				required : false,
				value: "",
				ordinal: 1,
				process: null,
				created : new Date()
			};
		},
		url: function() {
			var process = this.get("process");
			if (process == null)
				return "";
			var processDefinitionKey = process.get("processDefinitionKey");
			return 'process/' + processDefinitionKey + '/field/' + this.id;
		},
	});
	return Field;
});