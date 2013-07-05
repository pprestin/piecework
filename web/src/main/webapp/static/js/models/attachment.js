define([ 'chaplin', 'models/base/model' ], function(Chaplin, Model) {
	'use strict';

	var Attachment = Model.extend({
	    parse: function(response, options) {
	        var contentType =  response['contentType'];
	        var lastModified = response['lastModified']
	        if (contentType != undefined && contentType == 'text/plain') {
                response['inline'] = true;
	        }
            if (lastModified != undefined) {
                var lastModifiedDate = new Date(lastModified);
                response['lastModified'] = lastModifiedDate.toLocaleString();
            }
            return response;
	    },
		url: function() {
			var uri = this.get("link");
			if (uri == null)
				return Model.__super__.url.apply(this);
			return uri;
		},
	});
	return Attachment;
});