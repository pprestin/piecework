define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Alert = Model.extend({
		defaults : function() {
			return {
				title : "",
				content: "",
			};
		},
	});
	return Alert;
});