define([
  'chaplin',
  'controllers/base/controller',
  'models/base/model',
  'models/base/collection',
  'models/runtime/form',
  'models/runtime/page',
  'models/runtime/results',
  'models/runtime/search-filter',
  'models/user',
  'views/form/button-view',
  'views/form/button-link-view',
  'views/form/fields-view',
  'views/form/form-view',
  'views/form/grouping-view',
  'views/runtime/head-view',
  'views/runtime/search-filter-view',
  'views/runtime/search-view',
  'views/runtime/search-results-container-view',
  'views/runtime/search-results-view',
  'views/form/section-view',
  'views/form/sections-view',
  'views/user-view',
  'views/base/view',
  'text!templates/form/button.hbs',
  'text!templates/form/button-link.hbs'
], function(Chaplin, Controller, Model, Collection, Form, Page, Results, SearchFilter, User,
            ButtonView, ButtonLinkView, FieldsView, FormView,
            GroupingView, HeadView, SearchFilterView, SearchView, SearchResultsContainerView, SearchResultsView,
            SectionView, SectionsView, UserView, View) {
  'use strict';

  var FormController = Controller.extend({
    beforeAction: function(params, route) {
        var user = window.piecework.context.user;
        this.compose('userModel', User, user);
        var userModel = this.compose('userModel');
        this.compose('userView', UserView, {model: userModel});
    },
    index: function(params) {
        this.step(params);
    },
    search: function(params) {
        var model = window.piecework.model;
        var collection = window.piecework.collection;

        this.compose('resultsModel', Results, model);
        var resultsModel = this.compose('resultsModel');

        var url = resultsModel.url();
        var ResultsCollection = Collection.extend({
            url: url,
            parse: function(response, options) {
                return response.list;
            },
        });
        var resultsCollection = new ResultsCollection();
        resultsCollection.add(collection);

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

        this.compose('searchView', SearchView, {model: resultsModel});
        this.compose('searchResultsContainerView', SearchResultsContainerView, {model: resultsModel});
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
        var resultsModel = this.compose('resultsModel');
        var url = resultsModel.url();
        var ResultsCollection = Collection.extend({
            url: url,
            parse: function(response, options) {
                return response.list;
            },
        });

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
    step: function(params) {
        var formView = this.compose('formView');
        var groupingIndex = 0;
        var currentScreen = params.ordinal;
        if (currentScreen != undefined)
            groupingIndex = parseInt(currentScreen, 10) - 1;

        this.compose('formView', {
            compose: function(options) {
                var link = window.piecework.model.link;
                var requestId = params.requestId != undefined ? params.requestId : '';

                if (/.html$/.test(requestId))
                    requestId = requestId.substring(0, requestId.length - 5);

                if (/\/form$/.test(link))
                    link += '/' + params.processDefinitionKey + '/' + requestId;

                this.model = new Form(window.piecework.context.resource);
                this.view = FormView;

                this.check(options);

        //        if (this.model.get("valid") != true)
        //            currentScreen = screen.reviewIndex;

                var screen = this.model.get("screen");
                if (screen !== undefined) {
                    var autoRender, disabledAutoRender;
                    this.item = new FormView({model: this.model}, options);
                    autoRender = this.item.autoRender;
                    disabledAutoRender = autoRender === void 0 || !autoRender;
                    if (disabledAutoRender && typeof this.item.render === "function") {
                        return this.item.render();
                    }
                } else {
                    this.model.set("link", link);
                    this.listenToOnce(this.model, 'sync', function() {
                        var autoRender, disabledAutoRender;
                        this.item = new FormView({model: this.model}, options);
                        autoRender = this.item.autoRender;
                        disabledAutoRender = autoRender === void 0 || !autoRender;
                        if (disabledAutoRender && typeof this.item.render === "function") {
                          return this.item.render();
                        }
                    });
                    this.model.fetch();
                }

            },
            check: function(options) {
                var groupingIndex = 0;
                var currentScreen = options.params.ordinal;
                if (currentScreen != undefined)
                    groupingIndex = parseInt(currentScreen, 10) - 1;
                this.model.set("groupingIndex", groupingIndex);
                return options.params.processDefinitionKey != undefined;
            },
            options: {
                params: params
            }
        });

        Chaplin.mediator.publish('groupingIndex:change', groupingIndex);

//        this.compose('formModel', Form, window.piecework.context.resource);
//        this.compose('pageModel', Page, window.piecework.context);
//
//        var formModel = this.compose('formModel');
//        var pageModel = this.compose('pageModel');
        //        this.compose('headView', HeadView, {model: pageModel});

//        var screenModel = formModel.get("screen");
//        var sections = screenModel.sections;
//
//        var groupingIndex = 0;
//        var currentScreen = params.ordinal;
//
//        if (formModel.get("valid") != true)
//            currentScreen = screenModel.reviewIndex;
//
//        if (currentScreen != undefined)
//            groupingIndex = parseInt(currentScreen, 10) - 1;
//
//        var groupings = screenModel.groupings;
//        var grouping = groupings != undefined && groupings.length > groupingIndex ? groupings[groupingIndex] : { sectionIds : []};
//        var includeAll = groupings.length < 1;
//
//        var sectionVisibleMap = {};
//        var lastVisibleSection;
//        var pageLink = formModel.get("link");
//
//        for (var i=0;i<groupings.length;i++) {
//            groupings[i].breadcrumbLink = pageLink + '/step/' + groupings[i].ordinal;
//        }
//
//        for (var i=0;i<grouping.sectionIds.length;i++) {
//            sectionVisibleMap[grouping.sectionIds[i]] = true;
//            lastVisibleSection = grouping.sectionIds[i];
//        }
//
//        this.compose('formView', {
//            compose: function(options) {
//                this.model = formModel;
//                this.view = FormView;
//                var autoRender, disabledAutoRender;
//                this.item = new FormView({model: this.model});
//                autoRender = this.item.autoRender;
//                disabledAutoRender = autoRender === void 0 || !autoRender;
//                if (disabledAutoRender && typeof this.item.render === "function") {
//                    return this.item.render();
//                }
//            },
//            check: function(options) {
//                return true;
//            },
//        });

//        $('.section').removeClass('selected');
//        $('.section').addClass('hide');
//
//        if (grouping != null) {
//            var groupingId = grouping.groupingId;
//
//            this.compose(groupingId, GroupingView, {model: new Model(grouping)});
//        }
//
//        var formDataMap = {};
//        var formData = formModel.get("formData");
//
//        if (formData != undefined) {
//            for (var i=0;i<formData.length;i++) {
//                var formValue = formData[i];
//                formDataMap[formValue.name] = formValue;
//            }
//        }
//
//        if (sections != null) {
//            for (var i=0;i<sections.length;i++) {
//                var section = sections[i];
//
//                if (section == null)
//                    continue;
//
//                var sectionId = section.sectionId;
//                var sectionType = section.type;
//                var tagId = section.tagId;
//                var fields = section.fields;
//                var sectionViewId = tagId != null ? tagId : sectionId;
//                var contentSelector = '#' + sectionViewId + " > .section-content";
//                var showButtons = (i+1) == sections.length;
//                var className;
//
//                var doShow = sectionVisibleMap[sectionId] != undefined;
//
//                if (doShow)
//                    className = 'section selected';
//                else
//                    className = 'section hide';
//
//                if (fields != undefined) {
//                    for (var j=0;j<fields.length;j++) {
//                        var field = fields[j];
//                        var formValue = formDataMap[field.name];
//
//                        if (formValue != undefined) {
//                            var values = formValue.values;
//                            for (var n=0;n<values.length;n++) {
//                                var value = values[n];
//                                field.value = value;
//                                break;
//                            }
//                            var messages = formValue.messages;
//                            if (messages != undefined && messages.length > 0) {
//                                field.messages = messages;
//                                for (var m=0;m<messages.length;m++) {
//                                    field.messageType = messages[m].type;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//
//                this.compose(sectionViewId, {
//                    compose: function(options) {
//                        this.model = new Model(section);
//                        this.view = SectionView;
//                        var autoRender, disabledAutoRender;
//                        this.item = new SectionView({id: sectionViewId, className: className, container: 'ul.sections', model: this.model});
//                        autoRender = this.item.autoRender;
//                        disabledAutoRender = autoRender === void 0 || !autoRender;
//                        if (disabledAutoRender && typeof this.item.render === "function") {
//                            return this.item.render();
//                        }
//                    },
//                    check: function(options) {
//                        if (options.show) {
//                            this.item.$el.addClass('selected');
//                            this.item.$el.removeClass('hide');
//                        } else {
//                            this.item.$el.removeClass('selected');
//                            this.item.$el.addClass('hide');
//                        }
//                        return true;
//                    },
//                    options: {
//                        show: doShow
//                    }
//                });
//
//                this.compose('fieldsView_' + sectionId, {
////                    options: { collection: new Collection(fields), container: contentSelector },
//                    compose: function(options) {
//                       this.collection = new Collection(fields);
//                       var autoRender, disabledAutoRender;
//                       this.item = new FieldsView({collection: this.collection, container: contentSelector });
//                       autoRender = this.item.autoRender;
//                       disabledAutoRender = autoRender === void 0 || !autoRender;
//                       if (disabledAutoRender && typeof this.item.render === "function") {
//                           return this.item.render();
//                       }
//                    },
//                    check: function(options) {
//                        return true;
//                    }
//                });
//            }
//
//            if (grouping.buttons != undefined && grouping.buttons.length > 0) {
//              var buttons = grouping.buttons;
//              for (var b=0;b<buttons.length;b++) {
//                  var button = buttons[b];
//                  var buttonId = button.buttonId;
//
//                  if (button.value != undefined) {
//                      if (button.value == 'next') {
//                          button.value = groupings.length > grouping.ordinal ? groupings[groupingIndex + 1].breadcrumbLink : '';
//                          button.link = button.value;
//                          button.alt = "Next";
//                      } else if (buttons[b].value == 'prev') {
//                          if (groupingIndex > 0)
//                            button.value = groupings[groupingIndex - 1].breadcrumbLink;
//                          else {
//                            var rootLink = pageLink;
//                            var indexOfLastSlash = rootLink.lastIndexOf('/');
//                            if (indexOfLastSlash != -1 && indexOfLastSlash < rootLink.length)
//                                rootLink = rootLink.substring(0, indexOfLastSlash);
//                            button.value = rootLink;
//                          }
//                          button.link = button.value;
//                          button.alt = "Previous";
//                      }
//                  }
//
//                  if (button.type == 'button-link')
//                      this.compose('buttons_' + buttonId, ButtonLinkView, {model: new Model(button)});
//                  else
//                      this.compose('buttons_' + buttonId, ButtonView, {model: new Model(button)});
//              }
//            }  else if (groupings.length > groupingIndex+1) {
//                this.compose('buttons_submitButton', ButtonView, {model: new Model({label: 'Submit'})});
//            }
//
//            var $requiredFields = $('.section.selected').find(':input.required');
//            var $hiddenFields = $('.section').find(':input').not(':visible');
//            $requiredFields.attr('required', true);
//            $requiredFields.attr('aria-required', true);
//
//            // Hidden fields are not required
//            $hiddenFields.removeAttr('required');
//            $hiddenFields.removeAttr('aria-required');
//        }
    },
  });

  return FormController;
});