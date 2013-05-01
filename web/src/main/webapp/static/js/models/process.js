define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				shortName : "",
				formalName: "",
				summary : "",
				participants: "",
				created : new Date()
			};
		},
	});
	return Process;
});