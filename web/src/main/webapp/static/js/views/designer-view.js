define([ 'models/process', 'models/processes', 'views/base/view', 'views/process-detail-view', 'views/sidebar-view', 'text!templates/designer.hbs' ], 
		function(Process, Processes, View, ProcessDetailView, SidebarView, template) {
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
	});

	return DesignerView;
});