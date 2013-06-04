define([
  'controllers/base/controller',
  'models/process',
  'models/processes',
  'models/runtime/results',
  'models/screen',
  'models/runtime/search-filter',
  'models/user',
  'models/base/collection',
  'views/runtime/search-filter-view',
  'views/runtime/search-view',
  'views/runtime/search-results-view',
], function(Controller, Process, Processes, Results, Screen, SearchFilter, User, Collection, SearchFilterView, SearchView, SearchResultsView) {
  'use strict';
  
  var RuntimeController = Controller.extend({
    beforeAction: function(params, route) {
        var result = window.piecework.context.resource;
        this.compose('resultsModel', Results, result);
        var resultsModel = this.compose('resultsModel');

        var statusFilter = new SearchFilter({
            selector: 'parameters',
            options: [
                {label: "Open", key: "complete", value: 'false'},
                {label: "Complete", key: "complete", value: 'true'},
                {label: "Canceled", key: "canceled", value: 'true'},
                {label: "All" }
            ],
            results: resultsModel
        });

        var processFilter = new SearchFilter({
            key: 'definitions',
            results: resultsModel
        });

        this.compose('searchView', SearchView, {model: resultsModel});
        this.compose('statusFilterContainer', SearchFilterView, {container: '.status-filter-container', model: statusFilter})
        this.compose('processFilterContainer', SearchFilterView, {container: '.process-filter-container', model: processFilter})
    },
    search: function(params) {
        var resultsModel = this.compose('resultsModel');
        var url = resultsModel.url();
        var ResultsCollection = Collection.extend({
            url: url,
            parse: function(response, options) {
                return response.list;
            },
        });
        var collection = new ResultsCollection();
        collection.add(resultsModel.get("list"));
        this.view = new SearchResultsView({collection: collection});

        if (params.keyword !== undefined) {
            collection.fetch();
        }
    },
  });

  return RuntimeController;
});