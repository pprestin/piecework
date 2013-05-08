define([ 'chaplin', 'models/field', 'views/field-checkbox-layout-view', 'views/field-listbox-layout-view', 'views/field-radio-layout-view', 'views/field-textarea-layout-view', 'views/field-textbox-layout-view', 'views/base/view', 'text!templates/section-detail.hbs' ], 
		function(Chaplin, Field, CheckboxLayoutView, ListboxLayoutView, RadioLayoutView, TextareaLayoutView, TextboxLayoutView, View, template) {
	'use strict';

	var SectionDetailView = View.extend({
		autoRender : true,
		className: 'section-layout selectable',
		container: '.screen-content',
	    template: template,
	    events: {
	    	'keydown .field-layout': '_onKeyFieldLayout',
	    },
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    addField: function(type) {
	    	if (this.$el.hasClass('selected')) {
	    		var ordinal = this.$el.find('.field-layout').length + 1;
	    		ordinal += this.model.attributes.ordinal;
		    	var sectionId = this.$el.prop('id');
	    		var field = new Field({type: type, ordinal: ordinal});
	    		var fieldId = type + '-' + field.cid;
	    		// Initialize the name to the view id
	    		field.attributes.name = fieldId;
	    		
		    	var container = '#' + sectionId + ' .section-content';		    
		    	if (type == 'checkbox')
		    		this.subview(fieldId, new CheckboxLayoutView({id: fieldId, container: container, model: field}));
		    	else if (type == 'listbox')
		    		this.subview(fieldId, new ListboxLayoutView({id: fieldId, container: container, model: field}));
		    	else if (type == 'radio')
		    		this.subview(fieldId, new RadioLayoutView({id: fieldId, container: container, model: field}));
		    	else if (type == 'textarea')
		    		this.subview(fieldId, new TextareaLayoutView({id: fieldId, container: container, model: field}));
		    	else
		    		this.subview(fieldId, new TextboxLayoutView({id: fieldId, container: container, model: field}));
	    	}
	    },
	    _onAddedToDOM: function() {
	    	var sectionId = this.model.cid;
	    	var ordinal = this.model.attributes.ordinal;
	    	if (ordinal == undefined)
	    		ordinal = 1000;
	    	
	    	this.$el.attr('tabindex', ordinal);
	    	this.$el.find('.section-content').sortable({ handle: ".drag-handle" });
		},
		_onKeyFieldLayout: function(event) {
			// If the ctrl key is down then move the selected item
			if (event.ctrlKey) {
				var $target = $('li.selectable.selected');
				var $parent = $target.closest('ul');
				
				switch (event.keyCode) {
				case 38: // Up arrow
					var $oneAbove = $target.prev('li.selectable');
					if ($oneAbove.length == 1) {
						$target.insertBefore($oneAbove);
						$target.focus();
					}
					break;
				case 40: // Down arrow
					var $oneBelow = $target.next('li.selectable');
					if ($oneBelow.length == 1) {
						$target.insertAfter($oneBelow);
						$target.focus();
					}
					break;
				}
			}
		},
	});

	return SectionDetailView;
});