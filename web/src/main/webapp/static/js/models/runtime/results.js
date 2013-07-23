define([ 'chaplin', 'models/base/collection', 'models/base/model' ], function(Chaplin, Collection, Model) {
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
//		parse: function(response, options) {
//            var resultsCollection = new Collection();
//            resultsCollection.add(response['list']);
//            response['collection'] = resultsCollection;
//            return response;
//        },
		url: function() {
            var uri = this.get("link");
            if (uri == null)
                return Model.__super__.url.apply(this);
            return uri;
        },
	});
	return Results;
});