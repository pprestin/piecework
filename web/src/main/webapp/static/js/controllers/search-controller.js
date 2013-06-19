define([
  'controllers/base/controller',
  'models/design/process',
  'models/design/processes',
  'models/runtime/results',
  'models/design/screen',
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

        var url = resultsModel.url();
        var ResultsCollection = Collection.extend({
            url: url,
            parse: function(response, options) {
                return response.list;
            },
        });
        var collection = new ResultsCollection();
        collection.add(resultsModel.get("list"));

        var statusFilter = new SearchFilter({
            selector: 'parameters',
            options: [
                {id: "statusOpen", label: "Open", key: "processStatus", value: 'open', default: true},
                {id: "statusComplete", label: "Complete", key: "processStatus", value: 'complete'},
                {id: "statusCancelled", label: "Canceled", key: "processStatus", value: 'cancelled'},
                {id: "statusSuspended", label: "Suspended", key: "processStatus", value: 'suspended'},
                {id: "statusAny", label: "Any status", key: "processStatus", value: 'all' }
            ],
            results: resultsModel
        });

        var processFilter = new SearchFilter({
            selector: 'parameters',
            key: 'definitions',
            results: resultsModel
        });

        this.compose('searchView', SearchView, {model: resultsModel});

        this.compose('statusFilterContainer', {
            compose: function(options) {
                this.model = statusFilter;
                this.view = SearchFilterView;
                var autoRender, disabledAutoRender;
                this.item = new SearchFilterView({container: '.status-filter-container', model: this.model});
                autoRender = this.item.autoRender;
                disabledAutoRender = autoRender === void 0 || !autoRender;
                if (disabledAutoRender && typeof this.item.render === "function") {
                    return this.item.render();
                }
            },
            check: function(options) {
                return true;
            },
        });

        this.compose('processFilterContainer', {
            compose: function(options) {
                this.model = processFilter;
                this.view = SearchFilterView;
                var autoRender, disabledAutoRender;
                this.item = new SearchFilterView({container: '.process-filter-container', model: this.model});
                autoRender = this.item.autoRender;
                disabledAutoRender = autoRender === void 0 || !autoRender;
                if (disabledAutoRender && typeof this.item.render === "function") {
                    return this.item.render();
                }
            },
            check: function(options) {
                return true;
            },
        });

//        this.compose('statusFilterContainer', SearchFilterView, {container: '.status-filter-container', model: statusFilter})
//        this.compose('processFilterContainer', SearchFilterView, {container: '.process-filter-container', model: processFilter})
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

        var data = {};

        if (params.keyword !== undefined || params.status !== undefined || params.process !== undefined) {
            if (params.keyword !== undefined && params.keyword != 'none')
                data['keyword'] = params.keyword;
            if (params.status !== undefined && params.status != 'undefined')
                data['processStatus'] = params.status;
            if (params.process !== undefined && params.process != 'all')
                data['processDefinitionKey'] = params.process;

            collection.fetch({data: data});
        }
    },
  });

  return RuntimeController;
});