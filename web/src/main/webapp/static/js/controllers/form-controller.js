define([
  'controllers/base/controller',
  'models/runtime/form',
  'models/runtime/page',
  'views/runtime/form-view',
  'views/runtime/head-view'
], function(Controller, Form, Page, FormView, HeadView) {
  'use strict';

  var FormController = Controller.extend({
    beforeAction: function(params, route) {
        this.compose('formModel', Form, window.piecework.context.resource);
        this.compose('pageModel', Page, window.piecework.context);

        var pageModel = this.compose('pageModel');
        this.compose('headView', HeadView, {model: pageModel});
    },
    index: function(params) {
        var formModel = this.compose('formModel');
        this.view = new FormView({model: formModel});
    },
  });

  return FormController;
});