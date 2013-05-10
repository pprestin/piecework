define([ 'chaplin', 'models/base/model', 'models/sections' ], function(Chaplin, Model, Sections) {
	'use strict';

	var Screen = Model.extend({
		defaults : function() {
			return {
				title : "",
				type: "",
				location : "",
				sections: new Sections(),
				ordinal: 1,
				created : new Date()
			};
		},
	});
	return Screen;
});