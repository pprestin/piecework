define([ 'chaplin', 'models/base/collection', 'models/interaction'], function(Chaplin, Collection, Interaction) {
	'use strict';

	var Interactions = Collection.extend({
		model: Interaction,
		comparator: 'ordinal'
	});
	return Interactions;
});