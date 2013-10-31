angular.module('Form',
    [
        'ngResource',
        'ngSanitize',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal'
    ])
    .config(['$routeProvider', '$locationProvider',
        function($routeProvider, $locationProvider) {
            $routeProvider
                .when('/form.html', {controller: 'ListController', templateUrl:'/piecework/static/ng/views/form-list.html'})
                .when('/form/:processDefinitionKey', {controller: 'FormController', templateUrl:'/piecework/static/ng/views/form.html'})
                .when('/secure/form/:processDefinitionKey/:requestId', {controller: 'FormController', templateUrl:'/piecework/static/ng/views/form.html'})
                .otherwise({redirectTo:'/form.html'});

            $locationProvider.html5Mode(true);
        }
    ])
    .controller('FormController', ['$scope', '$resource', '$http', '$routeParams','limitToFilter',
        function($scope, $resource, $http, $routeParams, limitToFilter) {
            var resourcePath = '/form/:processDefinitionKey';
            if ($routeParams.requestId != null)
                resourcePath += '/:requestId';
            var Form = $resource(resourcePath, {processDefinitionKey:'@processDefinitionKey',requestId:'@requestId'});
            var form = Form.get({processDefinitionKey:$routeParams.processDefinitionKey,requestId:$routeParams.requestId}, function(form) {
                $scope.form = form;

                var fields = $scope.form.container.fields;
                var data = $scope.form.data;
                var validation = $scope.form.validation;
                var readonly = $scope.form.container.readonly;

                angular.forEach(fields, function(field) {
                    var values = data[field.name];
                    if (values != null && values.length == 1)
                        field.value = values[0];
                    if (typeof(validation) !== 'undefined' && validation[field.name] != null)
                        field.messages = validation[field.name];
                    field.readonly = readonly;
                });

                if (form.task.active) {
                    form.state = 'assigned';
                } else if (form.endTime == null) {
                    form.state = 'suspended';
                } else {
                    form.state = 'completed';
                }

                var root = form.root;
                // strip off 'form' path param from root url
                var indexOf = root.indexOf('/form');
                if (indexOf != -1)
                    root = root.substring(0, indexOf)

                $scope.getPeople = function(displayNameLike) {
                    var url = root + '/person.json?displayNameLike=' + displayNameLike;

                    return $http.get(url).then(function(response) {
                        var people = new Array();
                        if (response != null && response.data != null && response.data.list != null) {
                            angular.forEach(response.data.list, function(item) {
                                var person = {
                                    displayName: item.displayName,
                                    userId: item.userId,
                                    toString: function() {
                                        return this.displayName;
                                    }
                                };
                                people.push(person);
                            });
                        }
                        return people;
                    });

                };

                $scope.showPeople = function() {

                };

            });


        }

    ])
    .controller('ListController', ['$scope', '$resource', '$http', '$routeParams','limitToFilter',
        function($scope, $resource, $http, $routeParams, limitToFilter) {
            var SearchResults = $resource('form', {});
            var results = SearchResults.get({}, function(results) {
                $scope.definitions = results.definitions;
                $scope.forms = results.list;
            });

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