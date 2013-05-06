define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Section = Model.extend({
		defaults : function() {
			return {
				title: "",
				fieldIds: [],
				ordinal: 1,
				process: null,
				created : new Date()
			};
		},
	});
	return Section;
});