define([ 'views/form/base-field-view', 'text!templates/form/field-file.hbs'],
		function(View, template) {
	'use strict';

	var FieldFileView = View.extend({
		template: template,
	});

	return FieldFileView;
});