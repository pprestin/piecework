define([ 'views/design/field-detail-view', 'views/base/view', 'text!templates/field-checkbox-layout.hbs'],
		function(FieldDetailView, View, template) {
	'use strict';

	var CheckboxLayoutView = FieldDetailView.extend({
//		className: 'selectable field-layout checkbox-lo',
	    template: template,
	});

	return CheckboxLayoutView;
});