define([ 'views/form/base-field-view', 'text!templates/form/field-checkbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldCheckboxView = View.extend({
		template: template,
	});

	return FieldCheckboxView;
});