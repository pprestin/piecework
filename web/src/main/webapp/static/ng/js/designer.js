'use strict';

angular.module('ProcessDesigner', ['ngResource','ngSanitize','ui.bootstrap'])
    .controller('DeploymentDetailController', ['$scope','$resource','$routeParams',
        function($scope, $resource, $routeParams) {
            var Deployment = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
            Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey, deploymentId:$routeParams.deploymentId}, function(data) {
                /*for (var i=0;i<data.sections.length;i++) {
                    if (data.sections[i].title == null)
                        data.sections[i].title = 'Untitled';
                }*/
                $scope.deployment = data;
            });
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});

            $scope.updateDeployment = function() {
                $scope.deployment.$save();
            };


        }
    ])
    .controller('DeploymentListController', ['$scope','$resource','$routeParams',
        function($scope, $resource, $routeParams) {
            var Deployment = $resource('process/:processDefinitionKey/deployment', {processDefinitionKey:'@processDefinitionKey'});
            Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey}, function(data) {
                $scope.deployments = data.list;
            });
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});
        }
    ])
    .controller('InteractionDetailController', ['$scope','$resource','$routeParams','$location','$anchorScroll',
         function($scope, $resource, $routeParams, $location, $anchorScroll) {
            var Deployment = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
            $scope.deployment = Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey, deploymentId:$routeParams.deploymentId});

            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});

            var Interaction = $resource('process/:processDefinitionKey/deployment/:deploymentId/interaction/:interactionId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId',interactionId:'@interactionId'})
            Interaction.get({processDefinitionKey:$routeParams.processDefinitionKey, deploymentId:$routeParams.deploymentId, interactionId:$routeParams.interactionId}, function(interaction) {

                if (interaction.screens != null) {
                    var createScreen = interaction.screens['CREATE'];
                    if (createScreen != null)
                        createScreen.cssClass='active';

                    angular.forEach(interaction.screens, function(screen) {
                        var sections = screen.sections;
                        if (sections != null) {
                            var sectionMap = {};
                            for (var n=0;n<sections.length;n++) {
                                var section = sections[n];
                                sectionMap[section.sectionId] = section;
                            }
                            angular.forEach(screen.groupings, function(grouping, index) {
                                if (screen.cssClass == 'active' && index == 0)
                                    grouping.cssClass = 'active';
                                grouping.sections = [];
                                if (grouping.sectionIds != null) {
                                    for (var m=0;m<grouping.sectionIds.length;m++) {
                                        var sectionId = grouping.sectionIds[m];
                                        var section = sectionMap[sectionId];
                                        if (section != null) {
                                            grouping.sections.push(section);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }

                $scope.interaction = interaction;
            })

            $scope.onSelectGrouping = function(selected, groupings) {
                angular.forEach(groupings, function(grouping) {
                    grouping.cssClass = 'inactive';
                });
                selected.cssClass='active';
            }

            $scope.onSelectScreen = function(selected, screens) {
                angular.forEach(screens, function(screen, action) {
                    screen.cssClass = 'inactive';
                });
                selected.cssClass='active';
                if (selected.groupings != null && selected.groupings.length > 0) {
                    selected.groupings[0].cssClass = 'active';
                    for (var i=1;i<selected.groupings.length;i++) {
                        selected.groupings[i].cssClass = 'inactive';
                    }
                }
            }

            $scope.scrollTo = function(id) {
                $anchorScroll();
            }
         }
     ])
    .controller('ProcessListController', ['$scope','$resource',
        function($scope, $resource) {
            var Process = $resource('process');

            Process.get({}, function(data) {
                $scope.processes = data.list;
            });
        }
    ])
    .controller('ProcessDetailController', ['$scope','$resource','$routeParams',
        function($scope, $resource, $routeParams) {
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});
        }
    ])
    .controller('ProcessEditController', ['$scope','$resource','$routeParams',
        function($scope, $resource, $routeParams) {
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});
            $scope.updateProcess = function() {
                $scope.process.$save();
            };
        }
    ])
    .config(function($routeProvider) {
        $routeProvider
            .when('/', {controller: 'ProcessListController', templateUrl:'../static/ng/views/process-list.html'})
            .when('/process/:processDefinitionKey', {controller: 'ProcessEditController', templateUrl:'../static/ng/views/process-detail.html'})
            .when('/deployment/:processDefinitionKey', {controller: 'DeploymentListController', templateUrl:'../static/ng/views/deployment-list.html'})
            .when('/deployment/:processDefinitionKey/:deploymentId', {controller: 'DeploymentDetailController', templateUrl:'../static/ng/views/deployment-detail.html'})
            .when('/interaction/:processDefinitionKey/:deploymentId/:interactionId', {controller: 'InteractionDetailController', templateUrl:'../static/ng/views/interaction-detail.html'})

            .otherwise({redirectTo:'/'});
    })
    .filter('orderActionType', function() {
        return function(input, attribute) {
            if (!angular.isObject(input)) return input;

            var keys = ['CREATE','COMPLETE','REJECT'];
            var array = [];
            for(var i=0;i<keys.length;i++) {
                var action = keys[i];
                var screen = input[action];
                if (screen != null) {
                    screen.action = action;
                    array.push(screen);
                }
            }

            return array;
        }
    });