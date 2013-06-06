define([ 'chaplin', 'models/base/collection', 'models/design/field'], function(Chaplin, Collection, Field) {
	'use strict';

	var Fields = Collection.extend({
		model: Field,
		comparator: 'ordinal',
	});
	return Fields;
});