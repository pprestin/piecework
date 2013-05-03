define([ 'models/process', 'models/processes', 'views/base/view', 'views/process-detail-view', 'views/sidebar-view', 'text!templates/designer.hbs' ], 
		function(Process, Processes, View, ProcessDetailView, SidebarView, template) {
	'use strict';

	var DesignerView = View.extend({
		autoRender: true,
		container : '#main-screen',
//		events: {
//			'click .start-button': 'onCreateProcess',
//		},
		id : 'designer-view',
		template : template,
//		initialize: function(options) {
//			View.__super__.initialize.apply(this, options);
//			//var collection = new Processes();
//			//this.subview('sidebar', new SidebarView({model: collection}));
//		},
		regions : {
			'#left-frame' : 'left',
			'#main-frame' : 'main'
		},

	});

	return DesignerView;
});