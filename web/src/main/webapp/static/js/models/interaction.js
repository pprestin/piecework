define([ 'chaplin', 'models/base/model', 'models/screens' ], function(Chaplin, Model, Screens) {
	'use strict';

	var Interaction = Model.extend({
		defaults : function() {
			return {
				label : "",
				ordinal: 1,
				screens: new Screens(),
				created : new Date()
			};
		},
	});
	return Interaction;
});