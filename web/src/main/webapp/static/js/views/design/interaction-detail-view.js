define([ 'chaplin', 'models/screen', 'models/screens', 'views/base/collection-view', 'views/base/view', 'views/screen-item-view', 'views/screen-list-view', 'text!templates/interaction-detail.hbs' ], 
		function(Chaplin, Screen, Screens, CollectionView, View, ScreenItemView, ScreenListView, template) {
	'use strict';

	var InteractionDetailView = View.extend({
		autoRender: false,
		className: 'group-layout interaction selectable',
		tagName: 'li',
	    template: template,
	    listen: {
	    	'addScreen mediator': '_onAddScreen',
	    	'addedToParent': '_addedToParent',
	    	'processReady mediator': '_onProcessReady',
	    	'sync model': '_onInteractionModelSynced',
	    	
	    },
	    events: {
	    	'show #process-interactions': '_onVisibilityChange',
	    },
	    initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
		},
		render: function(options) {
	   		View.__super__.render.apply(this, options);
	   		
	   		this.subview('screen-list-view', new ScreenListView({collection: new Screens()}));
	   		
	   		var screens = this.subview('screen-list-view').collection;
	   		var screenArray = this.model.get("screens");
    		for (var i=0;i<screenArray.length;i++) {
    			screens.add(screenArray[i]);
    		}
	   		return this;
		},
	    _addedToParent: function() {
	    	var ordinal = this.model.get("ordinal");
	    	var tabindex = ordinal * 1000;
	    	this.$el.attr('id', this.model.id);
	    	this.$el.attr('data-content', this.model.get("label"));
	    	this.$el.attr('tabindex', tabindex);
	    	this.$('.hastooltip').tooltip();
	    	
	   		this.subview('screen-list-view', new ScreenListView({collection: new Screens()}));
	   		
	   		var screens = this.subview('screen-list-view').collection;
	   		var screenArray = this.model.get("screens");
    		for (var i=0;i<screenArray.length;i++) {
    			screens.add(screenArray[i]);
    		}
	    },
	    _onAddScreen: function(interactionId) {
	    	// Check to see if this model is the one that needs to respond to the add screen event
	    	if (this.model === undefined || this.model.id != interactionId) 
	    		return;
	    	
	    	var processDefinitionKey = this.model.get('processDefinitionKey');
	    	var screens = this.model.get("screens");
	    	var ordinal = screens == null ? 1 : screens.length;
	    	var screen = new Screen({processDefinitionKey: processDefinitionKey, interactionId: this.model.id, ordinal: ordinal});
	    	var screens = this.subview('screen-list-view').collection;
	    	screens.add(screen);
	    	
	    	this.listenTo(screen, 'sync', this._onScreenModelSynced);
//	    	screens.add(screen);
	    	screen.save();
	    },
	    _onInteractionModelSynced: function(model, options) {
//			var model = this.model;
//			this.$(':input').each(function(i, element) {
//				var name = element.name;
//				if (name !== undefined && name != '') {
//					var previous = element.value;
//					var next = model.get(name);
//					if (previous === undefined || previous != next)
//						element.value = next;
//				}
//			});
//	    	this.render();
		},
		_onProcessReady: function(process) {
//			var screenList = this.subview('screen-list-view');
//	    	if (screenList === undefined || screenList.collection.length == 0) {
//	    		var screens = this.model.get("screens");
//		    	this.subview('screen-list-view', new ScreenListView({collection: screens}));
//	    		//new ScreenListView({collection: screens});
//	    	}
		},
		_onScreenModelSynced: function(screen, options) {
//			var screens = this.model.get("screens");
//			screens.add(screen);
//			this.subview('screen-list-view', new ScreenListView({collection: screens}));
		},
		_onVisibilityChange: function() {
			this.subview('screen-list-view', new ScreenListView({collection: new Screens()}));
	   		
	   		var screens = this.subview('screen-list-view').collection;
	   		var screenArray = this.model.get("screens");
    		for (var i=0;i<screenArray.length;i++) {
    			screens.add(screenArray[i]);
    		}	
		},
	});
	
	return InteractionDetailView;
});