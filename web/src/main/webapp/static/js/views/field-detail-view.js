define([ 'views/base/view'], 
		function(View) {
	'use strict';

	var FieldDetailView = View.extend({
		autoRender: false,
		className: 'group-layout field selectable',
		tagName: 'li',
	    listen: {
	        addedToParent: '_onAddedToParent'
	    },
	    events: {
	    	'blur .field-label-input': '_onBlurFieldLabel',
	    	'change .field-label-input': '_onChangeFieldLabelInput',
	    	'change .field-numeric': '_onChangeFieldNumeric',
	    	'change .field-quantity': '_onChangeFieldQuantity',
	    	'change .field-quantity-options': '_onChangeFieldQuantityOptions',
	    	'change .field-required': '_onChangeFieldRequired',
	    	'focus .field-label-input': '_onFocusFieldLabel',
	    	'keydown .field-numeric': '_onKeyFieldNumeric',
	    },
	    _onAddedToParent: function() {
//	    	var fieldId = this.model.cid;
	    	var ordinal = this.model.get("ordinal");
	    	if (ordinal == undefined)
	    		ordinal = 1;
	    	var type = this.model.get("type");
	    	var label = type.charAt(0).toUpperCase() + type.slice(1);
	    	
	    	this.$el.attr('tabindex', ordinal);
	    	$('.hastooltip').tooltip();
	    	this.$el.attr('data-content', label);
		},
		_onBlurFieldLabel: function(event) {
			var $target = $(event.target);
			$target.addClass('unfocused');
		},
		_onChangeFieldLabelInput: function(event) {
			var $target = $(event.target);
			var value = $target.val();
			
			$target.toggleClass('empty', (value === undefined || value == ''));
		},
		_onChangeFieldNumeric: function(event) {
			var $target = $(event.target);
			var max = $target.attr('data-value-max');
			var min = $target.attr('data-value-min');
			
			if (max === undefined)
				max = 10;
			if (min === undefined)
				min = 0;
			
			var value = parseInt($target.val());
			if (value > max)
				value = max;
			if (value < min)
				value = min;
			
			$target.val(value);
		},
		_onChangeFieldQuantity: function(event) {
			var $target = $(event.target);
			var value = parseInt($target.val());
			var $fieldLayout = $(event.target).closest('.selectable.field');
			var $controls = $fieldLayout.find('.controls:first').clone();
			var $controlGroup = $fieldLayout.find('.control-group');
			var counter = 1;
			
			$controls.find('.field-label-input').val('');
			
			$fieldLayout.find('.controls').each(function(i, element) {
				if (i >= value)
					$(element).remove();
				
				counter += 1;
			});
			
			for (var i=counter;i<=value;i++) {
				$controlGroup.append($controls.clone());
			}
		},
		_onChangeFieldQuantityOptions: function(event) {
			var $target = $(event.target);
			var value = parseInt($target.val());
			var $fieldLayout = $(event.target).closest('.selectable.field');
			var $option = $fieldLayout.find('option:first').clone();
			var $select = $fieldLayout.find('select.field-input');
			var counter = 1;
			
			$option.val('');
			$option.text('');
			
			$select.find('option').each(function(i, element) {
				if (i >= value)
					$(element).remove();
				
				counter += 1;
			});
			
			for (var i=counter;i<=value;i++) {
				$select.append($option.clone());
			}
		},
		_onChangeFieldRequired: function(event) {
			var $fieldLayout = $(event.target).closest('.selectable.field');
			$fieldLayout.find('.field-input').attr('required', event.target.checked);
		},
		_onFocusFieldLabel: function(event) {
			var $target = $(event.target);
			$target.removeClass('unfocused');
		},
		_onKeyFieldNumeric: function(event) {
			var $target = $(event.target);
			switch (event.keyCode) {
			case 8:
			case 9:
			case 13:
			case 27:
			case 37:
			case 39: 
			case 46:
				// Allow delete, backspace, tab, escape, left, right, and enter
				return;
			case 38: // Up arrow
				var increment = $target.attr('data-value-increment');
				if (increment === undefined)
					increment = 1;
				else
					increment = parseInt(increment);
				
				var max = $target.attr('data-value-max');
				if (max === undefined)
					max = 10;
				else
					max = parseInt(max);
				
				var value = parseInt($target.val()) + increment;
				if (value > max)
					value = max;
				$target.val(value).trigger("change");
				break;
			case 40: // Down arrow
				var increment = $target.attr('data-value-increment');
				if (increment === undefined)
					increment = 1;
				else
					increment = parseInt(increment);
				
				var min = $target.attr('data-value-min');
				if (min === undefined)
					min = 0;
				else
					min = parseInt(min);
				
				var value = parseInt($target.val()) - increment;
				if (value < min)
					value = min;
				$target.val(value).trigger("change");
				break;
			default:
				// Don't allow non-numeric characters
				if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105 )) {
	                event.preventDefault(); 
	            }  
				break;
			};
		}
	});

	return FieldDetailView;
});