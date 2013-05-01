define([ 'chaplin', 'models/base/collection', 'models/process'], function(Chaplin, Collection, Process) {
	'use strict';

	var Processes = Collection.extend({
		model: Process,
		comparator: 'created'
	});
	return Processes;
});