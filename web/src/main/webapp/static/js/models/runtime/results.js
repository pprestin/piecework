define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Results = Model.extend({
		defaults : function() {
			return {
			    definitions: [],
				firstResult : 0,
				list: [],
				maxResults: 0,
				total: 0,
			};
		},
		url: function() {
            var uri = this.get("link");
            if (uri == null)
                return Model.__super__.url.apply(this);
            return uri;
        },
	});
	return Results;
});