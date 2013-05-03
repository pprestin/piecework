define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Screen = Model.extend({
		defaults : function() {
			return {
				title : "",
				type: "",
				url : "",
				ordinal: 1,
				created : new Date()
			};
		},
	});
	return Screen;
});