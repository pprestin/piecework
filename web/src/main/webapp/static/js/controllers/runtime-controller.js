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
  
  var RuntimeController = Controller.extend({
    search: function(params) {
//	  	var screen = new Screen({});
//    	this.view = new ScreenConfigureView({model: screen});
    },
  });

  return RuntimeController;
});