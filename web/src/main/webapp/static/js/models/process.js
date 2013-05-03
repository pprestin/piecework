define([ 'chaplin', 'models/base/model', 'models/screens' ], function(Chaplin, Model, Screens) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				shortName : "",
				formalName: "",
				summary : "",
				participants: "",
				screens: new Screens(),
				created : new Date()
			};
		},
	});
	return Process;
});