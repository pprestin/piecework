define([ 'chaplin', 'models/screen', 'views/base/view', 'views/screen-list-view', 'text!templates/interaction-detail.hbs' ], 
		function(Chaplin, Screen, View, ScreenListView, template) {
	'use strict';

	var InteractionDetailView = View.extend({
		className: 'group-layout interaction selectable',
		tagName: 'li',
	    template: template,
	    listen: {
	    	'addScreen mediator': '_onAddScreen',
	    	'addedToParent': '_addedToParent',
	    	'sync model': '_onInteractionModelSynced'
	    },
	    initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
		},
	    _addedToParent: function() {
	    	var ordinal = this.model.get("ordinal");
	    	var tabindex = ordinal * 1000;
	    	this.$el.attr('id', this.model.id);
	    	this.$el.attr('data-content', this.model.get("label"));
	    	this.$el.attr('tabindex', tabindex);
	    	this.$('.hastooltip').tooltip();
	    	
	    	var view = this.subview('screen-list');
	    	if (view === undefined) {
	    		var screens = this.model.get("screens");
		    	view = new ScreenListView({collection: screens, container: '.interaction-content'});
		    	this.subview('screen-list', view);	    
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
	    	
	    	this.listenTo(screen, 'sync', this._onScreenModelSynced);
//	    	screens.add(screen);
	    	screen.save();
	    },
	    _onInteractionModelSynced: function(model, options) {
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
		_onScreenModelSynced: function(screen, options) {
			var interaction = this.model;
			var screens = interaction.get("screens");
			screens.add(screen);
		},
	});
	
	return InteractionDetailView;
});