define([
  'chaplin',
  'controllers/base/controller',
  'models/field',
  'models/fields',
  'models/interactions',
  'models/process',
  'models/processes',
  'models/screen',
  'models/selection',
  'models/sidebar',
  'models/user',
  'views/designer-view',
  'views/header-view',
  'views/interaction-list-view',
  'views/intro-view',
  'views/process-detail-view',
  'views/process-designer-view',
  'views/screen-detail-view',
  'views/sidebar-view',
], function(Chaplin, Controller, Field, Fields, Interactions, Process, Processes, Screen, Selection, Sidebar, User, DesignerView, HeaderView, 
		InteractionListView, IntroView, ProcessDetailView, ProcessDesignerView, ScreenDetailView, SidebarView) {
  'use strict';
  
  // FIXME: Just for development -- remove this!!!
  var field = new Field({id: '456', name: 'EmployeeName'});
  //var process = new Process({id: '123', processDefinitionKey: 'Testing'});
  var user = new User({displayName: 'Test User'});
  // FIXME: End section to remove
  
  var DesignerController = Controller.extend({
	beforeAction: function(params, route) {
		// Create a new interactions collection if one doesn't already exist
		this.compose('interactions', Interactions);
		// Create a new processes collection if one doesn't already exist
		this.compose('processes', Processes);
		// Retrieve it either way
		var collection = this.compose('processes');
		var interactions = this.compose('interactions');
		
		// Compose the header if it's not there
		this.compose('header', HeaderView, {model: user});
		
		var sidebar;
		var selection = new Selection({processDefinitionKey: params.processDefinitionKey, collection: collection, interactions: interactions});
		
		if (route.action == 'edit') {
			sidebar = new Sidebar({collection: collection, title: 'PROCESSES', type: 'process', actions: { add: "#designer/edit" }});
			this.compose('designer', ProcessDesignerView, {model: selection});
		} else if (route.action == 'index') {
			sidebar = new Sidebar({collection: collection, title: 'PROCESSES', type: 'process', actions: { add: "#designer/edit" }});
			this.compose('designer', ProcessDesignerView, {model: selection});
		} else if (route.action == 'screen') {
			var fields = new Fields();
			fields.add(field);
			sidebar = new Sidebar({collection: fields, title: 'FIELDS', type: 'screen', actions: { add: null }});
			this.compose('designer', DesignerView, {model: fields});
		}
		
		this.compose('sidebar', SidebarView, {model: sidebar});
	},
	edit: function(params) {
		var collection = this.compose('processes');
		var designer = this.compose('designer');
		var interactions = this.compose('interactions');
		
		designer.listenTo(collection, 'sync', designer.onCollectionReady);
		
		this.view = new InteractionListView({collection: interactions});
		
		// Fetch data for the collection if it is empty
		if (collection.length == 0) {
			collection.fetch();
		}
		
		
//		if (params.processDefinitionKey !== undefined)
//			Chaplin.mediator.publish('editProcess', params.processDefinitionKey);
		
//		var sidebarView = this.compose('sidebar');
//		var processListView = sidebarView.subview('content');
//		var collection = this.compose('processes');
//		var process;
//		if (params.processDefinitionKey !== undefined) {	
//			if (collection !== undefined && collection.length > 0)
//				process = collection.findWhere({processDefinitionKey: params.processDefinitionKey})
//			else {
//				process = new Process({processDefinitionKey: params.processDefinitionKey});
//			} 				
//		} else {
//			process = new Process();
//			process.save();
//		}
		
//		this.view = new ProcessDetailView({model: process});
//		processListView.listenTo(process, 'change:processDefinitionKey', processListView.onProcessDefinitionKeyChanged);
	},
    index: function(params) {
//    	var collection = this.compose('processes');
//		var sidebarView = this.compose('sidebar');
//    	var designerView = this.compose('designer');
//    	this.view = new IntroView({model: collection});
    	
    	var collection = this.compose('processes');
		var designer = this.compose('designer');
		
		designer.listenTo(collection, 'sync', designer.onCollectionReady);
		
		// Fetch data for the collection if it is empty
		if (collection.length == 0) {
			collection.fetch();
		}
    },
    screen: function(params) {
    	var process = new Process({id: '123', processDefinitionKey: 'Testing'});
    	var screenId = params.screenId;
    	var screen = new Screen({id: screenId, title:'New employee form', url: 'http://localhost:8000/static/sample_form.html', process: process})
		this.view = new ScreenDetailView({model: screen});
    },
    _onOnceProcessSynced: function(process, options) {
    	this.view = new ProcessDetailView({model: process});
		processListView.listenTo(process, 'change:processDefinitionKey', processListView.onProcessDefinitionKeyChanged);
    }
    
  });

  return DesignerController;
});
