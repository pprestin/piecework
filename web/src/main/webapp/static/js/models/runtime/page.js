define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Page = Model.extend({
		defaults : function() {
            return {
                applicationTitle: "",
                pageTitle: "",
                user: null,
                static: "",
                resource: null
            };
        }
	});
	return Page;
});