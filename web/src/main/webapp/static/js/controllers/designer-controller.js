define([
  'controllers/base/controller',
  'models/processes',
  'views/designer-view',
  'views/sidebar-view',
], function(Controller, Processes, DesignerView, SidebarView) {
  'use strict';

  var DesignerController = Controller.extend({
	beforeAction: {
		'.*': function() {
			this.compose('designer-view', DesignerView);
		}
	},
    index: function(params) {
    	var collection = new Processes();
    	this.view = new SidebarView({model: collection});
    }
  });

  return DesignerController;
});
