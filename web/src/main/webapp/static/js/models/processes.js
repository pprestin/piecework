define([ 'chaplin', 'models/base/collection', 'models/process'], function(Chaplin, Collection, Process) {
	'use strict';

	var Processes = Collection.extend({
		model: Process,
		comparator: 'ordinal',
		url: 'secure/v1/process',
		parse: function(response, options) {
			return response.list;
		},
	});
	return Processes;
});