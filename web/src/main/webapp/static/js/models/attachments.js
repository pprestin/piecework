define([ 'chaplin', 'models/base/collection', 'models/attachment'], function(Chaplin, Collection, Attachment) {
	'use strict';

	var Attachments = Collection.extend({
		model: Attachment,
		parse: function(response, options) {
            var attachments = response["attachments"];
            return attachments;
        },
	});
	return Attachments;
});