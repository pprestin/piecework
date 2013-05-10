define([
  'controllers/base/controller',
  'models/field',
  'models/fields',
  'models/process',
  'models/processes',
  'models/screen',
  'models/sidebar',
  'models/user',
  'views/designer-view',
  'views/header-view',
  'views/intro-view',
  'views/process-detail-view',
  'views/screen-detail-view',
  'views/sidebar-view',
], function(Controller, Field, Fields, Process, Processes, Screen, Sidebar, User, DesignerView, HeaderView, 
		    IntroView, ProcessDetailView, ScreenDetailView, SidebarView) {
  'use strict';
  
  // FIXME: Just for development -- remove this!!!
  var field = new Field({id: '456', name: 'EmployeeName'});
//  var process = new Process({id: '123', processDefinitionKey: 'Testing'});
  var user = new User({displayName: 'Test User'});
  // FIXME: End section to remove
  
  var DesignerController = Controller.extend({
	beforeAction: {
		'edit': function() {
			var processes = new Processes();
			var sidebar = new Sidebar({collection: processes, title: 'PROCESSES', type: 'process', actions: { add: "#designer/edit" }});
			this.compose('header', HeaderView, {model: user});
			this.compose('designer', DesignerView, {model: processes});
			this.compose('sidebar', SidebarView, {model: sidebar});
		},
		'index': function() {
			var processes = new Processes();
			var sidebar = new Sidebar({collection: processes, title: 'PROCESSES', type: 'process', actions: { add: "#designer/edit" }});
			this.compose('header', HeaderView, {model: user});
			this.compose('designer', DesignerView, {model: processes});
			this.compose('sidebar', SidebarView, {model: sidebar});
		},
		'screen': function() {
			var fields = new Fields();
			fields.add(field);
			var sidebar = new Sidebar({collection: fields, title: 'FIELDS', type: 'screen', actions: { add: null }});
			this.compose('header', HeaderView, {model: user});
			this.compose('designer', DesignerView, {model: fields});
			this.compose('sidebar', SidebarView, {model: sidebar});
		}
	},
	edit: function(params) {
		var sidebarView = this.compose('sidebar');
		var processListView = sidebarView.subview('content');
		var collection = processListView.collection;
		var process;
		if (params.processDefinitionKey !== undefined) {	
			if (collection !== undefined && collection.length > 0)
				process = collection.findWhere({processDefinitionKey: params.processDefinitionKey})
			else {
				process = new Process({processDefinitionKey: params.processDefinitionKey});
				process.fetch();
			} 
				
		} else {
			process = new Process();
		}
		
		this.view = new ProcessDetailView({model: process});	
		processListView.listenTo(process, 'change:processDefinitionKey', processListView.onProcessDefinitionKeyChanged);
	},
    index: function(params) {
    	var designerView = this.compose('designer');
    	this.view = new IntroView({model: designerView.model});
    },
    screen: function(params) {
    	var process = new Process({id: '123', processDefinitionKey: 'Testing'});
    	var screenId = params.screenId;
    	var screen = new Screen({id: screenId, title:'New employee form', url: 'http://localhost:8000/static/sample_form.html', process: process})
		this.view = new ScreenDetailView({model: screen});
    }
  });

  return DesignerController;
});
