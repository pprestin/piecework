define([ 'models/process', 'models/screen', 'views/base/view', 'views/screen-list-view', 'text!templates/process-detail.hbs' ], 
         function(Process, Screen, View, ScreenListView, template ) {
	'use strict';

	var ProcessDetailView = View.extend({
		autoRender : true,
		container: '#main-frame',
	    template: template,
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	   	events: {
	   		'change input': '_fieldValueChanged',
	   		'click .add-screen-button': '_addScreen',
	   		'click .edit-button': '_toggleEditing',
	   		'hide .accordion-group': '_toggleSection',
	   		'show .accordion-group': '_toggleSection',
	   	},
	   		    	
	   	initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
		},
		
		render: function(options) {
			View.__super__.render.apply(this, options);
			// Ensure that the input elements are populated
			var model = this.model;
			this.$(':input').each(function(i, element) {
				var name = element.name;
				if (name !== undefined && name != '')
					element.value = model.attributes[name];
			});
		},
		
		_addScreen: function(event) {
//			var screenListView = this.subview('screens');
//			if (screenListView == undefined) {
//				var screens = this.model.attributes.screens;
//				screens.add(new Screen({title: 'Testing'}));
//				screenListView = new ScreenListView({collection: screens});
//				this.subview('screens', screenListView);
//				screenListView.listenTo(this, 'onScreenChanged', screenListView.onScreenChanged);
//				for (var i=0;i<screens.length;i++) {
//					this.trigger('onScreenChanged', screens[i]);
//				}
//			}
			this.trigger('onScreenChanged', new Screen());
		},
			    
	   	_fieldValueChanged: function(event) {
	    	var attributeName = event.target.name;
	    	var attributes = {};
	    	var $controlGroup = this.$(event.target).closest('.control-group');
	    	var isNotEmpty = event.target.value != null && event.target.value != '';
	    	
	    	if (attributeName === undefined) 
	    		return;
				    	
	    	attributes[attributeName] = event.target.value;
	    	this.model.set(attributes);	
	    	$controlGroup.toggleClass('hasData', isNotEmpty);
	    },
	    
		_onAddedToDOM: function() {
			var screens = this.model.attributes.screens;
			screens.add(new Screen({title: 'Testing'}));
			var screenListView = new ScreenListView({collection: screens});
			this.subview('screens', screenListView);
			screenListView.listenTo(this, 'onScreenChanged', screenListView.onScreenChanged);
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
