define([
  'controllers/base/controller',
  'models/process',
  'models/processes',
  'models/screen',
  'models/user',
  'views/designer-view',
  'views/header-view',
  'views/intro-view',
  'views/process-detail-view',
  'views/screen-configure-view',
  'views/sidebar-view',
], function(Controller, Process, Processes, Screen, User, DesignerView, HeaderView, IntroView, ProcessDetailView, ScreenConfigureView, SidebarView) {
  'use strict';
  
  // FIXME: Just for development -- remove this!!!
  var process = new Process({id: '123', shortName: 'Testing'});
  var user = new User({displayName: 'Test User'});
  var DesignerController = Controller.extend({
	beforeAction: {
		'.*': function() {
			var processes = new Processes();
			processes.add(process);
			this.compose('header', HeaderView, {model: user});
			this.compose('designer', DesignerView, {model: processes});
			this.compose('sidebar', SidebarView, {model: processes});
		}
	},
	configure: function(params) {
		// FIXME: Just for development -- swap for lookup from api by id
		var screen = new Screen({id: params.screenId, title:'New employee form', url: 'http://localhost:8000/static/sample_form.html', process: process})
		this.view = new ScreenConfigureView({model: screen});
	},
	edit: function(params) {
		var model = process;
		// FIXME: Just for development
    	if (params.processDefinitionKey != "123")
    		model = new Process();
		var sidebarView = this.compose('sidebar');
		var processListView = sidebarView.subview('content');
		this.view = new ProcessDetailView({model: model});	
		processListView.listenTo(process, 'change', processListView.onProcessChanged);
	},
    index: function(params) {
    	var designerView = this.compose('designer');
    	this.view = new IntroView({model: designerView.model});
    },
  });

  return DesignerController;
});
