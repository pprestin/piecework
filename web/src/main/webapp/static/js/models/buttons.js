define([ 'chaplin', 'models/base/collection', 'models/button'], function(Chaplin, Collection, Button) {
	'use strict';

	var Buttons = Collection.extend({
		model: Button,
		comparator: 'ordinal',
	});
	return Buttons;
});