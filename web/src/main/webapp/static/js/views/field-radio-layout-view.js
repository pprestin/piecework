define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-radio-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var RadioLayoutView = FieldDetailView.extend({
//		className: 'field-layout selectable radio-lo',
	    template: template,
	});

	return RadioLayoutView;
});