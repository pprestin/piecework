define([
  'chaplin',
  'controllers/base/controller',
  'models/design/field',
  'models/design/fields',
  'models/design/interactions',
  'models/design/process',
  'models/design/processes',
  'models/design/screen',
  'models/selection',
  'models/design/sidebar',
  'models/user',
  'views/design/designer-view',
  'views/header-view',
  'views/design/interaction-list-view',
  'views/intro-view',
  'views/design/process-detail-view',
  'views/design/process-designer-view',
  'views/design/screen-detail-view',
  'views/design/sidebar-view',
  'views/user-view',
], function(Chaplin, Controller, Field, Fields, Interactions, Process, Processes, Screen, Selection, Sidebar,
        User, DesignerView, HeaderView, InteractionListView, IntroView, ProcessDetailView, ProcessDesignerView,
        ScreenDetailView, SidebarView, UserView) {
    'use strict';

    var DesignerController = Controller.extend({
        beforeAction: function(params, route) {
            var user = window.piecework.context.user;
            this.compose('userModel', User, user);
            var userModel = this.compose('userModel');
            this.compose('userView', UserView, {model: userModel});
        },
        index: function(params, route) {
            this.compose('interactions', Interactions);
            this.compose('processes', Processes);
            var interactions = this.compose('interactions');
            var processes = this.compose('processes');
            var selection = new Selection({processDefinitionKey: params.processDefinitionKey, collection: processes, interactions: interactions});

            this.compose('designer', ProcessDesignerView, {model: selection});

            // Fetch data for the collection if it is empty
            if (processes.length == 0) {
                processes.fetch();
            }
      },
    });

    return DesignerController;
});