define([ 'chaplin', 'models/base/collection', 'models/design/screen'], function(Chaplin, Collection, Screen) {
	'use strict';

	var Screens = Collection.extend({
		model: Screen,
		comparator: 'ordinal',
	});
	return Screens;
});