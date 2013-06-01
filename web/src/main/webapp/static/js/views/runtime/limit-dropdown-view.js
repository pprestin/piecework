define([ 'chaplin', 'views/base/view', 'text!templates/runtime/limit-dropdown.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var LimitDropdownView = View.extend({
		autoRender : true,
		className: 'dropdown',
		container: '.limit-dropdown-container',
		tagName: 'div',
	    template: template,
	});

	return LimitDropdownView;
});