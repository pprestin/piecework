angular.module('Form',
    [
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'blueimp.fileupload'
    ])
    .config(['$routeProvider', '$locationProvider', '$logProvider','$provide',
        function($routeProvider, $locationProvider, $logProvider, $provide) {
            //$logProvider.debugEnabled(true);

            var context = window.piecework.context;
            var root = context['static'];

            $routeProvider
                .when('/form.html', {controller: 'ListController', templateUrl: root + '/static/ng/views/form-list.html'})
                .when('/form/:processDefinitionKey', {controller: 'FormController', templateUrl: root + '/static/ng/views/form.html'})
                .when('/form/:processDefinitionKey/:requestId', {controller: 'FormController', templateUrl: root + '/static/ng/views/form.html'})
                .otherwise({redirectTo:'/form.html'});

            $locationProvider.html5Mode(true).hashPrefix('!');

            $provide.decorator('$sniffer', function($delegate) {
                $delegate.history = true;
                return $delegate;
            });
        }
    ])
    .controller('FormController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams', 'personService', 'dialogs',
        function($scope, $window, $location, $resource, $http, $routeParams, personService, dialogs) {

            $scope.context = window.piecework.context;
            //$window.piecework.context;

            $scope.fileUploadOptions = {
                autoUpload: true
            };
            $scope.isCollapsed = false;
            $scope.isEditingAttachments = false;
            $scope.isViewingAttachments = false;
            $scope.sending = false;
            $scope.addAttachment = function(form, attachment) {

            };
            $scope.deleteAttachment = function(attachment) {
                $http['delete'](attachment.link).then($scope.refreshAttachments);
            }
            $scope.editAttachments = function() {
                $scope.isEditingAttachments = !$scope.isEditingAttachments;
            };
            $scope.refreshAttachments = function() {
                var form = $scope.form;
                $http.get(form.attachment).then(function(response) {
                    $scope.attachments = response.data.list;
                    $scope.form.attachmentCount = response.data.total;
                });
            };
            $scope.refreshForm = function(form) {
                $scope.form = form;

                var data = $scope.form.data;
                var validation = $scope.form.validation;
                var readonly = $scope.form.container.readonly;

                var rootContainer = form.container;
                var fields = new Array();
                if (rootContainer.children != null && rootContainer.children.length > 1 && rootContainer.activeChildIndex != -1) {
                    $scope.form.steps = rootContainer.children;
                    $scope.form.layout = 'wizard';
                    $scope.form.activeStep = rootContainer.activeChildIndex;
                    angular.forEach(rootContainer.children, function(child) {
                        if (child.readonly) {
                            angular.forEach(child.fields, function(field) {
                                field.readonly = true;
                            });
                        }
                        fields.push.apply(fields, child.fields);
                    });
                } else {
                    fields = $scope.form.container.fields;
                    $scope.form.layout = 'normal';
                }

                angular.forEach(fields, function(field) {
                    var values = data[field.name];
                    if (values != null && values.length == 1)
                        field.value = values[0];
                    if (typeof(validation) !== 'undefined' && validation[field.name] != null)
                        field.messages = validation[field.name];
                    if (readonly)
                        field.readonly = readonly;
                });

                if (form.task != null) {
                    if (form.task.active) {
                        form.state = 'assigned';
                    } else if (form.task.taskStatus == 'Suspended') {
                        form.state = 'suspended';
                    } else if (form.task.taskStatus == 'Cancelled') {
                        form.state = 'cancelled';
                    } else {
                        form.state = 'completed';
                    }
                }

                $scope.dialogs = dialogs;
                $scope.getPeople = personService.getPeople;

                $scope.showPeople = function() {

                };

                if (form != null && form.attachment != null)
                    $scope.refreshAttachments(form);
            };
            $scope.$on('fileuploaddone', function(event, data) {
                $scope.refreshAttachments();
            });
            $scope.$on('fileuploadfail', function(event, data) {
                var message = angular.fromJson(data.jqXHR.responseText);

                notificationService.notify($scope, message.messageDetail);
            });
            $scope.$on('fileuploadstart', function() {
                $scope.sending = true;
            });
            $scope.$on('fileuploadstop', function() {
                $scope.sending = false;
            });

            var resourcePath = './form/:processDefinitionKey';
            if ($routeParams.requestId != null)
                resourcePath += '/:requestId';
            var Form = $resource(resourcePath, {processDefinitionKey:'@processDefinitionKey',requestId:'@requestId'});
            $scope.criteria = {
                processDefinitionKey:$routeParams.processDefinitionKey,
                requestId: $routeParams.requestId
            };

            $scope.$on('event:refresh', function(event, message) {
                $scope.refreshing = true;
                Form.get($scope.criteria, $scope.refreshForm);
            });

            $scope.model = $window.piecework.model;
            if (typeof($scope.model) !== 'undefined' && typeof($scope.model.total) === 'undefined') {
                $scope.refreshForm($scope.model);
                delete $scope['model'];
            } else {
                Form.get($scope.criteria, $scope.refreshForm);
            }
        }
    ])
    .controller('ListController', ['$scope', '$window', '$resource', '$http', '$routeParams','$modal', 'personService', 'taskService', 'dialogs',
        function($scope, $window, $resource, $http, $routeParams, $modal, personService, taskService, dialogs) {
            $scope.context = $window.piecework.context;
            //$window.piecework.context;
            $scope.processSearchResults = function(results) {
                $scope.definitions = results.definitions;
                $scope.forms = results.list;
                $scope.user = results.user;
                $scope.processDefinitionDescription = new Object();
                angular.forEach(results.definitions, function(definition) {
                    $scope.processDefinitionDescription[definition.task.processDefinitionKey] = definition.task.processDefinitionLabel;
                });
                $scope.processDefinitionDescription[''] = 'Any process';
                $scope.searching = false;
            };
            $scope.criteria = new Object();
            $scope.criteria.processDefinitionKey = '';
            $scope.criteria.processStatus = 'open';
            $scope.criteria.taskStatus = 'all';
            var SearchResults = $resource('./form', {processStatus:'@processStatus'});
            //var results = SearchResults.get($scope.criteria, $scope.processSearchResults);

            $scope.processStatusDescription = {
                'open': 'Active',
                'complete': 'Completed',
                'cancelled': 'Cancelled',
                'suspended': 'Suspended',
                'all': 'Any status'
            };
            $scope.taskStatusDescription = {
                'Open': 'Open tasks',
                'Complete': 'Completed tasks',
                'Cancelled': 'Cancelled tasks',
                'Rejected': 'Rejected tasks',
                'Suspended': 'Suspended tasks',
                'all': 'All tasks'
            };

            $scope.selectedFormMap = new Object();
            $scope.getFormsSelected = function(taskStatuses) {
                var formIds = Object.keys($scope.selectedFormMap);
                var selectedForms = new Array();
                var acceptableTaskStatuses = new Object();
                angular.forEach(taskStatuses, function(taskStatus) {
                    acceptableTaskStatuses[taskStatus] = true;
                });
                angular.forEach(formIds, function(formId) {
                    var form = $scope.selectedFormMap[formId];
                    if (typeof(form) !== 'undefined' && form != null && form.task != null) {
                        if (typeof(taskStatuses) === 'undefined' || taskStatuses == null ||
                            acceptableTaskStatuses[form.task.taskStatus] != null)
                            selectedForms.push(form);
                    }
                });
                return selectedForms;
            };

            $scope.isSingleFormSelected = function(taskStatuses) {
                if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                    var selectedForms = $scope.getFormsSelected(taskStatuses);
                    return selectedForms.length === 1;
                }

                return Object.keys($scope.selectedFormMap).length === 1;
            };

            $scope.isFormSelected = function(taskStatuses) {
                if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                    var selectedForms = $scope.getFormsSelected(taskStatuses);
                    return selectedForms.length !== 0;
                }

                return Object.keys($scope.selectedFormMap).length !== 0;
            };

            $scope.$on('event:refresh', function(event, message) {
                $scope.searching = true;
                $scope.selectedFormMap = new Object();
                SearchResults.get($scope.criteria, $scope.processSearchResults);
            });

            $scope.dialogs = dialogs;

            $scope.refreshSearch = function() {
                $scope.$broadcast('event:refresh', 'criteria');
            };

            $scope.selectForm = function(form) {
                if ($scope.selectedFormMap[form.formInstanceId] == null)
                    $scope.selectedFormMap[form.formInstanceId] = form;
                else
                    delete $scope.selectedFormMap[form.formInstanceId];
            };

            $scope.model = $window.piecework.model;
            //if (typeof($window.piecework) !== 'undefined' && typeof($window.piecework.model) !== 'undefined' && typeof($window.piecework.model.total) !== 'undefined') {
            if (typeof($scope.model) !== 'undefined' && typeof($window.piecework.model.total) !== 'undefined') {
                $scope.processSearchResults($scope.model);
                delete $scope['model'];
            } else {
                SearchResults.get($scope.criteria, $scope.processSearchResults);
            }
        }
    ])
    .factory('controllerService', ['instanceService', 'notificationService', 'personService', 'taskService',
        function(instanceService, notificationService, personService, taskService) {
            return {
                'ActivationModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.selectedForms = selectedForms;
                    $scope.ok = function (reason) {
                        var checkActivationStatuses = function(scope) {
                            var selectedForms = scope.selectedForms;
                            var operationStatus = 'ok';
                            angular.forEach(selectedForms, function(form) {
                                if (typeof(form._activationStatus) === 'undefined')
                                    operationStatus = 'incomplete';
                                else if (form._activationStatus !== 'ok')
                                    operationStatus = 'error';
                            });

                            if (operationStatus == 'ok') {
                                $modalInstance.close(selectedForms);
                                $rootScope.$broadcast('event:refresh', 'activation');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkActivationStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be reactivated';
                            var title = data.messageDetail;
                            notificationService.notify($scope, message, title);
                            checkActivationStatuses(scope);
                        };

                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            instanceService.activate($scope, form, reason, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'AssignmentModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.getPeople = personService.getPeople;
                    $scope.selectedForms = selectedForms;
                    $scope.ok = function (assignee) {
                        var checkAssignmentStatuses = function(scope) {
                            var selectedForms = scope.selectedForms;
                            var operationStatus = 'ok';
                            angular.forEach(selectedForms, function(form) {
                                if (typeof(form._assignmentStatus) === 'undefined')
                                    operationStatus = 'incomplete';
                                else if (form._assignmentStatus !== 'ok')
                                    operationStatus = 'error';
                            });

                            if (operationStatus == 'ok') {
                                $modalInstance.close(assignee, selectedForms);
                                $rootScope.$broadcast('event:refresh', 'assignment');
                                scope.assigning = false;
                            }
                        };

                        var success = function(scope, data, status, headers, config, form, assignee) {
                            form._assignmentStatus = 'ok';
                            checkAssignmentStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form, assignee) {
                            form._assignmentStatus = 'error';
                            var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be assigned to <em>' + displayName + '</em>';
                            var title = data.messageDetail;
                            notificationService.notify($scope, message, title);
                            checkAssignmentStatuses(scope);
                        };

                        $scope.assigning = true;
                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            taskService.assignTask($scope, form, assignee, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'CancellationModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.selectedForms = selectedForms;
                    $scope.ok = function (reason) {
                        var checkStatuses = function(scope) {
                            var selectedForms = scope.selectedForms;
                            var operationStatus = 'ok';
                            angular.forEach(selectedForms, function(form) {
                                if (typeof(form._activationStatus) === 'undefined')
                                    operationStatus = 'incomplete';
                                else if (form._activationStatus !== 'ok')
                                    operationStatus = 'error';
                            });

                            if (operationStatus == 'ok') {
                                $modalInstance.close(selectedForms);
                                $rootScope.$broadcast('event:refresh', 'cancellation');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be cancelled/deleted because it is in an inconsistent state';
                            var title = "Unable to cancel";
                            notificationService.notify($scope, message, title);
                            checkStatuses(scope);
                        };

                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            instanceService.cancel($scope, form, reason, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'CommentModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.selectedForms = selectedForms;
                    $scope.ok = function (comment) {
                        var checkStatuses = function(scope) {
                            var selectedForms = scope.selectedForms;
                            var operationStatus = 'ok';
                            angular.forEach(selectedForms, function(form) {
                                if (typeof(form._activationStatus) === 'undefined')
                                    operationStatus = 'incomplete';
                                else if (form._activationStatus !== 'ok')
                                    operationStatus = 'error';
                            });

                            if (operationStatus == 'ok') {
                                $modalInstance.close(selectedForms);
                                $rootScope.$broadcast('event:refresh', 'comment');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot add comment because it is in an inconsistent state';
                            var title = "Unable to add comment";
                            notificationService.notify($scope, message, title);
                            checkStatuses(scope);
                        };

                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            var data = new FormData();
                            data.append("comment", comment);
                            instanceService.attach($scope, form, data, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'HistoryModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.selectedForms = selectedForms;
                    $scope.loading = true;
                    notificationService.clear($scope);
                    var selectedForms = $scope.selectedForms;
                    angular.forEach(selectedForms, function(form) {
                        instanceService.getHistory(form, function(response) {
                            $scope.history = response.data;
                            $scope.loading = false;
                        });
                    });

                    $scope.datediff = function(a, b) {
                        if (a == null || b == null)
                            return '';
                        var first = moment(a);
                        var second = moment(b);
                        var days = first.diff(second, 'days');
                        if (days > 1)
                            return days + ' days';
                        var hours = first.diff(second, 'hours');
                        if (hours > 1)
                            return hours + ' hours';
                        var minutes = first.diff(second, 'minutes');
                        if (minutes > 1)
                            return minutes + ' minutes';
                        var seconds = first.diff(second, 'seconds');
                        if (seconds > 1)
                            return seconds + ' seconds';
                        return '';
                    };
                    $scope.ok = function () {

                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'SuspensionModalController': ['$rootScope', '$scope', '$modalInstance', function ($rootScope, $scope, $modalInstance, selectedForms) {
                    $scope.selectedForms = selectedForms;
                    $scope.ok = function (reason) {
                        var checkStatuses = function(scope) {
                            var selectedForms = scope.selectedForms;
                            var operationStatus = 'ok';
                            angular.forEach(selectedForms, function(form) {
                                if (typeof(form._activationStatus) === 'undefined')
                                    operationStatus = 'incomplete';
                                else if (form._activationStatus !== 'ok')
                                    operationStatus = 'error';
                            });

                            if (operationStatus == 'ok') {
                                $modalInstance.close(selectedForms);
                                $rootScope.$broadcast('event:refresh', 'suspension');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be suspended';
                            var title = data.messageDetail;
                            notificationService.notify($scope, message, title);
                            checkStatuses(scope);
                        };

                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            instanceService.suspend($scope, form, reason, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }]
            }
        }
    ])
    .factory('dialogs', ['$modal','controllerService','notificationService', 'personService','taskService',
        function($modal, controllerService, notificationService, personService, taskService) {
            var context = window.piecework.context;
            var root = context['static'];
            return {
                openActivateModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/activate-modal-dialog.html',
                        controller: controllerService.ActivationModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openAssignModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/assign-modal-dialog.html',
                        controller: controllerService.AssignmentModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function (assignee) {}, function () {});
                },
                openCancelModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/cancel-modal-dialog.html',
                        controller: controllerService.CancellationModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openCommentModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/comment-modal-dialog.html',
                        controller: controllerService.CommentModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openHistoryModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/history-modal-dialog.html',
                        controller: controllerService.HistoryModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openSuspendModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: root + '/static/ng/views/suspend-modal-dialog.html',
                        controller: controllerService.SuspensionModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in',
                    });
                    modalInstance.result.then(function () {}, function () {});
                }
            };
        }
    ])
    .factory('instanceService', ['$http',
        function($http) {
            return {
                activate: function($scope, form, reason, success, failure) {
                    var url = form.activation + ".json";
                    var data = '{ "reason": "' + reason + '"}';
                    $http.post(url, data)
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, reason);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, reason);
                        });
                },
                attach: function($scope, form, formData, success, failure) {
                    var url = form.attachment + ".json";
                    $http.post(url, formData, {
                            headers: {'Content-Type': 'multipart/form-data'},
                            transformRequest: angular.identity
                        })
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, formData);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, formData);
                        });
                },
                cancel: function($scope, form, reason, success, failure) {
                    var url = form.cancellation + ".json";
                    var data = '{ "reason": "' + reason + '"}';
                    $http.post(url, data)
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, reason);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, reason);
                        });
                },
                getHistory: function(form, callback) {
                    var url = form.history + '.json';
                    return $http.get(url).then(function(response) {
                        callback(response);
                    });
                },
                suspend: function($scope, form, reason, success, failure) {
                    var url = form.suspension + ".json";
                    var data = '{ "reason": "' + reason + '"}';
                    $http.post(url, data)
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, reason);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, reason);
                        });
                }
            };
        }
    ])
    .factory('notificationService', ['$http',
        function($http) {
            return {
                clear: function($scope) {
                    delete $scope['notifications'];
                },
                notify: function($scope, message, title) {
                    // Don't bother to do anything unless $scope and message are defined
                    if (typeof($scope) !== 'undefined' && typeof(message) !== 'undefined') {
                        // Ensure that our notifications array exists in this scope
                        if (typeof($scope.notifications) === 'undefined')
                            $scope.notifications = new Array();
                        // Create the notification as an object
                        var notification = new Object();
                        notification.message = message;
                        if (typeof(title) !== 'undefined')
                            notification.title = title;
                        // Add this notification to the array
                        $scope.notifications.push(notification);
                    }
                }
            }
        }
    ])
    .factory('personService', ['$http',
        function($http) {
            return {
                getPeople: function(displayNameLike) {
                    var url = './person.json?displayNameLike=' + displayNameLike;

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
                }
            };
        }
    ])
    .factory('taskService', ['$http', 'notificationService',
        function($http, notificationService) {
            return {
                assignTask: function($scope, form, assignee, success, failure) {
                    if (form.task != null) {
                        if (typeof(form.task.taskStatus) !== 'undefined' && form.task.taskStatus == 'Suspended')
                            notificationService.notify($scope, 'Cannot assign a suspended task');

                        var url = form.assignment + ".json";
                        var data = '{ "assignee": "' + assignee + '"}';
                        $http.post(url, data)
                            .success(function(data, status, headers, config) {
                                success($scope, data, status, headers, config, form, assignee);
                            })
                            .error(function(data, status, headers, config) {
                                failure($scope, data, status, headers, config, form, assignee);
                            });
                    }
                }
            }
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