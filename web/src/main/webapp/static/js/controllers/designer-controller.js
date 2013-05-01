define([
  'controllers/base/controller',
  'views/designer-view',
  'models/processes',
  'views/process-list-view',
  'views/process-detail-view',
], function(Controller, DesignerView, Processes, ProcessListView, ProcessDetailView) {
  'use strict';

  var DesignerController = Controller.extend({
	beforeAction: {
		'.*': function() {
			this.compose('designer-view', DesignerView);
		}
	},
    index: function(params) {
      this.model = new Processes();
      this.view = new ProcessListView({autoRender: true, model: this.model});
//      this.view.subview('process-detail', new ProcessDetailView({model: this.model}));
    }
  });

  return DesignerController;
});
