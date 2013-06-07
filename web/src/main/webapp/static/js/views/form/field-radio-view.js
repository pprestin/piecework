define([ 'views/form/base-field-view', 'text!templates/form/field-radio.hbs'],
		function(View, template) {
	'use strict';

	var FieldRadioView = View.extend({
		template: template,
	});

	return FieldRadioView;
});