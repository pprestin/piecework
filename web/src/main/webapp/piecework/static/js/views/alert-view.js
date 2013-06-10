define([ 'chaplin', 'views/base/view', 'text!templates/alert.hbs' ], 
		function(Chaplin, View, template) {
	'use strict';

	var AlertView = View.extend({
		autoRender : true,
		className: 'alert alert-block alert-error fade in',
		container: '.alert-container',
	    template: template,
	});

	return AlertView;
});