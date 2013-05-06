define([ 'chaplin', 'models/base/collection', 'models/field'], function(Chaplin, Collection, Field) {
	'use strict';

	var Fields = Collection.extend({
		model: Field,
		comparator: 'ordinal'
	});
	return Fields;
});