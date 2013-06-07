define([ 'views/form/base-field-view', 'text!templates/form/field-textbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldTextboxView = View.extend({
		template: template,
	});

	return FieldTextboxView;
});