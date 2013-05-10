define([ 'chaplin', 'models/base/collection', 'models/section'], 
		function(Chaplin, Collection, Section) {
	'use strict';

	var Sections = Collection.extend({
		model: Section,
		comparator: 'ordinal'
	});
	return Sections;
});