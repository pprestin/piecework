define([ 'chaplin', 'models/screen', 'models/section', 'views/section-detail-view', 'views/base/view', 'text!templates/screen-detail.hbs' ], 
		function(Chaplin, Screen, Section, SectionDetailView, View, template) {
	'use strict';

	var ScreenDetailView = View.extend({
		autoRender : true,
		region: 'main',
	    template: template,
	    events: {
//	    	'blur .field-layout': '_unselectField',
	    	'click .add-field-button': '_addField',
	    	'click .add-section-button': '_addSection',
	    	'click .remove-button': '_remove',
	    	'keydown .accessible-btn': '_onButtonPress',
	    	'focus .selectable': '_selectItem',
//	    	'focus .field-layout': '_selectField',
//	    	'focus .section-layout': '_selectSection',
	    }, 
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    _addField: function() {
	    	var $selectedSectionLayout = $('.section-layout.selected');
	    	
	    	if ($selectedSectionLayout.length == 0)
	    		$selectedSectionLayout = $('.section-layout:first');
	    	
	    	if ($selectedSectionLayout.length == 0) {
	    		this._addSection();
	    		$selectedSectionLayout = $('.section-layout:first');
	    	}
	    	
	    	$selectedSectionLayout.addClass('selected');
	    	
	    	var sectionId = $selectedSectionLayout.attr('id');
	    	
	    	if (sectionId === undefined)
	    		return;
	    	
	    	var sectionView = this.subview(sectionId);
	    	
	    	if (sectionView !== undefined)
	    		sectionView.addField();
	    },
	    _addSection: function() {
	    	var section = new Section({});
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
	    _remove: function() {
	    	
	    },
	    _selectItem: function(event) {
	    	var $selectable = $(event.target).closest('.selectable');
	    	$('.selectable').removeClass('selected');
	    	
	    	if ($selectable.hasClass('selected')) {
	    		$selectable.removeClass('selected');
			    $('.remove-button').attr('disabled', 'disabled');
	    	} else {
		    	$selectable.addClass('selected');
			    $('.remove-button').removeAttr('disabled');
	    	}
	    },
//	    _selectField: function(event) {
//	    	$('.selectable').removeClass('selected');
//	    	$(event.currentTarget).addClass('selected');
//	    	$('.remove-button').removeAttr('disabled');
//	    },
//	    _selectSection: function(event) {
//	    	// If another section is selected, unselect it
//	    	$('.selectable').removeClass('selected');
//	    	$(event.currentTarget).addClass('selected');
//	    },
//	    _unselectField: function(event) {
//	    	$('.remove-button').attr('disabled', 'disabled');
//	    },
//	    _unselectSection: function(event) {
//	    	$(event.target).removeClass('selected');
//	    },
	    _onAddedToDOM: function() {

		}
	});

	return ScreenDetailView;
});