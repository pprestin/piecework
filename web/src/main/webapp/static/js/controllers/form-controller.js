define([
  'chaplin',
  'controllers/base/controller',
  'models/base/model',
  'models/base/collection',
  'models/runtime/form',
  'models/notification',
  'models/runtime/page',
  'models/runtime/results',
  'models/runtime/search-filter',
  'models/user',
  'views/form/button-view',
  'views/form/button-link-view',
  'views/form/fields-view',
  'views/form/form-view',
  'views/form/grouping-view',
  'views/form/notification-view',
  'views/search/search-filter-view',
  'views/search/search-toolbar-view',
  'views/search/search-results-container-view',
  'views/search/search-results-view',
  'views/form/section-view',
  'views/form/sections-view',
  'views/user-view',
  'views/base/view',
  'text!templates/form/button.hbs',
  'text!templates/form/button-link.hbs'
], function(Chaplin, Controller, Model, Collection, Form, Notification, Page, Results, SearchFilter, User,
            ButtonView, ButtonLinkView, FieldsView, FormView, GroupingView, NotificationView,
            SearchFilterView, SearchToolbarView, SearchResultsContainerView, SearchResultsView,
            SectionView, SectionsView, UserView, View) {
  'use strict';

  var FormController = Controller.extend({
    beforeAction: function(params, route) {
        var user = window.piecework.context.user;
        this.compose('userModel', User, user);
        var userModel = this.compose('userModel');
        this.compose('userView', UserView, {model: userModel});
    },
    index: function(params, route) {
        params.ordinal = -1;
        this.step(params, route);
    },
    search: function(params) {
        var explanation = window.piecework.explanation;

        if (explanation != undefined) {
            var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
            this.view = new NotificationView({container: '.main-content', model: notification});
            return;
        }

        var model = window.piecework.model;

        if (model == null)
            model = {};

        if (model.total == undefined) {
            // If total is not defined, then replace with the search url
            model = { link: Chaplin.helpers.reverse('form#search', params) }
        }

        this.compose('resultsModel', Results, model);
        var resultsModel = this.compose('resultsModel');

        var collection = resultsModel.get("list");

        var url = resultsModel.get('link');
        if (/.html$/.test(url))
            url = url.substring(0, url.length - 5);
        var ResultsCollection = Collection.extend({
            url: url,
            parse: function(response, options) {
                return response.list;
            },
        });
        var resultsCollection = new ResultsCollection();
        resultsCollection.add(collection);

        this.compose('formView', {
                    compose: function(options) {

                    },
                    check: function(options) {
                        return false;
                    },
                    options: {
                        params: params
                    }
        });

        this.compose('searchResultsContainerView', SearchResultsContainerView, {model: resultsModel});

        this.view = new SearchResultsView({collection: resultsCollection});

        var data = {};

        if (resultsCollection.length == 0) {
            if (params.keyword !== undefined && params.keyword != '')
                data['keyword'] = params.keyword;
            if (params.processStatus !== undefined && params.processStatus != '')
                data['processStatus'] = params.processStatus;
            if (params.processDefinitionKey !== undefined && params.processDefinitionKey != '')
                data['processDefinitionKey'] = params.processDefinitionKey;

            resultsCollection.fetch({data: data});
        }
    },
    step: function(params, route) {
        var explanation = window.piecework.explanation;

        if (explanation != undefined) {
            var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
            this.view = new NotificationView({container: '.main-content', model: notification});
            return;
        }

        var requestId = params.requestId != undefined ? params.requestId : '';
        var formModel = this.compose('formModel');

        if (/.html$/.test(requestId))
            requestId = requestId.substring(0, requestId.length - 5);

        var model = window.piecework.model;

        if (model == null || model.total != undefined)
            model = {};

        this.compose('formView', {
            compose: function(options) {
                this.model = new Form(model);
                var link = this.model.get("link");
                var route = '/' + options.route.path;
                if (/.html$/.test(route))
                    route = route.substring(0, route.length - 5);

                var composer = this;

                var groupingIndex = 0;
                var currentScreen = params.ordinal;
                if (currentScreen != undefined) {
                    var screen = this.model.get("screen");
                    var screenType = screen != null ? screen.type : 'standard';
                    var groupingsLength = screen != null ? screen.groupings.length : null;
                    var parsedOrdinal = parseInt(currentScreen, 10);
                    if (parsedOrdinal == -1) {
                        if (screenType == 'staged')
                            parsedOrdinal = groupingsLength;
                        else
                            parsedOrdinal = 1;
                    }
                    groupingIndex = parsedOrdinal - 1;
                }
                this.model.set("groupingIndex", groupingIndex);

                if (link != route) {
                    this.model.set('link', route);
                    this.listenToOnce(this.model, 'sync', function() {
                        var autoRender, disabledAutoRender;
                        this.view = new FormView({model: this.model}, options);
                        autoRender = this.view.autoRender;
                        disabledAutoRender = autoRender === void 0 || !autoRender;
                        if (disabledAutoRender && typeof this.view.render === "function") {
                            this.view.render();
                        }
                        Chaplin.mediator.publish('groupingIndex:change', groupingIndex);
                    });
                    this.model.fetch({
                        error: function(model, response, options) {
                            switch (response.status) {
                            case 404:
                                var notification = new Notification({title: 'Form is no longer available', message: 'This task may already have been completed by you or another person, or else there was a problem with the link provided.'})
                                composer.view = new NotificationView({container: '.main-content', model: notification});
                                break;
                            }
                        }
                    });
                } else {
                    window.piecework._isInitialLoad = false;
                    var autoRender, disabledAutoRender;
                    this.view = new FormView({model: this.model}, options);
                    autoRender = this.view.autoRender;
                    disabledAutoRender = autoRender === void 0 || !autoRender;
                    if (disabledAutoRender && typeof this.view.render === "function") {
                        this.view.render();
                    }

                    Chaplin.mediator.publish('groupingIndex:change', groupingIndex);
                }

        //        if (this.model.get("valid") != true)
        //            currentScreen = screen.reviewIndex;
            },
            check: function(options) {
                var groupingIndex = 0;
                var currentScreen = options.params.ordinal;
                if (currentScreen != undefined)
                    groupingIndex = parseInt(currentScreen, 10) - 1;
                Chaplin.mediator.publish('groupingIndex:change', groupingIndex);
                return options.params.processDefinitionKey != undefined;
            },
            options: {
                formModel: formModel,
                params: params,
                route: route
            }
        });
    },
  });

  return FormController;
});