angular.module('wf.directives',
    [])
    .directive('wfAttachments', ['attachmentService',
        function(attachmentService) {
            return {
                restrict: 'AE',
                scope: {
                    attachments : '=',
                    form : '=',
                    state : '='
                },
                templateUrl: 'attachments.html',
                link: function (scope, element) {
                    scope.deleteAttachment = function(attachment) {
                        attachmentService.deleteAttachment(scope.form, attachment);
                    };
                    scope.editAttachments = function() {
                        scope.state.isEditingAttachments = !scope.state.isEditingAttachments;
                    };
                }
            }
        }
    ])
    .directive('wfContainer', [
        function() {
            return {
                restrict: 'AE',
                scope: {
                    form : '=',
                    container : '=',
                    state : '='
                },
                templateUrl: 'container.html',
                //transclude: true,
                link: function (scope, element) {

                }
            }
        }
    ])
    .directive('wfField', [
        function() {
            return {
                require: '^ngModel',
                restrict: 'AE',
                scope: {
                    field : '='
                },
                templateUrl: 'field.html',
                transclude: true,
                link: function (scope, element) {
                    scope.onFieldChange = function(field) {
                        field.cssClass = null;
                        field.messages = null;
                    };
                    scope.range = function(min, max) {
                        var input = [];
                        for (var i=min; i<=max; i++) input.push(i);
                        return input;
                    };
                }
            }
        }
    ])
    .directive('wfFieldset', [
        function() {
            return {
                restrict: 'AE',
                scope: {
                    form : '=',
                    container : '='
                },
                templateUrl: 'fieldset.html',
                transclude: true,
                link: function (scope, element) {
                    scope.evaluateVisibilityConstraint = function(field, constraint) {
                        var f = scope.form.fieldMap[constraint.name];
                        if (typeof(f) === 'undefined')
                            return true;

                        var re = new RegExp(constraint.value);
                        var result = re.test(f.value);

                        if (typeof(constraint.and) !== 'undefined' && constraint.and != null) {
                            return result && scope.evaluateVisibilityConstraints(field, constraint.and, false);
                        }
                        if (typeof(constraint.or) !== 'undefined' && constraint.or != null) {
                            return result || scope.evaluateVisibilityConstraints(field, constraint.or, true);
                        }

                        if (!result)
                            field.required = false;

                        return result;
                    };
                    scope.evaluateVisibilityConstraints = function(field, constraints, acceptAny) {
                        var r = null;
                        angular.forEach(constraints, function(constraint) {
                            var b = scope.evaluateVisibilityConstraint(field, constraint);
                            if (r == null)
                                r = b;
                            else {
                                if (acceptAny)
                                    r = r || b;
                                else
                                    r = r && b;
                            }
                        });

                        return r;
                    };
                    scope.isVisible = function(field) {
                         if (field.constraints != undefined && field.constraints.length > 0) {
                              for (var i=0;i<field.constraints.length;i++) {
                                  var constraint = field.constraints[i];

                                  if (typeof(constraint) !== 'undefined') {
                                      if (constraint.type != null) {
                                          if (constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
                                              return scope.evaluateVisibilityConstraint(field, constraint)
                                          } else if (constraint.type == 'IS_ONLY_REQUIRED_WHEN') {

                                          }

                                      }
                                  }
                              }
                         }

                         return true;
                    };
                }
            }
        }
    ])
    .directive('wfMultipage', ['wizardService',
         function(wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     form : '=',
                     state : '='
                 },
                 templateUrl: 'multipage.html',
                 link: function (scope, element) {
                    scope.wizard = wizardService;
                 }
             }
         }
    ])
    .directive('wfMultistep', ['wizardService',
         function(wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     form : '=',
                     state : '='
                 },
                 templateUrl: 'multistep.html',
                 link: function (scope, element) {
                    scope.wizard = wizardService;
                 }
             }
         }
    ])
    .directive('wfNotifications', ['wizardService',
         function(wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     notifications : '='
                 },
                 templateUrl: 'notifications.html',
                 link: function (scope, element) {

                 }
             }
         }
    ])
    .directive('wfReview', ['wizardService',
         function(wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     form : '=',
                     state : '='
                 },
                 templateUrl: 'review.html',
                 link: function (scope, element) {
                    scope.wizard = wizardService;
                 }
             }
         }
    ])
    .directive('wfStatus', ['wizardService',
         function(wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     form : '='
                 },
                 templateUrl: 'status.html',
                 link: function (scope, element) {

                 }
             }
         }
    ])
    .directive('wfStep', ['wizardService',
        function(wizardService) {
            return {
                restrict: 'AE',
                scope: {
                    form : '=',
                    step : '=',
                    active: '=',
                    current: '='
                },
                templateUrl: 'step.html',
                //transclude: true,
                link: function (scope, element) {
                    scope.wizard = wizardService;
                }
            }
        }
    ])
    .directive('wfToolbar', ['wizardService',
        function(wizardService) {
            return {
                restrict: 'AE',
                scope: {
                    form : '=',
                    container : '='
                },
                templateUrl: 'toolbar.html',
                link: function (scope, element) {
                    scope.wizard = wizardService;
                }
            }
        }
    ]);
angular.module('wf.services',
    [])
    .factory('attachmentService', ['$http',
        function($http) {
            return {
                deleteAttachment : function(form, attachment) {
                    var attachmentService = this;
                    $http['delete'](attachment.link).then(function() { attachmentService.refreshAttachments(form); });
                },
                refreshAttachments : function(form) {
                    $http.get(form.attachment).then(function(response) {
                        form.attachments = response.data.list;
                        form.attachmentCount = response.data.total;
                    });
                }
            };
        }
    ])
    .factory('controllerService', ['instanceService', 'notificationService', 'personService', 'taskService',
        function(instanceService, notificationService, personService, taskService) {
            return {
                'ActivationModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                'AssignmentModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                            var userId = typeof(assignee.userId) === 'undefined' ? assignee : assignee.userId;
                            taskService.assignTask($scope, form, userId, success, failure);
                        });
                    };

                    $scope.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }],
                'CancellationModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                'CommentModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot add comment. ' + data.messageDetail;
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
                'HistoryModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                'SuspensionModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
    .factory('wizardService', ['$http',
        function($http) {
            return {
                changeStep : function(form, ordinal) {
                    form.activeStepOrdinal = ordinal;
                },
                clickButton : function(form, container, button) {
                    if (button.action == 'next') {
                        this.nextStep(form, container);
                    } else if (button.action == 'previous') {
                        this.previousStep();
                    }
                },
                getActiveStep : function(form) {
                    var activeStep = null;
                    angular.forEach(form.steps, function(step) {
                        if (form.activeStepOrdinal == step.ordinal)
                            activeStep = step;
                    });
                    return activeStep;
                },
                isActiveStep : function(form, step) {
                    if (!step.isStep && step.leaf)
                        step = step.parent;
                    if (form.container.reviewChildIndex > -1 && form.activeStepOrdinal == form.container.reviewChildIndex) {
                        return step.ordinal < form.activeStepOrdinal;
                    }
                    return this.isCurrentStep(form, step);
                },
                isAvailableStep : function(form, step) {
                    //return step.ordinal <= form.container.activeChildIndex;
                    return true;
                },
                isCurrentStep : function(form, step) {
                    var active = form.activeStepOrdinal;
                    var ordinal = step.ordinal;
                    var isCurrent = form.activeStepOrdinal == step.ordinal;
                    return isCurrent;
                },
                isReviewStep : function(form, step) {

                },
                nextStep : function(form, step) {
                    if (typeof(step) === 'undefined') {
                        step = this.getActiveStep(form);
                    }
                    var success = function(data, status, headers, config) {
                        if (form.steps.length > form.activeStepOrdinal) {
                            form.activeStepOrdinal += 1;
                            if (form.activeStepOrdinal > form.maxStep)
                                form.maxStep = form.activeStepOrdinal;
                        }
                    };
                    this.validateStep(step, form, success);
                },
                previousStep : function(form) {
                    if (form.activeStepOrdinal > 1) {
                        form.activeStepOrdinal -= 1;
                    }
                },
                validateStep : function(step, form, success) {
                    var data = new FormData();

                    $('.generated').remove();
                    $('.control-group').removeClass('error');
                    $('.control-group').removeClass('warning');

                    $('.step:visible').find(':input').each(function(index, element) {
                        var name = element.name;
                        if (name == undefined || name == null || name == '')
                            return;

                        if (element.files !== undefined && element.files != null) {
                            $.each(element.files, function(fileIndex, file) {
                                if (file != null)
                                    data.append(name, file);
                            });
                        } else {
                            var $element = $(element);
                            var value = $(element).val();

                            if (($element.is(':radio') || $element.is(':checkbox'))) {
                                if ($element.is(":checked")) {
                                    if (value != undefined)
                                        data.append(name, value);
                                }
                            } else {
                                data.append(name, value);
                            }
                        }
                    });
                    var validationId = step.leaf ? step.parent.containerId : step.containerId;
                    var url = form.action + '/' + step.containerId;
                    $http({
                             method: 'POST',
                             url: url,
                             data: {},
                             transformRequest: function() { return data; },
                             headers: {'Content-Type': 'multipart/form-data'}
                         })
                        .success(success)
                        .error(function(data, status, headers, config) {
                            if (data.items != null) {
                                var map = {};
                                for (var i=0;i<data.items.length;i++) {
                                    map[data.items[i].propertyName] = data.items[i];
                                }
                                var steps = [step];
                                if (!step.leaf) {
                                    steps = step.children;
                                }
                                var isFirst = true;
                                for (var n=0;n<steps.length;n++) {
                                    var s = steps[n];
                                    for (var i=0;i<s.fields.length;i++) {

                                        var field = s.fields[i];
                                        var item = map[field.name];
                                        if (typeof(item) !== 'undefined') {
                                            if (isFirst) {
                                                form.activeStepOrdinal = step.ordinal;
                                                isFirst = false;
                                            }
                                            field.messages = new Array();
                                            field.cssClass = "has-error";
                                            field.messages.push({ text: item.message });
                                        }  else {
                                            field.cssClass = null;
                                            field.messages = null;
                                        }
                                    }
                                }
                            }
                        });
                }

            }
        }
    ]);

angular.module('Form',
    [
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'blueimp.fileupload',
        'wf.directives',
        'wf.services'
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
                .when('/form/:processDefinitionKey/:state/:requestId', {controller: 'FormController', templateUrl: root + '/static/ng/views/form.html'})
                .otherwise({redirectTo:'/form.html'});

            $locationProvider.html5Mode(true).hashPrefix('!');

            $provide.decorator('$sniffer', ['$delegate', function($delegate) {
                $delegate.history = true;
                return $delegate;
            }]);
        }
    ])
    .controller('FormController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams', 'attachmentService', 'personService', 'taskService', 'wizardService', 'dialogs',
        function($scope, $window, $location, $resource, $http, $routeParams, attachmentService, personService, taskService, wizardService, dialogs) {
            console.log('started', 'Form controller started');
            $scope.context = window.piecework.context;
            $scope.assignTo = function(userId) {
                var success = function(scope, data, status, headers, config, form, assignee) {
                    scope.$broadcast('event:refresh', 'assignment');
                };

                var failure = function(scope, data, status, headers, config, form, assignee) {
                    form._assignmentStatus = 'error';
                    var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                    var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be assigned to <em>' + displayName + '</em>';
                    var title = data.messageDetail;
                    notificationService.notify($scope, message, title);
                };
                taskService.assignTask($scope, $scope.form, userId, success, failure);
            };

            $scope.fileUploadOptions = {
                autoUpload: true
            };
            $scope.state = new Object();
            $scope.state.isCollapsed = false;
            $scope.state.isEditingAttachments = false;
            $scope.state.isViewingAttachments = false;
            $scope.state.sending = false;
            $scope.markLeaves = function(container) {
                if (container.children != null && container.children.length > 1) {
                    angular.forEach(container.children, function(child) {
                        $scope.markLeaves(child);
                        child.parent = container;
                    });
                } else {
                    container.leaf = true;
                }
            };

            $scope.addFields = function(fields, form, container, isRoot) {
                var previousChild = null;
                if (container.children == null)
                    return;
                angular.forEach(container.children, function(child) {
                    if (isRoot)
                        child.isStep = true;
                    child.previous = previousChild;
                    if (previousChild != null)
                        previousChild.next = child;

                    if (form.layout == 'multipage' && child.readonly) {
                        angular.forEach(child.fields, function(field) {
                            field.editable = false;
                        });
                    }
                    angular.forEach(child.fields, function(field) {
                        field.parent = child;
                    });

                    fields.push.apply(fields, child.fields);
                    previousChild = child;
                    $scope.addFields(fields, form, child, false);
                });
            };
            $scope.handleField = function(form, data, validation, field, readonly) {
                var values = data[field.name];
                if (values != null && values.length > 0) {
                    if (values.length == 1) {
                        var value = values[0];
                        if (field.type == 'person') {
                            field.value = {
                                  displayName: value.displayName,
                                  userId: value.userId,
                                  toString: function() {
                                      return this.displayName;
                                  }
                              };
                        } else {
                            field.value = values[0];
                        }
                    }
                }
                field.values = new Array(field.maxInputs);
                if (values != null) {
                    for (var i=0;i<values.length;i++) {
                        field.values[i] = values[i];
                    }
                }

                if (typeof(validation) !== 'undefined' && validation[field.name] != null) {
                    field.messages = validation[field.name];
                    field.cssClass = "has-error";
                    form.activeStepOrdinal = field.parent.ordinal;
                    field.parent.breadcrumbCssClass = "invalid";
                } if (readonly)
                    field.editable = false;
            };
            $scope.refreshForm = function(form) {
                $scope.form = form;
                $scope.attachments = form.attachments;

                var data = $scope.form.data;
                var validation = $scope.form.validation;
                var readonly = $scope.form.container.readonly;

                var rootContainer = form.container;
                var fields = new Array();
                $scope.markLeaves(rootContainer);
                if (rootContainer.children != null && rootContainer.children.length > 1 && rootContainer.activeChildIndex != -1) {
                    $scope.form.steps = rootContainer.children;
                    $scope.form.activeStepOrdinal = rootContainer.activeChildIndex;
                    $scope.addFields(fields, form, rootContainer, true);
                } else {
                    fields = $scope.form.container.fields;
                    $scope.form.layout = 'normal';
                }
                form.fieldMap = new Object();
                angular.forEach(fields, function(field) {
                    form.fieldMap[field.name] = field;
                    $scope.handleField(form, data, validation, field, readonly);
                });

                if (form.task != null) {
                    if (form.task.active) {
                        form.state = 'assigned';
                    } else if (form.task.taskStatus == 'Suspended') {
                        form.state = 'suspended';
                    } else if (form.task.taskStatus == 'Cancelled') {
                        form.state = 'cancelled';
                    } else if (form.task.taskStatus == 'Complete' && (form.task.assignee == null || form.task.assignee.userId != $scope.context.user.userId)) {
                        form.state = 'completed';
                    }
                }

                $scope.dialogs = dialogs;
                $scope.getPeople = personService.getPeople;

                $scope.showPeople = function() {

                };

                // FIXME: Reduce cost by customizing to only use on fields that have a mask
                window.setTimeout(function() {
                    $(':input').inputmask();
                }, 500);

            };
            $scope.viewAttachments = function() {
                if (!$scope.state.isViewingAttachments)
                    attachmentService.refreshAttachments($scope.form);
                $scope.state.isViewingAttachments = !$scope.state.isViewingAttachments;
            };
            $scope.$on('keyup:37', function(onEvent, keypressEvent) {
                wizardService.previousStep($scope.form);
                $scope.$digest();
            });
            $scope.$on('keyup:39', function(onEvent, keypressEvent) {
                wizardService.nextStep($scope.form);
            });
            $scope.$on('fileuploaddone', function(event, data) {
                attachmentService.refreshAttachments($scope.form);
            });
            $scope.$on('fileuploadfail', function(event, data) {
                var message = angular.fromJson(data.jqXHR.responseText);

                notificationService.notify($scope, message.messageDetail);
            });
            $scope.$on('fileuploadstart', function() {
                $scope.state.sending = true;
            });
            $scope.$on('fileuploadstop', function() {
                $scope.state.sending = false;
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
                delete $window.piecework['model'];
            } else {
                Form.get($scope.criteria, $scope.refreshForm);
            }
        }
    ])
    .controller('ListController', ['$scope', '$window', '$resource', '$http', '$routeParams','$modal', 'personService', 'taskService', 'dialogs',
        function($scope, $window, $resource, $http, $routeParams, $modal, personService, taskService, dialogs) {
            $scope.context = $window.piecework.context;
            $scope.processSearchResults = function(results) {
                $scope.definitions = results.definitions;
                $scope.forms = results.list;
                $scope.user = results.user;
                $scope.processDefinitionDescription = new Object();
                angular.forEach(results.definitions, function(definition) {
                    $scope.processDefinitionDescription[definition.task.processDefinitionKey] = definition.task.processDefinitionLabel;
                });
                if (results.definitions != null && results.definitions.length == 1)
                    $scope.criteria.processDefinitionKey = results.definitions[0].task.processDefinitionKey;
                $scope.processDefinitionDescription[''] = 'Any process';
                $scope.searching = false;
            };
            $scope.criteria = new Object();
            $scope.criteria.processDefinitionKey = '';
            $scope.criteria.processStatus = 'open';
            $scope.criteria.taskStatus = 'all';
            var SearchResults = $resource('./form', {processStatus:'@processStatus'});
            //var results = SearchResults.get($scope.criteria, $scope.processSearchResults);

            $scope.exportCsv = function(selectedForms) {
                $window.location.href = "/workflow/ui/instance.csv?processDefinitionKey=" + $scope.criteria.processDefinitionKey;
            };

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

            $scope.isSingleProcessSelected = function() {
                return $scope.criteria.processDefinitionKey != null && $scope.criteria.processDefinitionKey != '';
            };

            $scope.isSingleProcessSelectable = function() {
                return typeof($scope.definitions) !== 'undefined' && $scope.definitions.length == 1;
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
            if (typeof($scope.model) !== 'undefined' && typeof($window.piecework.model.total) !== 'undefined') {
                $scope.processSearchResults($scope.model);
                delete $window.piecework['model'];
            } else {
                SearchResults.get($scope.criteria, $scope.processSearchResults);
            }
        }
    ])
    .directive('keypressEvents', ['$document', '$rootScope',
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