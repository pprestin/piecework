define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var History = Model.extend({
		url: function() {
			var uri = this.get("link");
			if (uri == null)
				return Model.__super__.url.apply(this);
			return uri;
		},
	});
	return History;
});