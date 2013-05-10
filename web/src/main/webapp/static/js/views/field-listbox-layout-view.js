define([ 'views/field-detail-view', 'views/base/view', 'text!templates/field-listbox-layout.hbs'], 
		function(FieldDetailView, View, template) {
	'use strict';

	var ListboxLayoutView = FieldDetailView.extend({
	    template: template,
	    events: {
	    	'change .field-listbox': '_onChangeFieldListbox',
//			'blur .field-listbox-edit': '_onBlurFieldListbox',
			'focus .field-listbox': '_onFocusFieldListbox',
			'keypress .field-listbox': '_onKeyFieldListbox',
			'keypress .field-listbox-edit': '_onKeyFieldListboxEdit',
		},
		_onChangeFieldListbox: function(event) {
			var $target = $(event.target);
			var $option = $target.find('option:selected');

		},
		
		_onFocusFieldListbox: function(event) {
			var $target = $(event.target);
			
			
		},
		
		_onKeyFieldListbox: function(event) {
			var $target = $(event.target);
			var $editor = $target.next('input[type="text"].field-listbox-edit');
			
			if (event.keyCode === 13) {			
				var $option = $target.find('option:selected');
				var value = $option.text();
				$target.hide();
				$editor.val(value);
				$editor.show();
				$editor.focus();
			}
		},
		
		_onKeyFieldListboxEdit: function(event) {
			var $target = $(event.target);
			var $select = $target.prev('select.field-listbox');
			
			if (event.keyCode === 13) {				
				var value = $target.val();
				var $option = $select.find('option:selected');
				$target.hide();
				$option.text(value);
				$option.val(value);
				$target.val('');
				$select.show();
				$select.focus();
			}
		},
		
//		_onBlurFieldListbox: function(event) {
//			var $target = $(event.target).closest('.field-listbox-edit');
//			var $select = $target.prev('select.field-listbox');
////			$select.empty();
//			$select.show();
////			$target.remove();
//			$target.find('input[type="text"]').each(function(i, element) {
//				var value = element.value;
//				$editor.append('<option value="' + value + '">' + value + '</input>');
//			});
//		},
//		_onFocusFieldListbox: function(event) {
//			var $target = $(event.target);
//			var $option = $target.find('option:selected');
//			
//			
//			$target.after('<div class="field-listbox-edit"></div>');
//			var $editor = $target.next('div.field-listbox-edit');
//			$target.hide();
//			$target.find('option').each(function(i, element) {
//				$editor.append('<input type="text"></input>');
//			});
//		}
	});

	return ListboxLayoutView;
});