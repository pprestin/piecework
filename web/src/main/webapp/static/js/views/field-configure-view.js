define([ 'views/base/view', 'text!templates/field-configure.hbs' ], function(View, template) {
	'use strict';

	var ScreenItemView = View.extend({
		autoRender: true,
		container: 'body',
		className: 'piecework-field-editor',
		tagName: 'div',
	    template: template,
	});

	return ScreenItemView;
});