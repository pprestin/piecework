define([
  'controllers/base/controller',
  'models/process',
  'models/processes',
  'models/runtime/results',
  'models/screen',
  'models/user',
  'models/base/collection',
  'views/runtime/limit-dropdown-view',
  'views/runtime/search-view',
  'views/runtime/search-results-view',
], function(Controller, Process, Processes, Results, Screen, User, Collection, LimitDropdownView, SearchView, SearchResultsView) {
  'use strict';
  
  var RuntimeController = Controller.extend({
    beforeAction: function(params, route) {
        var result = window.piecework.context;
        this.compose('resultsModel', Results, result);

        var resultsModel = this.compose('resultsModel');
        this.compose('searchView', SearchView, {model: resultsModel});
        this.compose('limitDropdownView', LimitDropdownView, {model: resultsModel})
    },
    search: function(params) {
        var resultsModel = this.compose('resultsModel');
        var collection = new Collection();
        collection.url = resultsModel.getUrl();
        collection.add(resultsModel.get("list"));
        this.view = new SearchResultsView({collection: collection});

        if (params.keyword !== undefined) {
            collection.fetch();
        }
    },
  });

  return RuntimeController;
});