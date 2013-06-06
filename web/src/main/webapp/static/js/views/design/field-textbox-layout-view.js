define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-textbox-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var TextboxLayoutView = FieldDetailView.extend({
//		className: 'selectable field-layout textbox-lo',
		template: template,
	});

	return TextboxLayoutView;
});