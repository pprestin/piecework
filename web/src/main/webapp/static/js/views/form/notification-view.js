define([ 'chaplin', 'views/base/view', 'text!templates/form/notification.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var NotificationView = View.extend({
		autoRender : true,
	    template: template,
	});

	return NotificationView;
});