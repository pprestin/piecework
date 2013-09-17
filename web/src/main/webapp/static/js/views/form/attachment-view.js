define([ 'chaplin', 'models/notification', 'views/base/view', 'views/form/notification-view', 'text!templates/form/attachment.hbs' ],
		function(Chaplin, Notification, View, NotificationView, template) {
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
	        var attachmentView = this;
	        Backbone.sync('delete', this.model, {
	            success: function() {
	                Chaplin.mediator.publish('refreshAttachments');
	            },
	            error: function() {
                    var notification = new Notification({title: 'Unable to delete', message: 'This attachment cannot be deleted or you are not authorized to delete it', permanent: true})
                    attachmentView.subview('notificationView', new NotificationView({container: '#main-content', model: notification}));
	            }
	        });
	    }
	});

	return AttachmentView;
});