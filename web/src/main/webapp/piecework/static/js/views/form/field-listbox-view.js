define([ 'views/form/base-field-view', 'text!templates/form/field-listbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldListboxView = View.extend({
		template: template,
	});

	return FieldListboxView;
});