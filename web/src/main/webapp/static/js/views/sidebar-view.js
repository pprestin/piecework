define([ 'chaplin',
         'views/design/field-list-view',
         'views/design/process-list-view',
         'views/base/view',
         'text!templates/sidebar.hbs' ],
		function(Chaplin, FieldListView, ProcessListView, View, template) {
	'use strict';

	var SidebarView = View.extend({
		autoRender : true,
		className: 'sidebar-nav well',
		region: 'sidebar',
	    template: template,
	    listen: {
	        'addedToDOM': '_addedToDOM'
	    },
	    _addedToDOM: function() {
			if (this.model.attributes.type == 'process')
				this.subview('content', new ProcessListView({collection: this.model.get("collection")}));
			else
				this.subview('content', new FieldListView({collection: this.model.get("collection")}));
		}
	});

	return SidebarView;
});