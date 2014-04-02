angular.module('wf',
    [
        'ngRoute',
        'ngResource',
        'ngSanitize',
        'blueimp.fileupload',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'wf.directives',
        'wf.services'
    ])
    .controller('SearchController', ['$filter', '$http', '$location', '$resource', '$sce', '$scope', '$window', 'localStorageService', 'fileUpload', 'wizardService',
        function($filter, $http, $location, $resource, $sce, scope, $window, localStorageService, fileUpload, formPageUri, formResourceUri, wizardService) {
            scope.application = {
                bucketList: [],
                criteria: {
                    keywords: [],
                    processDefinitionKey: '',
                    processStatus: 'open',
                    taskStatus: 'all'
                },
                currentUser: {},
                definitions: [],
                facets: [],
                facetMap: {},
                forms: {},
                paging: {},
                processDefinitionDescription: {
                    '': 'Any process'
                },
                processStatusDescription: {
                    'open': 'Active',
                    'complete': 'Completed',
                    'cancelled': 'Cancelled',
                    'suspended': 'Suspended',
                    'queued': 'Queued',
                    'all': 'Any status'
                },
                state: {
                    collapsed: false,
                    filtering: false,
                    organizing: true,
                    searching: false,
                    selectAll: false,
                    selectedFacets: [],
                    selectedForms: [],
                    selectedFormMap: {}
                },
                taskStatusDescription: {
                    'Open': 'Open tasks',
                    'Complete': 'Completed tasks',
                    'Cancelled': 'Cancelled tasks',
                    'Rejected': 'Rejected tasks',
                    'Suspended': 'Suspended tasks',
                    'all': 'All tasks'
                }
            };
            var criteria = localStorageService.get("criteria");
            if (criteria !== null) {
                console.log("Retrieving saved criteria");
                scope.application.criteria = criteria;
            }

            var facetValue = function(form, facet) {
                if (facet.type !== 'user' && form[facet.name] == null)
                    return null;

                if (facet.type == 'datetime')
                    return $filter('date')(form[facet.name], 'MMM d, y H:mm');
                else if (facet.type == 'date')
                    return $filter('date')(form[facet.name], 'MMM d, y');
                else if (facet.type == 'user')
                    return form[facet.name] != null && form[facet.name].displayName != null ? form[facet.name].displayName : 'Nobody';
                if (scope.application.criteria[facet.name] != null && scope.application.criteria[facet.name] != '')
                    scope.application.state.filtering = true;

                return form[facet.name];
            };
            var processMetadata = function(results) {
                scope.application.state.organizing = true;
                scope.application.facets = results.facets;
                scope.application.state.searching = false;
                scope.application.definitions = results.metadata;
                scope.application.currentUser = results.currentUser;
                scope.application.state.selectedFormMap = {};
                scope.application.state.selectedForms = [];
                angular.forEach(results.metadata, function(definition) {
                    scope.application.processDefinitionDescription[definition.processDefinitionKey] = definition.processDefinitionLabel;
                });
                scope.application.bucketList = results.bucketList;
                scope.application.criteria.pg = results.processGroup;

                var selectedFacetMap = {};
                var state = localStorageService.get('state');
                if (state != null)
                    scope.application.state.selectedFacets = state.selectedFacets;
                else {
                    selectedFacetMap['lastModifiedTime'] = true;
                    selectedFacetMap['taskLabel'] = true;
                }

                if (scope.application.state.selectedFacets != null) {
                    angular.forEach(scope.application.state.selectedFacets, function(selectedFacet) {
                        selectedFacetMap[selectedFacet.name] = true;
                    });
                }
                angular.forEach(scope.application.facets, function(facet) {
                    if (facet.required || selectedFacetMap[facet.name])
                        facet.selected = true;
                    scope.application.facetMap[facet.name] = facet;
                });
                scope.application.state.organizing = false;
            };
            var processData = function(results) {
//                var results = response.data;
                scope.application.state.selectedForms = [];
                scope.application.state.searching = false;
                scope.application.criteria.sortBy = results.sortBy;

                scope.application.paging.total = results.total;
                scope.application.paging.pageNumber = results.pageNumber + 1;
                scope.application.paging.pageSize = results.pageSize;

                scope.application.paging.required = (scope.application.paging.pageNumber > 1 || scope.application.paging.total > scope.application.paging.pageSize);

                scope.application.paging.pageNumbers = [];
                var numberOfPages = scope.application.paging.total / scope.application.paging.pageSize + 1;
                for (var i=1;i<=numberOfPages;i++) {
                    scope.application.paging.pageNumbers.push(i);
                }

                scope.application.state.selectAll = false;

                var criteria = scope.application.criteria;
                angular.forEach(criteria.sortBy, function(sortBy) {
                    var indexOf = sortBy.indexOf(':');
                    if (indexOf != -1) {
                        var name = sortBy.substring(0, indexOf);
                        var direction = sortBy.substring(indexOf+1);
                        var facet = scope.application.facetMap[name];
                        if (facet != null) {
                            facet.direction = direction;
                        }
                    }
                });

                var specialFields = ['activation', 'assignment', 'attachment', 'cancellation', 'history', 'restart', 'suspension', 'bucketUrl'];
                scope.application.forms = [];
                angular.forEach(results.data, function(form) {
                    var displayedForm = {'formInstanceId': form.formInstanceId, 'link' : form.link };
                    angular.forEach(specialFields, function(field) {
                        displayedForm[field] = form[field];
                    });
                    angular.forEach(scope.application.facets, function(facet) {
                        var key = facet.name;
                        var value = facetValue(form, facet);
                        displayedForm[key] = value;
                    });
                    scope.application.forms.push(displayedForm);
                });
            };

            scope.application.paging.pageNumbers = [1,2,3,4,5];
            scope.application.paging.changePageSize = function(event) {
                scope.criteria.pageSize = scope.paging.pageSize;
                scope.application.search();
            };
            scope.application.paging.previousPage = function() {
                scope.criteria.pageNumber = scope.paging.pageNumber >= 2 ? scope.paging.pageNumber - 2 : 0;
                scope.application.search();
            };
            scope.application.paging.toPage = function(pageNumber) {
                scope.criteria.pageNumber = pageNumber - 1;
                scope.application.search();
            };
            scope.application.paging.nextPage = function() {
                scope.criteria.pageNumber = scope.paging.pageNumber;
                scope.application.search();
            };

//            scope.SearchResponse = $resource('./form', {processStatus:'@processStatus'});
            scope.application.clearFilters = function() {

            };
            scope.SearchResponse = $resource('./form', {processStatus:'@processStatus'});
            scope.application.search = function() {
                console.log("Searching...");
                var criteria = {};
                if (scope.application.criteria != null)
                    criteria = scope.application.criteria;
                if (criteria.keywords != null && typeof(criteria.keywords) == 'string')
                    criteria.keyword = criteria.keywords.split(' ');
                var facets = scope.application.facets;
                if (facets != null) {
                    angular.forEach(facets, function(facet) {
                        var isFilteringThisFacet = false;
                        if (facet.type == 'date' || facet.type == 'datetime') {
                            var afterName = facet.name + 'After';
                            var beforeName = facet.name + 'Before';
                            var afterCriterion = criteria[afterName];
                            var beforeCriterion = criteria[beforeName];
                            if (afterCriterion != null && afterCriterion != '')
                                isFilteringThisFacet = true;
                            else if (beforeCriterion != null && beforeCriterion != '')
                                isFilteringThisFacet = true;
                        } else {
                            var criterion = criteria[facet.name];
                            if (criterion != null && criterion != '')
                                isFilteringThisFacet = true;
                        }

                        if (isFilteringThisFacet) {
                            facet.selected = true;
                            scope.application.state.filtering = true;
                        }

                        // special handling for applicationStatusExplanation
                        // @see https://jira.cac.washington.edu/browse/EDMSIMPL-177
                        if (facet.name == 'applicationStatusExplanation') {
                            if (criteria != null && criteria.processStatus == 'suspended' ) {
                                facet.selected = true;  // show suspension reason for suspended process instances
                            } else {
                                facet.selected = false;  // hide suspension reason otherwise
                            }
                        }
                    });
                }

                scope.application.state.searching = true;
                localStorageService.set("criteria", criteria);
//                var url = './form?' + $.param(criteria);
//                $http.get(url).then(processData);
                scope.SearchResponse.get(criteria, processData);
            };
            scope.application.selectFacet = function(facet) {
                facet.selected = !facet.selected;
                scope.application.state.selectedFacets = [];
                angular.forEach(scope.application.facets, function(facet) {
                    if (facet.selected && facet.name != 'processInstanceLabel')
                        scope.application.state.selectedFacets.push(facet);
                });
                localStorageService.set('state', scope.application.state);
            };

            var model = $window.piecework.model;
            if (typeof(model) !== 'undefined' && typeof(model.total) !== 'undefined') {
                processMetadata(model);
                delete model['data'];
                delete $window['piecework']['model'];
                scope.application.search();
            } else {
                scope.application.search();
            }

            scope.$on("wfEvent:search", function() {
                scope.application.search();
            });
        }
    ])
    .directive('wfKeypressEvents', ['$document', '$rootScope',
        function($document, $rootScope) {
              return {
                  restrict: 'A',
                  link: function() {
                      $document.bind('keyup', function(e) {
                          $rootScope.$broadcast('keyup:' + e.which, e);
                      });
                  }
              };
        }
    ])
    .filter('typeaheadHighlight', function() {

          function escapeRegexp(queryToEscape) {
            return queryToEscape.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
          }

          return function(matchItem, query) {
              var displayName = matchItem.displayName;
              return query ? displayName.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : displayName;
          };

    });