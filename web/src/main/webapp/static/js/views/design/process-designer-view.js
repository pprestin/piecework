define([ 'chaplin', 'models/process', 'models/interaction', 'models/screen', 'models/screens', 'views/base/view', 'views/interaction-list-view', 'text!templates/process-designer.hbs' ], 
         function(Chaplin, Process, Interaction, Screen, Screens, View, InteractionListView, template ) {
	'use strict';

	var ProcessDesignerView = View.extend({
		autoRender: true,
		container : '#main-screen',
		id: 'process-designer-view',
		template: template,
		regions : {
			'#left-frame' : 'sidebar',
			'.sidebar-content' : 'sidebar-content',
			'.screen-list' : 'screen-list',
		},
	    listen: {
	        'addedToDOM': '_onAddedToDOM',
	    },
	   	events: {
	   		'change :input': '_fieldValueChanged',
	   		'click .add-screen-button': '_addScreen',
	   		'click .add-interaction-button': '_addInteraction',
	   		'click .edit-button': '_toggleEditing',
	   		'click .remove-button': '_remove',
	   		'focus .selectable': '_selectItem',
	   		'hide .accordion-group': '_toggleSection',
	   		'keypress .process-short-name': '_onKeyProcessShortName',
	   		'show .accordion-group': '_toggleSection',
	   	},
	   		    	
//	   	initialize: function(options) {
//	   		View.__super__.initialize.apply(this, options);
//	   		this.model.fetch();
//		},
		
//		render: function(options) {
//	   		View.__super__.render.apply(this, options);
//	   		return this;
//		},
		
		onCollectionReady: function() {
			var process = this.model.getProcess();
			var interactions = this.model.getInteractions();
			
			if (process.get("processDefinitionKey") !== undefined)
				this._onProcessSynced(process);
			
			var interactionArray = process.get("interactions");
			for (var i=0;i<interactionArray.length;i++) {
				interactions.add(interactionArray[i]);
			}
		},
		
		_addInteraction: function(event, includeNewScreen) {
			var process = this.model.getProcess();
			var interactions = process.get("interactions");
			var ordinal = interactions.length + 1;
			var label = "Interaction " + ordinal;
			var processDefinitionKey = process.get('processDefinitionKey');
			var interaction = new Interaction({label: label, processDefinitionKey: processDefinitionKey, ordinal: ordinal});
			
			if (includeNewScreen !== undefined && includeNewScreen) {
				var screen = new Screen();
				interaction.set("screens", new Screens(screen));
			}
			
			this.listenTo(interaction, 'sync', this._onInteractionSynced);
			interaction.save();
			return interaction.cid;
		},
		
		_addScreen: function(event) {
			var interactionId;
			var $selectedInteraction = $('.group-layout.interaction.selected');
	    	
	    	if ($selectedInteraction.length == 0)
	    		$selectedInteraction = $('.group-layout.interaction:first');
	    	
	    	if ($selectedInteraction.length == 0) {
	    		this._addInteraction(event, true);
	    		return;
	    	}
	    	
	    	if ($selectedInteraction.length > 0) {
	    		$selectedInteraction.addClass('selected');
		    	$('.remove-button').removeAttr('disabled');
	    	}
	    	
	    	if (interactionId === undefined)
	    		interactionId = $selectedInteraction.attr('id');
	    	
	    	if (interactionId === undefined)
	    		return;
	    	
	    	Chaplin.mediator.publish('addScreen', interactionId);
		},		    
	   	_fieldValueChanged: function(event) {
	    	var attributeName = event.target.name;
	    	var $controlGroup = this.$(event.target).closest('.control-group');
	    	var isNotEmpty = event.target.value != null && event.target.value != '';
	    	
	    	if (attributeName === undefined) 
	    		return;
	    	
	    	if (attributeName == 'processDefinitionKey') 
	    		this.$('ul.breadcrumb').find('li.active').html(event.target.value);
	    	
	    	var process = this.model.getProcess();	    	
	    	var previous;
	    	if (process !== undefined)
	    		previous = process.get(attributeName);
	    	
	    	if (previous === undefined || previous != event.target.value) {
	    		var attributes = {};
	    		attributes[attributeName] = event.target.value;
	    		
	    		process.save(attributes, {wait: true});
	    	}
	    	$controlGroup.toggleClass('hasData', isNotEmpty);
	    },
		_onAddedToDOM: function() {
			
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
		_onProcessSynced: function(process, options) {
			this.$(':input').each(function(i, element) {
				var name = element.name;
				if (name !== undefined && name != '') {
					var previous = element.value;
					var next = process.get(name);
					if (previous === undefined || previous != next) {
						if (name == 'processDefinitionKey' && this.model !== undefined) 
							this.model.get("collection").fetch();
						element.value = next;
					}
				}
			});
	   		
//	   		var interactionList = this.subview('interaction-list-view');
//			if (interactionList === undefined || interactionList.collection.length == 0) {
//				var interactions = process.get("interactions");
//				this.subview('interaction-list-view', new InteractionListView({collection: interactions}));
//			}
		},
		_onInteractionSynced: function(interaction, options) {
			var process = this.model.getProcess();
			var interactions = process.get("interactions");
			interactions.add(interaction);
		},
		_remove: function(event) {
			alert("remove!");
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

	return ProcessDesignerView;
});