define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-listbox-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var ListboxLayoutView = FieldDetailView.extend({
		className: 'field-layout selectable listbox-lo',
	    template: template,
	});

	return ListboxLayoutView;
});