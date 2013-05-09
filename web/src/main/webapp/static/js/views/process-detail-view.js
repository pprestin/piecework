define([ 'models/process', 'models/screen', 'views/base/view', 'views/screen-list-view', 'text!templates/process-detail.hbs' ], 
         function(Process, Screen, View, ScreenListView, template ) {
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
	   		'click .edit-button': '_toggleEditing',
	   		'hide .accordion-group': '_toggleSection',
	   		'keypress .process-short-name': '_onKeyProcessShortName',
	   		'show .accordion-group': '_toggleSection',
	   	},
	   		    	
	   	initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
		},
		
		_addScreen: function(event) {
			this.trigger('onScreenChanged', new Screen());
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
			var screens = this.model.attributes.screens;
			screens.add(new Screen({title: 'Testing'}));
			var screenListView = new ScreenListView({collection: screens});
			this.subview('screens', screenListView);
			screenListView.listenTo(this, 'onScreenChanged', screenListView.onScreenChanged);
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
