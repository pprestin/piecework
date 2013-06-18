define([ 'chaplin', 'views/base/view', 'text!templates/form/button-link.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var ButtonLinkView = View.extend({
		autoRender : true,
		container: '.screen-buttons',
		tagName: 'li',
	    template: template,
	});

	return ButtonLinkView;
});