define([ 'chaplin', 'models/alert', 'models/screen', 'models/section', 'views/alert-view', 'views/section-detail-view', 'views/base/view', 'text!templates/screen-detail.hbs' ], 
		function(Chaplin, Alert, Screen, Section, AlertView, SectionDetailView, View, template) {
	'use strict';

	var ScreenDetailView = View.extend({
		autoRender : true,
		region: 'main',
	    template: template,
	    events: {
			'click .add-checkbox-button': '_addCheckbox',
			'click .add-listbox-button': '_addListbox',
			'click .add-radio-button': '_addRadio',
			'click .add-textarea-button': '_addTextarea',
			'click .add-textbox-button': '_addTextbox',
	    	'click .add-section-button': '_addSection',
	    	'click .remove-button': '_remove',
	    	'keydown .selectable': '_onSelectableKey',
	    	'keydown .accessible-btn': '_onButtonPress',
	    	'focus .selectable': '_selectItem',
	    }, 
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    _addField: function(type) {
	    	var $selectedSectionLayout = $('.section-layout.selected');
	    	
	    	if ($selectedSectionLayout.length == 0)
	    		$selectedSectionLayout = $('.section-layout:first');
	    	
	    	if ($selectedSectionLayout.length == 0) {
	    		this._addSection();
	    		$selectedSectionLayout = $('.section-layout:first');
	    	}
	    	
	    	if ($selectedSectionLayout.length > 0) {
		    	$selectedSectionLayout.addClass('selected');
		    	$('.remove-button').removeAttr('disabled');
	    	}
	    	
	    	var sectionId = $selectedSectionLayout.attr('id');
	    	
	    	if (sectionId === undefined)
	    		return;
	    	
	    	var sectionView = this.subview(sectionId);
	    	
	    	if (sectionView !== undefined)
	    		sectionView.addField(type);
	    },
	    _addCheckbox: function() {
	    	this._addField('checkbox');
	    },
	    _addListbox: function() {
	    	this._addField('listbox');
	    },
	    _addRadio: function() {
	    	this._addField('radio');
	    },
	    _addTextarea: function() {
	    	this._addField('textarea');
	    },
	    _addTextbox: function() {
	    	this._addField('textbox');
	    },
	    _addSection: function() {
	    	var ordinal = $('.section-layout').length + 1;
	    	var section = new Section({ordinal: ordinal});
	    	var sectionId = 'section-' + section.cid;
	    	
	    	this.subview(sectionId, new SectionDetailView({id: sectionId, model: section}));
	    },
	    _onButtonPress: function(event) {
	    	switch (event.keyCode) {
	    	case 13:
	    		$(event.target).click();
	    		break;
	    	case 39:      
	            $(".accessible-btn:focus").next(".accessible-btn").focus();
	            break;
	    	case 37:      
	            $(".accessible-btn:focus").prev(".accessible-btn").focus();
	            break;
	        }
	    },
	    _onSelectableKey: function(event) {
	    	switch (event.keyCode) {
	    	case 8: 
	    	case 46:
	    		this._remove(event);
	    		break;
	        }
	    },
	    _remove: function(event) {
	    	var $selected = $('.field-layout.selected');
	    	
	    	if ($selected.length > 0) {
	    		$selected.remove();
	    		$('.remove-button').attr('disabled', 'disabled');
	    	} else {
	    		$selected = $('.section-layout.selected');
	    		
	    		if ($('#alert-remove-section').length > 0 || $selected.find('.field-layout').length == 0) {
	    			$selected.remove();
	    			this.removeSubview('alert');
	    			$('.remove-button').attr('disabled', 'disabled');
	    		} else {
	    			$selected.addClass('for-deletion');
	    			var alert = new Alert({title: 'Are you sure?', content: "This section has one or more fields. If you change your mind later, you will have to add back each field one at a time. If you're really sure, simply click Delete again, and the section will be removed."})
	    			var alertView = new AlertView({id: 'alert-remove-section', model: alert});
	    			this.subview('alert', alertView);
	    			$('#alert-remove-section').on('close', function() {
	    				$selected.removeClass('for-deletion');
	    				$('.remove-button').attr('disabled', 'disabled');
	    			});
	    		}
	    	}
	    },
	    _selectItem: function(event) {
	    	var $target = $(event.target);
	    	
	    	// Prevent this event from bubbling higher, since we want to explicitly select
	    	// the element that is closest to the target
	    	event.stopPropagation();
	    	var $selectable = $target; //.closest('.selectable');
	    	
	    	if ($selectable.hasClass('selected') && $selectable.is("not :focus")) {
	    		$selectable.removeClass('selected');
			    $('.remove-button').attr('disabled', 'disabled');
	    	} else {
	    		$('.selectable').removeClass('selected');
		    	$selectable.addClass('selected');
			    $('.remove-button').removeAttr('disabled');
	    	}
	    },
	    _onAddedToDOM: function() {
	    	
		}
	});

	return ScreenDetailView;
});