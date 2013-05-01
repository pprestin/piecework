define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Process = Model.extend({
		defaults : function() {
			return {
				label : "",
				summary : "",
				created : new Date()
			};
		},
	});
	return Process;
});