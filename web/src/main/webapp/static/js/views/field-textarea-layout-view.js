define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-textarea-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var TextareaLayoutView = FieldDetailView.extend({
		className: 'field-layout selectable textarea-lo',
	    template: template,
	});

	return TextareaLayoutView;
});