define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-listbox-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var ListboxLayoutView = FieldDetailView.extend({
		className: 'field-layout selectable listbox-lo',
	    template: template,
	    events: {
			'blur .field-listbox-edit': '_onBlurFieldListbox',
			'focus .field-listbox': '_onFocusFieldListbox',
		},
		_onBlurFieldListbox: function(event) {
			var $target = $(event.target).closest('.field-listbox-edit');
			var $select = $target.prev('select.field-listbox');
			$select.empty();
			$select.show();
			$target.remove();
			$target.find('input[type="text"]').each(function(i, element) {
				var value = element.value;
				$editor.append('<option value="' + value + '">' + value + '</input>');
			});
		},
		_onFocusFieldListbox: function(event) {
			var $target = $(event.target);
			$target.after('<div class="field-listbox-edit"></div>');
			var $editor = $target.next('div.field-listbox-edit');
			$target.hide();
			$target.find('option').each(function(i, element) {
				$editor.append('<input type="text"></input>');
			});
		}
	});

	return ListboxLayoutView;
});