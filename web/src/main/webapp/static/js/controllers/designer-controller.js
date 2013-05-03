define([
  'controllers/base/controller',
  'models/process',
  'models/processes',
  'views/designer-view',
  'views/intro-view',
  'views/process-detail-view',
  'views/sidebar-view',
], function(Controller, Process, Processes, DesignerView, IntroView, ProcessDetailView, SidebarView) {
  'use strict';
  
  // FIXME: Just for development -- remove this!!!
  var process = new Process({shortName: 'Testing'});
  var DesignerController = Controller.extend({
	beforeAction: {
		'.*': function() {
			
			var processes = new Processes();
			processes.add(process);
			this.compose('designer', DesignerView, {model: processes});
			this.compose('sidebar', SidebarView, {model: processes});
		}
	},
    index: function(params) {
    	var designerView = this.compose('designer');
    	this.view = new IntroView({model: designerView.model});
    },
//    edit: function(params) {
//    	var process = new Process();
//		var sidebarView = this.compose('sidebar');
//		var processListView = sidebarView.subview('content');
//		this.view = new ProcessDetailView({model: process});	
//		processListView.listenTo(process, 'change', processListView.onProcessChanged);
//    },
	start: function(params) {
//		var process = new Process();
		var sidebarView = this.compose('sidebar');
		var processListView = sidebarView.subview('content');
		this.view = new ProcessDetailView({model: process});	
		processListView.listenTo(process, 'change', processListView.onProcessChanged);
	}
  });

  return DesignerController;
});
