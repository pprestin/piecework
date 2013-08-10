define([ 'chaplin', 'views/base/view', 'text!templates/form/attachment.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var AttachmentView = View.extend({
		autoRender : true,
		className: 'well',
		tagName: 'li',
	    template: template,
	    events: {
	        'click .delete-attachment-btn': '_onDeleteAttachment',
	    },
	    _onDeleteAttachment: function(event) {
	        Backbone.sync('delete', this.model, {
	            success: function() {
	                Chaplin.mediator.publish('refreshAttachments');
	            }
	        });
	    }
	});

	return AttachmentView;
});