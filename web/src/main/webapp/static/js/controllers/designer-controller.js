define([
  'controllers/base/controller',
  'models/processes',
  'views/process-list-view',
], function(Controller, Processes, ProcessListView) {
  'use strict';

  var DesignerController = Controller.extend({
    index: function(params) {
      this.model = new Processes();
      this.view = new ProcessListView({model: this.model});
    }
  });

  return DesignerController;
});
