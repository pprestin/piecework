define([ 'chaplin', 'views/base/view', 'text!templates/form/form.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var FormView = View.extend({
		autoRender : true,
		container: '.main-content',
		id: 'main-form',
		tagName: 'form',
	    template: template,
	});

	return FormView;
});