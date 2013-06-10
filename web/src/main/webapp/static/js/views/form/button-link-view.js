define([ 'chaplin', 'views/base/view', 'text!templates/form/button-link.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var ButtonLinkView = View.extend({
		autoRender : true,
		tagName: 'span',
	    template: template,
	});

	return ButtonLinkView;
});