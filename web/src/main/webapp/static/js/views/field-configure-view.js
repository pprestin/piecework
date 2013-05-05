define([ 'views/base/view', 'text!templates/field-configure.hbs' ], function(View, template) {
	'use strict';

	var ScreenItemView = View.extend({
		tagName: 'div',
	    template: template,
	});

	return ScreenItemView;
});