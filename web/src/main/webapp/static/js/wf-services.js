angular.module('wf.services',
    [])
    .factory('attachmentService', ['$http', '$rootScope', '$sce',
        function($http, $rootScope, $sce) {
            return {
                deleteAttachment : function(form, attachment) {
                    var attachmentService = this;
                    var url = attachment.link + '/removal';
                    $http.post($sce.trustAsResourceUrl(url), null, {
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                    })
                    .success(function() {
                        attachmentService.refreshAttachments(form);
                    });
                },
                refreshAttachments : function(form) {
                    $http.get($sce.trustAsResourceUrl(form.attachment)).then(function(response) {
                        form.attachments = response.data.list;
                        form.attachmentCount = response.data.total;
                        $rootScope.$broadcast('wfEvent:attachments', form.attachments);
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
                                $rootScope.$broadcast("wfEvent:search");
                                $rootScope.$broadcast('wfEvent:refresh', 'activation');
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
                                $rootScope.$broadcast("wfEvent:search");
                                $rootScope.$broadcast('wfEvent:refresh', 'assignment');
                                scope.assigning = false;
                            }
//                            else {
//                                var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
//                                var message = 'Cannot assign task(s) to ' + displayName;
//                                var title = data.messageDetail;
//                                notificationService.notify($scope, message, title);
//                            }
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
                                $rootScope.$broadcast("wfEvent:search");
                                $rootScope.$broadcast('wfEvent:refresh', 'cancellation');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be cancelled/deleted. ' + data.messageDetail;
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
               'ColumnsModalController': ['$rootScope', '$scope', '$modalInstance', 'facets', function ($rootScope, $scope, $modalInstance, facets) {
                    $scope.facets = facets;
                    $scope.selectFacet = function(facet) {
                        facet.selected = !facet.selected;
                        $scope.$root.$broadcast('wfEvent:facet-changed', facet);
                    };
                    $scope.ok = function (comment) {

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
                                $rootScope.$broadcast('wfEvent:refresh', 'comment');
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
                            instanceService.comment($scope, form, comment, success, failure);
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
                'RestartModalController': ['$rootScope', '$scope', '$modalInstance', 'selectedForms', function ($rootScope, $scope, $modalInstance, selectedForms) {
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
                                $rootScope.$broadcast("wfEvent:search");
                                $rootScope.$broadcast('wfEvent:refresh', 'activation');
                            }
                        };

                        var success = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'ok';
                            checkActivationStatuses(scope);
                        };

                        var failure = function(scope, data, status, headers, config, form) {
                            form._activationStatus = 'error';
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be restarted';
                            var title = data.messageDetail;
                            notificationService.notify($scope, message, title);
                            checkActivationStatuses(scope);
                        };

                        notificationService.clear($scope);
                        var selectedForms = $scope.selectedForms;
                        angular.forEach(selectedForms, function(form) {
                            instanceService.restart($scope, form, reason, success, failure);
                        });
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
                                $rootScope.$broadcast("wfEvent:search");
                                $rootScope.$broadcast('wfEvent:refresh', 'suspension');
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
            return {
                openActivateModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/activate-modal-dialog.html',
                        controller: controllerService.ActivationModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openAssignModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/assign-modal-dialog.html',
                        controller: controllerService.AssignmentModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function (assignee) {}, function () {});
                },
                openCancelModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/cancel-modal-dialog.html',
                        controller: controllerService.CancellationModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openColumnsModal: function(facets) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/columns-modal-dialog.html',
                        controller: controllerService.ColumnsModalController,
                        resolve: {
                            facets: function () {
                                return facets;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openCommentModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/comment-modal-dialog.html',
                        controller: controllerService.CommentModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openHistoryModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/history-modal-dialog.html',
                        controller: controllerService.HistoryModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openRestartModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/restart-modal-dialog.html',
                        controller: controllerService.RestartModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                openSuspendModal: function(selectedForms) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/suspend-modal-dialog.html',
                        controller: controllerService.SuspensionModalController,
                        resolve: {
                            selectedForms: function () {
                                return selectedForms;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                },
                alert: function(message) {
                    var modalInstance = $modal.open({
                        backdrop: true,
                        templateUrl: 'templates/alert-modal-dialog.html',
                        controller: ['$rootScope', '$scope', '$modalInstance', 'message', function ($rootScope, $scope, $modalInstance, message) {
                            $scope.message = message;
                            $scope.ok= function () {
                                $modalInstance.dismiss('ok');
                            };
                        }],
                        resolve: {
                            message: function () {
                                return message;
                            }
                        },
                        windowClass: 'in'
                    });
                    modalInstance.result.then(function () {}, function () {});
                }
            };
        }
    ])
    .factory('httpHelper', ['$http', '$sce',
        function($http, $sce) {
            return {
                send: function(url, $scope, form, reason, success, failure) {
                    if (typeof(reason) === 'undefined')
                        reason = '';
                    var data = { "reason": reason };
                    $http({
                        method: 'POST',
                        url: $sce.trustAsResourceUrl(url),
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        transformRequest: function(obj) {
                            var str = [];
                            for(var p in obj)
                                str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                            return str.join("&");
                        },
                        data: data
                    }).success(function(data, status, headers, config) {
                        success($scope, data, status, headers, config, form, reason);
                    })
                    .error(function(data, status, headers, config) {
                        failure($scope, data, status, headers, config, form, reason);
                    });
                }
            }
        }
    ])
    .factory('instanceService', ['$http', '$rootScope', '$sce', 'httpHelper',
        function($http, $rootScope, $sce, httpHelper) {
            return {
                activate: function($scope, form, reason, success, failure) {
                    var url = form.activation;
                    httpHelper.send(url, $scope, form, reason, success, failure);
                },
                attach: function($scope, form, formData, success, failure) {
                    var url = form.attachment + ".json";
                    $http.post($sce.trustAsResourceUrl(url), formData, {
                            headers: {'Content-Type': 'multipart/form-data'},
                            transformRequest: angular.identity
                        })
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, formData);
                            $rootScope.$broadcast('wfEvent:attachments', data.list);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, formData);
                        });
                },
                comment: function($scope, form, comment, success, failure) {
                    var formData = 'comment=' + encodeURIComponent(comment);
                    var url = form.attachment + ".json";
                    $http.post($sce.trustAsResourceUrl(url), formData, {
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                        })
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, formData);
                            $rootScope.$broadcast('wfEvent:attachments', data.list);
                        })
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, formData);
                        });
                },
                cancel: function($scope, form, reason, success, failure) {
                    var url = form.cancellation;
                    httpHelper.send(url, $scope, form, reason, success, failure);
                },
                getHistory: function(form, callback) {
                    var url = form.history + '.json';
                    return $http.get($sce.trustAsResourceUrl(url)).then(function(response) {
                        callback(response);
                    });
                },
                restart: function($scope, form, reason, success, failure) {
                    var url = form.restart;
                    httpHelper.send(url, $scope, form, reason, success, failure);
                },
                suspend: function($scope, form, reason, success, failure) {
                    var url = form.suspension;
                    httpHelper.send(url, $scope, form, reason, success, failure);
                },
                changeBucket: function($scope, form, bucket, success, failure) {
                    var url = form.bucketUrl;
                    $http.post($sce.trustAsResourceUrl(url), bucket, {
                            headers: {'Content-Type': 'text/plain'}
                        })  
                        .success(function(data, status, headers, config) {
                            success($scope, data, status, headers, config, form, bucket);
                        })  
                        .error(function(data, status, headers, config) {
                            failure($scope, data, status, headers, config, form, bucket);
                        }); 
                }
            };
        }
    ])
    .factory('notificationService', ['$http', '$rootScope',
        function($http, $rootScope) {
            return {
                clear: function($scope) {
                    delete $scope['notifications'];
                },
                notify: function($scope, message, title) {
                    // Don't bother to do anything unless message is defined
                    if (typeof(message) !== 'undefined') {

                        // Create the notification as an object
                        var notification = new Object();
                        notification.message = message;
                        if (typeof(title) !== 'undefined')
                            notification.title = title;

                        $rootScope.$broadcast("wfEvent:notification", notification);
                    }
                }
            }
        }
    ])
    .factory('personService', ['$http', '$rootScope', '$sce',
        function($http, $rootScope, $sce) {
            return {
                getPeople: function(displayNameLike) {
                    var rootUri = '.';
                    var formUri = $rootScope.form != null ? $rootScope.form.root : null;
                    if (formUri != null && formUri) {
                        var indexOf = formUri.indexOf('/form');
                        if (indexOf != -1) {
                            rootUri = formUri.substring(0, indexOf);
                        }
                    }
                    var url = rootUri + '/person.json?displayNameLike=' + displayNameLike;

                    return $http.get($sce.trustAsResourceUrl(url)).then(function(response) {
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
    .factory('taskService', ['$http', '$rootScope', '$sce', 'notificationService',
        function($http, $rootScope, $sce, notificationService) {
            return {
                assignTask: function($scope, form, assignee, success, failure) {
                    var taskStatus = form.task != null ? form.task.taskStatus : form.taskStatus;
                    if (taskStatus != null) {
                        if (typeof(taskStatus) !== 'undefined' && taskStatus == 'Suspended')
                            notificationService.notify($scope, 'Cannot assign a suspended task');

                        var url = form.assignment;
                        var data = { "assignee" : assignee };
                        $http({
                             method: 'POST',
                             url: $sce.trustAsResourceUrl(url),
                             headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                             transformRequest: function(obj) {
                                 var str = [];
                                 for(var p in obj)
                                     str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                                 return str.join("&");
                             },
                             data: data
                        }).success(function(data, status, headers, config) {
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
    .factory('wizardService', ['$http', '$rootScope', '$sce',
        function($http, $rootScope, $sce) {
            return {
                changeStep : function(form, ordinal) {
                    if (form.layout == 'multipage' && ordinal > form.container.activeChildIndex)
                        return;
                    if (typeof(ordinal) === 'undefined')
                        return;

                    form.activeStepOrdinal = ordinal;
                    $rootScope.$broadcast("wfEvent:step-changed", ordinal);
                },
                clickButton : function(form, container, button) {
                    if (button.action == 'next') {
                        this.nextStep(form, container);
                    } else if (button.action == 'previous') {
                        this.previousStep(form);
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
                    if (form.layout == 'multipage' && ordinal > form.container.activeChildIndex)
                        return false;
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
                             url: $sce.trustAsResourceUrl(url),
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
