define([ 'chaplin', 'models/base/collection', 'models/field'], function(Chaplin, Collection, Field) {
	'use strict';

	var Fields = Collection.extend({
		defaults : function() {
			return {
				process: null
			};
		},
		model: Field,
		comparator: 'ordinal',
		url: function() {
			var process = this.get("process");
			if (process == null)
				return "";
			var processDefinitionKey = process.get("processDefinitionKey");
			return 'secure/v1/process/' + processDefinitionKey + '/field';
		},
	});
	return Fields;
});