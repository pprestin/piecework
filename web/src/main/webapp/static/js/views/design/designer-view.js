define([ 'models/process', 'models/processes', 'models/sidebar', 'views/base/view', 'views/intro-view', 'views/field-list-view', 'views/process-list-view', 'views/process-detail-view', 'views/sidebar-view', 'text!templates/designer.hbs' ], 
		function(Process, Processes, Sidebar, View, IntroView, FieldListView, ProcessListView, ProcessDetailView, SidebarView, template) {
	'use strict';

	var DesignerView = View.extend({
		autoRender: true,
		container : '#main-screen',
		id : 'designer-view',
		template : template,
		regions : {
			'#left-frame' : 'sidebar',
			'#main-frame' : 'main',
			'.sidebar-content' : 'sidebar-content',
			'.screen-list' : 'screen-list',
		},
//	   	initialize: function(options) {
//	   		View.__super__.initialize.apply(this, options);
//	   	},
//	   	listen: {
//	        'addedToDOM': '_addedToDOM'
//	    },
//	    _addedToDOM: function() {
//	    	var sidebar = new Sidebar({collection: this.model, title: 'PROCESSES', type: 'process', actions: { add: "#designer/edit" }});
//	    	//this.subview('sidebar-view', new ProcessListView({container: '#left-frame', collection: this.model}));
//	   		this.subview('sidebar-view', new SidebarView({container: '#left-frame', model: sidebar}));
//	   		this.subview('main-view', new IntroView({container: '#main-frame', model: this.model}));
//	   	}
	});

	return DesignerView;
});