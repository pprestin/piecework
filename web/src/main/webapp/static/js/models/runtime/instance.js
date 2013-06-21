define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Instance = Model.extend({
		url: function() {
			var uri = this.get("uri");
			if (uri == null)
				return Model.__super__.url.apply(this);
			return uri;
		},
	});
	return Instance;
});