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

angular
.module('ProcessDesigner', ['ngResource','ngSanitize','ui.bootstrap','ui.bootstrap.alert','ui.bootstrap.modal','ui.sortable','blueimp.fileupload'])
    .config([
        '$httpProvider', 'fileUploadProvider',
        function ($httpProvider, fileUploadProvider) {
            angular.extend(fileUploadProvider.defaults, {
                maxFileSize: 5000000,
                acceptFileTypes: /(\.|\/)(xml)$/i
            });
        }
    ])
    .controller('DeploymentDetailController', ['$scope','$resource','$routeParams',
        function($scope, $resource, $routeParams) {
            var Deployment = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
            var deployment = Deployment.get({processDefinitionKey:$routeParams.processDefinitionKey, deploymentId:$routeParams.deploymentId}, function(data) {
                $scope.deployment = data;
                $scope.deployment.processDefinitionKey = $routeParams.processDefinitionKey;
            });
            var Process = $resource('process/:processDefinitionKey', {processDefinitionKey:'@processDefinitionKey'});
            var process = Process.get({processDefinitionKey:$routeParams.processDefinitionKey}, function(data) {
                $scope.process = data;
            });

            $scope.$on('fileuploaddone', function(event, data) {
               $scope.deployment = data.result;
            });

            $scope.releaseDeployment = function(process, deployment) {
                var Release = $resource('process/:processDefinitionKey/release/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
                var release = new Release({processDefinitionKey:process.processDefinitionKey, deploymentId:deployment.deploymentId});
                release.$save();
            }

            $scope.updateDeployment = function() {
                deployment.$save({processDefinitionKey:$routeParams.processDefinitionKey});
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
                var DeploymentClone = $resource('process/:processDefinitionKey/deployment/:deploymentId/clone', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});
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
    .controller('ActivityDetailController', ['$scope','$resource','$routeParams','$location','$anchorScroll',
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
     .controller('ActivityListController', ['$scope','$resource','$routeParams','$location','$anchorScroll','$modal',
        function($scope, $resource, $routeParams, $location, $anchorScroll, $modal) {
            $scope.onGetDeployment = function(deployment) {
                var activityMap = deployment.activityMap;
                var startActivityKey = deployment.startActivityKey;
                deployment.activities = new Array();

                if (activityMap == null)
                    return;

                angular.forEach(deployment.flowElements, function(flowElement) {
                    if (flowElement.id == null)
                        return;

                    var activity = activityMap[flowElement.id];
                    var actionMap = activity.actionMap;

                    activity.activityKey = flowElement.id;
                    activity.flowElementLabel = flowElement.label;
                    if (startActivityKey == activity.activityKey)
                        $scope.onSelectActivity(activity);

                    activity.form = actionMap['CREATE'];
                    activity.accept = actionMap['COMPLETE'];
                    activity.reject = actionMap['REJECT'];

                    activity.containers = new Array();
                    if (activity.form && activity.form.container)
                        activity.containers.push(activity.form.container);
                    if (activity.accept && activity.accept.container)
                        activity.containers.push(activity.accept.container);
                    if (activity.reject && activity.reject.container)
                        activity.containers.push(activity.reject.container);

                    deployment.activities.push(activity);
                });

                $scope.deployment = deployment;
                if (deployment.editable) {
                    $scope.editing = true;
                    $scope.cssClass = 'editing';
                    $scope.sortableOptions = {
                        disabled: false
                    };
                }
            }

            var Deployment = $resource('process/:processDefinitionKey/deployment/:deploymentId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId'});

            $scope.addConstraint = function(field) {
                field.constraints.push({});
            }

            $scope.addActivity = function(deployment) {
                var activity = {};
                deployment.activity.push(activities);
                return interaction;
            }

            $scope.addOption = function(field) {
                field.options.push({});
            }

            $scope.addScreen = function(deployment) {
                var interaction = $scope.selectedInteraction;
                if (interaction == null)
                    interaction = $scope.addInteraction(deployment);
                if (interaction.screens == null)
                    interaction.screens = {};
                interaction.screens['CREATE'] = {};
            }

            $scope.confirmDeleteConstraint = function(processDefinitionKey, deploymentId, activityKey, fieldId, constraintId) {
                var constraintToDelete = {
                    title:'Are you sure you want to delete this constraint?',
                    text: 'Deleting a constraint will remove it from the field permanently',
                    processDefinitionKey: processDefinitionKey,
                    deploymentId: deploymentId,
                    interactionId: interactionId,
                    sectionId: sectionId,
                    fieldId: fieldId,
                    constraintId: constraintId
                };
                var deleteConstraint = function(constraintToDelete) {
                    var Field = $resource('process/:processDefinitionKey/deployment/:deploymentId/section/:sectionId/field/:fieldId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId',activityKey:'@activityKey',fieldId:'@fieldId',constraintId:'@constraintId'})
                    Field.remove(constraintToDelete, function() {
                        Deployment.get({processDefinitionKey:constraintToDelete.processDefinitionKey, deploymentId:constraintToDelete.deploymentId}, $scope.onGetDeployment);
                    });
                }
                utils.openDeleteModal(constraintToDelete, $scope, $modal, deleteConstraint);
            }

            $scope.confirmDeleteField = function(processDefinitionKey, deploymentId, activityKey, containerId, fieldId) {
                var fieldToDelete = {
                    title:'Are you sure you want to delete this field?',
                    text: 'Deleting a field will remove it from the container',
                    processDefinitionKey: processDefinitionKey,
                    deploymentId: deploymentId,
                    activityKey: activityKey,
                    containerId: containerId,
                    fieldId: fieldId
                };
                var deleteField = function(fieldToDelete) {
                    var Field = $resource('process/:processDefinitionKey/deployment/:deploymentId/section/:sectionId/field/:fieldId', {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId',interactionId:'@interactionId'})
                    Field.remove({processDefinitionKey:fieldToDelete.processDefinitionKey, deploymentId:fieldToDelete.deploymentId, sectionId:fieldToDelete.sectionId, fieldId:fieldToDelete.fieldId}, function() {
                        Deployment.get({processDefinitionKey:fieldToDelete.processDefinitionKey, deploymentId:fieldToDelete.deploymentId}, $scope.onGetDeployment);
                    });
                }
                utils.openDeleteModal(fieldToDelete, $scope, $modal, deleteField);
            }

            $scope.confirmDeleteSection = function(processDefinitionKey, deploymentId, interactionId, actionTypeId, groupingId, sectionId) {
                var sectionToDelete = {
                    title:'Are you sure you want to delete this section?',
                    text: 'Deleting a section will remove it from the interaction',
                    processDefinitionKey: processDefinitionKey,
                    deploymentId: deploymentId,
                    interactionId: interactionId,
                    actionTypeId: actionTypeId,
                    groupingId: groupingId,
                    sectionId: sectionId
                };
                var deleteSection = function(sectionToDelete) {
                    var Section = $resource('process/:processDefinitionKey/deployment/:deploymentId/interaction/:interactionId/screen/:actionTypeId/grouping/:groupingId/section/:sectionId',
                        {processDefinitionKey:'@processDefinitionKey',deploymentId:'@deploymentId',interactionId:'@interactionId',actionTypeId:'@actionTypeId',groupingId:'@groupingId',sectionId:'@sectionId'})
                    Section.remove({processDefinitionKey:sectionToDelete.processDefinitionKey, deploymentId:sectionToDelete.deploymentId, interactionId:sectionToDelete.interactionId, actionTypeId:sectionToDelete.actionTypeId, groupingId:sectionToDelete.groupingId,sectionId:sectionToDelete.sectionId}, function() {
                        Deployment.get({processDefinitionKey:sectionToDelete.processDefinitionKey, deploymentId:sectionToDelete.deploymentId}, $scope.onGetDeployment);
                    });
                }
                utils.openDeleteModal(sectionToDelete, $scope, $modal, deleteSection);
            }

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
                $scope.sortableOptions = {
                    disabled: false
                };
            }

            $scope.onFieldBlur = function(field) {

            }

            $scope.onFieldCancel = function(field) {
                field.cssClass = 'viewing';
            }

            $scope.onFieldEdit = function(field) {
                field.cssClass = 'editing';
            }

            $scope.onFieldFocus = function(field, fields) {
                if (field.cssClass == null || field.cssClass == 'viewing') {
                    angular.forEach(fields, function(current) {
                        current.cssClass = null;
                    })

                    field.cssClass = 'hasFocus';
                }
            }

            $scope.onFieldKeyUp = function(field, $event) {
                switch ($event.keyCode) {
                case 13:
                    $scope.onFieldEdit();
                    break;
                }
            }

            $scope.onSelectActivity = function(activity, activities) {
                angular.forEach(activities, function(selected) {
                    selected.cssClass = 'inactive';
                });
                activity.cssClass= 'active';
                angular.forEach(activity.containers, function(selected) {
                    selected.cssClass = 'inactive';
                });

                $scope.activity = activity;
                $scope.container = null;
            }

            $scope.onSelectContainer = function(container, containers) {
                if (container.cssClass == 'active') {
                    container.cssClass = null;
                    $scope.container = null;
                } else {
                    angular.forEach(containers, function(selected) {
                        selected.cssClass = 'inactive';
                    });
                    container.cssClass = 'active';
                    $scope.container = container;
                }
            }

            $scope.removeOption = function(option, field) {
                var index = field.options.indexOf(option);
                if (index > -1) {
                    field.options.splice(index, 1);
                }
            }

            $scope.saveActivity = function(process, deployment, activity) {
                var Activity = $resource('process/:processDefinitionKey/deployment/:deploymentId/activity/:activityKey',
                        {processDefinitionKey:'@processDefinitionKey', deploymentId:'@deploymentId', activityKey:'@activityKey'});
                var activityResource = new Activity({processDefinitionKey:process.processDefinitionKey, deploymentId:deployment.deploymentId, activityKey: activity.activityKey});
                activityResource.usageType = activity.usageType;
                activityResource['CREATE'] = activity.form;
                activityResource['ACCEPT'] = activity.accept;
                activityResource['REJECT'] = activity.reject;
                activityResource.$save();
            }

            $scope.scrollTo = function(id) {
                $anchorScroll();
            }

            $scope.view = function() {
                $scope.editing = false;
                $scope.cssClass = '';
                $scope.sortableOptions = {
                    disabled: true
                };
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
            .when('/activity/:processDefinitionKey/:deploymentId', {controller: 'ActivityListController', templateUrl:'../static/ng/views/activity-list.html'})
            .when('/activity/:processDefinitionKey/:deploymentId/:interactionId', {controller: 'ActivityDetailController', templateUrl:'../static/ng/views/activity-detail.html'})

            .otherwise({redirectTo:'/'});
    })
    .directive('container', function($compile){
      return{
        restrict: 'A',
        replace: true,
        link: function(scope, element, attributes){
          if (scope.container.children && angular.isArray(scope.container.children)) {
    				$compile('<ul><li ng-repeat="child in container.children" person="child"></li></ul>')(scope, function(cloned, scope){
                        element.append(cloned);
    				});
    			}
        },
        scope:{
          container:'='
        },
        template: '<li><span ng-click="$emit(\'clicked\', person)">{{person.name}}</span></li>'
      }
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