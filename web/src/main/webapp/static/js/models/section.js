define([ 'chaplin', 'models/fields', 'models/base/model' ], function(Chaplin, Fields, Model) {
	'use strict';

	var Section = Model.extend({
		defaults : function() {
			return {
				title: "",
				fields: new Fields(),
				ordinal: 1,
				process: null,
				created : new Date()
			};
		},
	});
	return Section;
});