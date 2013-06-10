define([ 'views/base/view'],
		function(View) {
	'use strict';

	var FieldView = View.extend({
		autoRender: true,
		className: 'control-group',
		tagName: 'div',
	});

	return FieldView;
});