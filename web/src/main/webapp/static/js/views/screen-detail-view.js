define([ 'chaplin', 'models/alert', 'models/field', 'models/screen', 'models/section', 'views/alert-view', 
         'views/base/collection-view', 'views/section-detail-view', 'views/base/view', 'text!templates/screen-detail.hbs', 'jqueryui' ], 
		function(Chaplin, Alert, Field, Screen, Section, AlertView, CollectionView, SectionDetailView, View, template) {
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
	    	'keydown .dropdown-toggle': '_onKeyDropdownToggle',
	    	'keydown ul.dropdown-menu li': '_onKeyDropdownToggle',
	    	'keydown .selectable': '_onSelectableKey',
	    	'keydown .accessible-btn': '_onButtonPress',
	    	'focus .selectable': '_selectItem',
	    }, 
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    _addField: function(type) {
	    	
	    	var $selectedSectionLayout = $('.section.selectable.selected');
	    	
	    	if ($selectedSectionLayout.length == 0)
	    		$selectedSectionLayout = $('.section.selectable:first');
	    	
	    	if ($selectedSectionLayout.length == 0) {
	    		this._addSection();
	    		$selectedSectionLayout = $('.section.selectable:first');
	    	}
	    	
	    	if ($selectedSectionLayout.length > 0) {
		    	$selectedSectionLayout.addClass('selected');
		    	$('.remove-button').removeAttr('disabled');
	    	}
	    	
	    	var sectionId = $selectedSectionLayout.attr('id');
	    	
	    	if (sectionId === undefined)
	    		return;
	    	
	    	var sectionModels = this.model.attributes.sections.models;
	    	// TODO: Is it worth having a map here to speed lookup?
	    	for (var i=0;i<sectionModels.length;i++) {
	    		if (sectionModels[i].cid == sectionId) {
	    			var fieldModels = sectionModels[i].attributes.fields.models;
	    			
		    		var ordinal = fieldModels.length + 1;
		    		//ordinal += this.model.attributes.ordinal;
		    		
		    		var field = new Field({type: type, ordinal: ordinal});
		    		var fieldId = type + '-' + field.cid;
		    		// Initialize the name to the view id
		    		field.attributes.name = fieldId;
		    		sectionModels[i].attributes.fields.add(field);
	    			break;
	    		}
	    	}
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
	    	var sections = this.model.get("sections");
			var ordinal = sections.models.length + 1;
			var title = "Section " + ordinal;
	    	sections.add(new Section({title: title, ordinal: ordinal}));
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
	    _onKeyDropdownToggle: function(event) {
	    	var $target = $(event.target);
	    	var $focused = $('.dropdown-menu > li > a:focus');
	    	var $li = $focused.closest('li');
	    	if ($li.length == 0) {
	    		$li = $target.next('ul.dropdown-menu').find('li:first');
	    		$li.find('a').focus();
	    		return;
	    	}
	    	
	    	switch (event.keyCode) {
			case 38: // Up arrow
				var $oneAbove = $li.prev('li').find('a');
				if ($oneAbove.length == 1) {
					$oneAbove.focus();
				}
				break;
			case 40: // Down arrow
				var $oneBelow = $li.next('li').find('a');
				if ($oneBelow.length == 1) 
					$oneBelow.focus();
				
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
	    	var $selected = $('.field.selectable.selected');
	    	
	    	if ($selected.length > 0) {
	    		$selected.remove();
	    		$('.remove-button').attr('disabled', 'disabled');
	    	} else {
	    		$selected = $('.section.selected');
	    		
	    		if ($('#alert-remove-section').length > 0 || $selected.find('.field.selectable').length == 0) {
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
			this.subview('section-list', new CollectionView({autoRender:true, className:'section-list nav', container: '.screen-content', itemView: SectionDetailView, tagName: 'ul', collection: this.model.attributes.sections}));
		}
	});

	return ScreenDetailView;
});