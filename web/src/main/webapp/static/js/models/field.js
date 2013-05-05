define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Field = Model.extend({
		defaults : function() {
			return {
				name : "",
				type: "",
				required : false,
				value: "",
				ordinal: 1,
				created : new Date()
			};
		},
	});
	return Field;
});