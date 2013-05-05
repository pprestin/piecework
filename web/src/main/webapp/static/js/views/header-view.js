define([ 'chaplin', 'views/base/view', 'text!templates/header.hbs' ], 
		function(Chaplin, View, template) {
	'use strict';

	var HeaderView = View.extend({
		autoRender: true,
		container: '#header',
	    template: template
	});

	return HeaderView;
});