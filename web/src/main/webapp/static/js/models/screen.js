define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Screen = Model.extend({
		defaults : function() {
			return {
				title : "",
				type: "",
				location : "",
				ordinal: 1,
				process: null,
				created : new Date()
			};
		},
	});
	return Screen;
});