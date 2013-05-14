define([ 'chaplin', 'models/base/collection', 'models/screen'], function(Chaplin, Collection, Screen) {
	'use strict';

	var Screens = Collection.extend({
		model: Screen,
		comparator: 'ordinal',
	});
	return Screens;
});