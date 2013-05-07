define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-checkbox-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var CheckboxLayoutView = FieldDetailView.extend({
		className: 'field-layout selectable checkbox-lo',
	    template: template,
	});

	return CheckboxLayoutView;
});