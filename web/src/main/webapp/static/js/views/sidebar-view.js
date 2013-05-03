define([ 'chaplin', 'views/base/view', 'text!templates/sidebar.hbs', 'models/process', 'views/process-list-view', ], 
		function(Chaplin, View, template, Process, ProcessListView) {
	'use strict';

	var SidebarView = View.extend({
		autoRender : true,
		container: '.sidebar-nav',
	    template: template,
	    events: {
			'click': 'onStartButton',
		},
	   	initialize: function(options) {
	   		View.__super__.initialize.apply(this, options);
			this.subview('content', new ProcessListView({collection: this.model}));
		},
		onStartButton: function() {
			var contentView = this.subview('content');
			contentView.onProcessDesign();
			
			//Chaplin.mediator.publish('newProcess', '');
		}
	});

	return SidebarView;
});