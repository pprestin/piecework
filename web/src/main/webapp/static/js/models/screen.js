define([ 'chaplin', 'models/base/model', 'models/sections' ], function(Chaplin, Model, Sections) {
	'use strict';

	var Screen = Model.extend({
		defaults : function() {
			return {
				title : "",
				type: "",
				location : "",
				sections: new Sections(),
				ordinal: 1,
				created : new Date()
			};
		},
		urlRoot: function() {
			return 'secure/v1/screen/' + this.get('processDefinitionKey') + '/' + this.get("interactionId");
		},
		url: function() {
			var uri = this.get("uri");
			if (uri == null) 
				return Model.__super__.url.apply(this);
			return uri;
		},
	});
	return Screen;
});