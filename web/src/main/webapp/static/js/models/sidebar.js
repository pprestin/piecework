define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Sidebar = Model.extend({
		defaults : function() {
			return {
				title : "",
				type: "",
				actions: { add: "", remove: "" },
				collection: null
			};
		},
	});
	return Sidebar;
});