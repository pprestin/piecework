define([
  'controllers/base/controller',
  'models/process',
  'models/processes',
  'models/screen',
  'models/user',
  'models/base/collection',
  'views/runtime/search-results-view',
], function(Controller, Process, Processes, Screen, User, Collection, SearchResultsView) {
  'use strict';
  
  var RuntimeController = Controller.extend({
    search: function(params) {
        var collection = new Collection({models:window.piecework.context.list});
        this.view = new SearchResultsView({collection: collection});
    },
  });

  return RuntimeController;
});