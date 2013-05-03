define([ 'chaplin', 'views/base/view', 'text!templates/sidebar.hbs', 'models/process', 'views/process-list-view', ], 
		function(Chaplin, View, template, Process, ProcessListView) {
	'use strict';

	var SidebarView = View.extend({
		autoRender : false,
		className: 'sidebar-nav well',
		region: 'sidebar',
	    template: template,
	    listen: {
	        addedToDOM: '_includeSubviews'
	    },
		_includeSubviews: function() {
			this.subview('content', new ProcessListView({collection: this.model}));
		}
	});

	return SidebarView;
});