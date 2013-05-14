define([ 'chaplin', 'models/screen', 'views/base/view', 'views/screen-list-view', 'text!templates/interaction-detail.hbs' ], 
		function(Chaplin, Screen, GroupingView, ScreenListView, template) {
	'use strict';

	var InteractionDetailView = GroupingView.extend({
		className: 'group-layout interaction selectable',
		tagName: 'li',
	    template: template,
	    listen: {
	    	'addedToParent': '_addedToParent',
	    	'sync model': '_onModelSynced'
	    },
	    _addedToParent: function() {
	    	var ordinal = this.model.get("ordinal");
	    	var tabindex = ordinal * 1000;
	    	this.$el.attr('id', this.model.id);
	    	this.$el.attr('data-content', this.model.get("label"));
	    	this.$el.attr('tabindex', tabindex);
	    	this.$('.hastooltip').tooltip();
	    },
	    _onModelSynced: function(model, options) {
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
			var view = this.subview('screen-list');
	    	if (view === undefined) {
	    		var screens = this.model.get("screens");
				screens.add(new Screen());
		    	view = new ScreenListView({collection: screens, container: '.interaction-content'});
		    	this.subview('screen-list', view);	    
	    	}
		},
	});
	
	return InteractionDetailView;
});