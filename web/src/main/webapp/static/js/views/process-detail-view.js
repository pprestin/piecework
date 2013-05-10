define([ 'models/process', 'models/interaction', 'models/screen', 'views/base/view', 'views/interaction-list-view', 'text!templates/process-detail.hbs' ], 
         function(Process, Interaction, Screen, View, InteractionListView, template ) {
	'use strict';

	var ProcessDetailView = View.extend({
		autoRender : true,
		container: '#main-frame',
	    template: template,
	    listen: {
	        'addedToDOM': '_onAddedToDOM',
	        'change model': '_onModelChanged'
	    },
	   	events: {
	   		'change :input': '_fieldValueChanged',
	   		'click .add-screen-button': '_addScreen',
	   		'click .add-interaction-button': '_addInteraction',
	   		'click .edit-button': '_toggleEditing',
	   		'focus .selectable': '_selectItem',
	   		'hide .accordion-group': '_toggleSection',
	   		'keypress .process-short-name': '_onKeyProcessShortName',
	   		'show .accordion-group': '_toggleSection',
	   	},
	   		    	
	   	initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
		},
		
		_addInteraction: function(event) {
			var interactions = this.model.get("interactions");
			var ordinal = interactions.length + 1;
			var label = "Interaction " + ordinal;
			var interaction = new Interaction({label: label, ordinal: ordinal});
			interactions.add(interaction);
			return interaction.cid;
		},
		
		_addScreen: function(event) {
			var layoutId;
			var $selectedLayout = $('.group-layout.interaction.selected');
	    	
	    	if ($selectedLayout.length == 0)
	    		$selectedLayout = $('.group-layout.interaction:first');
	    	
	    	if ($selectedLayout.length == 0) {
	    		layoutId = this._addInteraction();
	    		$selectedLayout = $('.group-layout.interaction:first');
	    	}
	    	
	    	if ($selectedLayout.length > 0) {
	    		$selectedLayout.addClass('selected');
		    	$('.remove-button').removeAttr('disabled');
	    	}
	    	
	    	if (layoutId === undefined)
	    		layoutId = $selectedLayout.attr('id');
	    	
	    	if (layoutId === undefined)
	    		return;
	    	
	    	var interactions = this.model.get("interactions");
	    	var models = interactions.models;
	    	// TODO: Is it worth having a map here to speed lookup?
	    	for (var i=0;i<models.length;i++) {
	    		if (models[i].cid == layoutId) {
	    			var screens = models[i].get("screens");
	    			var screenModels = screens.models;	    			
		    		var ordinal = screenModels.length + 1;
		    		screens.add(new Screen({ordinal: ordinal}));
	    			break;
	    		}
	    	}
	    	
		},
			    
	   	_fieldValueChanged: function(event) {
	    	var attributeName = event.target.name;
	    	var $controlGroup = this.$(event.target).closest('.control-group');
	    	var isNotEmpty = event.target.value != null && event.target.value != '';
	    	
	    	if (attributeName === undefined) 
	    		return;
	    	
	    	if (attributeName == 'processDefinitionKey')
	    		this.$('ul.breadcrumb').find('li.active').html(event.target.value);
				    	
	    	var previous = this.model.get(attributeName);
	    	if (previous === undefined || previous != event.target.value) {
	    		var attributes = {};
	    		attributes[attributeName] = event.target.value;
	    		this.model.save(attributes, {wait: true});
	    	}
	    	$controlGroup.toggleClass('hasData', isNotEmpty);
	    },
	    
		_onAddedToDOM: function() {
			var interactions = this.model.attributes.interactions;
			this.subview('interaction-list', new InteractionListView({collection: interactions}));
		},
		_onKeyProcessShortName: function(event) {
			var $target = $(event.target);
			switch (event.keyCode) {
			case 8:
			case 9:
			case 13:
			case 27:
			case 37:
			case 38:
			case 39: 
			case 40:
			case 46:
				// Allow delete, backspace, tab, escape, arrow keys, and enter
				return;
			default:
				var char = String.fromCharCode(event.keyCode);
				// Don't allow non-alphanumeric characters
				if (!/[a-z0-9]/i.test(char)) {
	                event.preventDefault(); 
	            }  
				break;
			};
		},
		_onModelChanged: function(model, options) {
			var model = this.model;
			this.$(':input').each(function(i, element) {
				var name = element.name;
				if (name !== undefined && name != '') {
					var previous = element.value;
					var next = model.get(name);
					if (previous === undefined || previous != next)
						element.value = next;
				}
			});
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
	    _toggleEditing: function(event) {
	    	var $editButton = this.$(event.currentTarget);
	    	var $accordionGroup = $editButton.closest('.accordion-group');
	    	$editButton.html(($accordionGroup.hasClass('editing') ? 'Done' : 'Edit'));
	    	$accordionGroup.toggleClass('editing');
	    }, 
	    _toggleSection: function(event) {
	    	var $accordionGroup = this.$(event.currentTarget);
	    	//var $editButton = $accordionGroup.find('.edit-button');
	    	$accordionGroup.find('.section-open-indicator').toggleClass('icon-chevron-right icon-chevron-down');
	    },
	   	
	});

	return ProcessDetailView;
});
