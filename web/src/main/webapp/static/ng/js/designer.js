'use strict';
var utils = {
    openDeleteModal: function(entityToDelete, $scope, $modal, callback) {
        var modalInstance = $modal.open({
            backdrop: true,
            templateUrl: '../static/ng/views/delete-modal-dialog.html',
            controller: function ($scope, $modalInstance) {

                $scope.entityToDelete = entityToDelete;

                $scope.ok = function () {
                    $modalInstance.close($scope.entityToDelete);
                };

                $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };
            },
            resolve: {
                entityToDelete: function () {
                    return $scope.entityToDelete;
                }
            },
            windowClass: 'in',
        });

        modalInstance.result.then(function (entityToDelete) {
            callback(entityToDelete);
        }, function () {

        });
    }

};


angular.module('ProcessDesigner', ['ngResource','ngSanitize','ui.bootstrap','ui.bootstrap.alert','ui.bootstrap.modal'])
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
    .controller('DeploymentListController', ['$scope','$resource','$routeParams','$modal',
        function($scope, $resource, $routeParams, $modal) {
            $scope.alerts = [];

            $scope.addAlert = function(alert) {
                $scope.alerts.push(alert);
            };

            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };

            var Deployment = $resource('process/:processDefinitionKey/deployment', {processDefinitionKey:'@processDefinitionKey'});
            Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey}, function(data) {
                $scope.deployments = data.list;
            });
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});

            $scope.copyDeployment = function(processDefinitionKey, deploymentId) {
                var DeploymentClone = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
                DeploymentClone.save({processDefinitionKey:processDefinitionKey, deploymentId:deploymentId}, function(data) {
                    $scope.deployments.push(data);
                });
            }

            $scope.confirmDeleteDeployment = function(processDefinitionKey, deploymentId, deploymentVersion, deploymentLabel) {
                var deploymentToDelete = {
                    title:'Are you sure you want to delete this deployment?',
                    text: '<strong class="text-danger">' + deploymentLabel + ' (#' + deploymentVersion + ') will be deleted.</strong><p/><br/>Deleting a deployment will prevent you from editing it in the future, but it will not disable or cancel and existing workflows that were started during the period when that deployment was active.',
                    processDefinitionKey: processDefinitionKey,
                    deploymentId: deploymentId,
                    deploymentVersion: deploymentVersion
                };
                var deleteDeployment = function(deploymentToDelete) {
                    if (deploymentToDelete == null)
                        return;

                    var DeploymentItem = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
                    DeploymentItem.remove(deploymentToDelete, function(data) {
                        Deployment.get({processDefinitionKey:deploymentToDelete.processDefinitionKey}, function(data) {
                            $scope.deployments = data.list;
                        });
                    });
                }
                utils.openDeleteModal(deploymentToDelete, $scope, $modal, deleteDeployment);
            }
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
     .controller('InteractionListController', ['$scope','$resource','$routeParams','$location','$anchorScroll','$modal',
        function($scope, $resource, $routeParams, $location, $anchorScroll, $modal) {
            $scope.onGetDeployment = function(deployment) {
              var interactions = deployment.interactions;
              angular.forEach(interactions, function(interaction) {
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
                });
                var interaction = interactions != null && interactions.length > 0 ? interactions[0] : null;
                if (interaction != null)
                    $scope.onSelectInteraction(interaction, interactions);

                $scope.deployment = deployment;
            }

            var Deployment = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});

            $scope.confirmDeleteInteraction = function(processDefinitionKey, deploymentId, interactionId) {
                var interactionToDelete = {
                    title:'Are you sure you want to delete this interaction?',
                    text: 'Deleting an interaction will all delete all screens and groupings that belong to that interaction',
                    processDefinitionKey: processDefinitionKey,
                    deploymentId: deploymentId,
                    interactionId: interactionId,
                };
                var deleteInteraction = function(interactionToDelete) {
                    var Interaction = $resource('process/:processDefinitionKey/deployment/:deploymentId/interaction/:interactionId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId',interactionId:'@interactionId'})
                    Interaction.remove({processDefinitionKey:interactionToDelete.processDefinitionKey, deploymentId:interactionToDelete.deploymentId, interactionId:interactionToDelete.interactionId}, function() {
                        Deployment.get({processDefinitionKey:interactionToDelete.processDefinitionKey, deploymentId:interactionToDelete.deploymentId}, $scope.onGetDeployment);
                    });
                }
                utils.openDeleteModal(interactionToDelete, $scope, $modal, deleteInteraction);
            }

            $scope.edit = function() {
                $scope.editing = true;
                $scope.cssClass = 'editing';
            }

            $scope.onFieldCancel = function(field) {
                field.cssClass = 'viewing';
            }

            $scope.onFieldFocus = function(field) {
                field.cssClass = 'editing';
            }

            $scope.onFieldKeyUp = function(field, $event) {
                switch ($event.keyCode) {
                case 13:
                    $scope.onFieldFocus();
                    break;
                }
            }

            $scope.onSelectGrouping = function(selected, groupings) {
                angular.forEach(groupings, function(grouping) {
                    grouping.cssClass = 'inactive';
                });
                selected.cssClass='active';
            }

            $scope.onSelectScreen = function(screen, screens) {
                var groupings = screen.groupings;

                angular.forEach(screens, function(selected, action) {
                    selected.cssClass = 'inactive';
                });
                screen.cssClass='active';

                if (groupings != null && groupings.length > 0) {
                    var grouping = groupings[0];
                    $scope.onSelectGrouping(grouping, groupings);
                }
            }

            $scope.onSelectInteraction = function(interaction, interactions) {
                angular.forEach(interactions, function(selected, action) {
                    selected.cssClass = 'inactive';
                });
                interaction.cssClass= 'active';
                var screens = interaction.screens;
                var screen = screens != null ? screens['CREATE'] : null;
                if (screen != null)
                    $scope.onSelectScreen(screen, screens);
            }

            $scope.scrollTo = function(id) {
                $anchorScroll();
            }

            $scope.view = function() {
                $scope.editing = false;
                $scope.cssClass = '';
            }

            Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey, deploymentId:$routeParams.deploymentId}, $scope.onGetDeployment);

            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            $scope.process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey});
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
            .when('/interaction/:processDefinitionKey/:deploymentId', {controller: 'InteractionListController', templateUrl:'../static/ng/views/interaction-list.html'})
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