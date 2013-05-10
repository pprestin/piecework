define([ 'chaplin', 'models/screen', 'views/base/view', 'views/screen-list-view', 'text!templates/interaction-detail.hbs' ], 
		function(Chaplin, Screen, GroupingView, ScreenListView, template) {
	'use strict';

	var InteractionDetailView = GroupingView.extend({
		className: 'group-layout interaction selectable',
		tagName: 'li',
	    template: template,
	    listen: {
	    	addedToParent: '_addedToParent'
	    },
	    _addedToParent: function() {
	    	var view = this.subview('screen-list');
	    	if (view === undefined) {
		    	view = new ScreenListView({collection: this.model.get("screens"), container: '.interaction-content'});
		    	this.subview('screen-list', view);	    
	    	}
	    	var ordinal = this.model.get("ordinal");
	    	var tabindex = ordinal * 1000;
	    	this.$el.attr('id', this.model.cid);
	    	this.$el.attr('data-content', this.model.get("label"));
	    	this.$el.attr('tabindex', tabindex);
	    	this.$('.hastooltip').tooltip();
	    }
	});
	
	return InteractionDetailView;
});