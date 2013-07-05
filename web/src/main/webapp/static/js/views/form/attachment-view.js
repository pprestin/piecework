define([ 'chaplin', 'views/base/view', 'text!templates/form/attachment.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var AttachmentView = View.extend({
		autoRender : true,
		className: 'well',
		tagName: 'li',
	    template: template,
	});

	return AttachmentView;
});