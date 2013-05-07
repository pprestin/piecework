define([ 'views/base/view'], 
		function(View) {
	'use strict';

	var FieldDetailView = View.extend({
		autoRender: true,
		tagName: 'div',
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    events: {
	    	'blur .field-label-input': '_onBlurFieldLabel',
	    	'change .field-label-input': '_onChangeFieldLabelInput',
	    	'change .field-required': '_onChangeFieldRequired',
	    	'focus .field-label-input': '_onFocusFieldLabel',
	    },
	    _onAddedToDOM: function() {
	    	var fieldId = this.model.cid;
	    	var ordinal = this.model.ordinal;
	    	if (ordinal == undefined)
	    		ordinal = "-1";
	    	var $layout = this.$el;
	    	
	    	$layout.attr('id', fieldId);
	    	$layout.attr('tabindex', ordinal);
	    	$('.hastooltip').tooltip();
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
		_onChangeFieldRequired: function(event) {
			var $fieldLayout = $(event.target).closest('.field-layout');
			$fieldLayout.find('.field-input').attr('required', event.target.checked);
		},
		_onFocusFieldLabel: function(event) {
			var $target = $(event.target);
			$target.removeClass('unfocused');
		},
	});

	return FieldDetailView;
});