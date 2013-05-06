define([ 'views/base/view', 'text!templates/field-item.hbs' ], function(View, template) {
	'use strict';

	var FieldItemView = View.extend({
		tagName: 'li',
	    template: template,
	});

	return FieldItemView;
});