define([ 'views/form/base-field-view', 'text!templates/form/field-textarea.hbs'],
		function(View, template) {
	'use strict';

	var FieldTextareaView = View.extend({
		template: template,
	});

	return FieldTextareaView;
});