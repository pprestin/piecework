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
                    scope.isCheckboxChecked = function(field, option) {
                        var isChecked = false;
                        angular.forEach(field.values, function(value) {
                            if (option != null && option.value == value)
                                isChecked = true;
                        });

                        return isChecked;
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
    .directive('wfStatus', ['$rootScope', '$window', 'notificationService', 'taskService', 'wizardService',
         function($rootScope, $window, notificationService, taskService, wizardService) {
             return {
                 restrict: 'AE',
                 scope: {
                     form : '='
                 },
                 templateUrl: 'status.html',
                 link: function (scope, element) {
                    scope.claim = function() {
                        var success = function(scope, data, status, headers, config, form, assignee) {
                            $rootScope.$broadcast('event:refresh', 'assignment');
                        };

                        var failure = function(scope, data, status, headers, config, form, assignee) {
                            form._assignmentStatus = 'error';
                            var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                            var message = form.task.processInstanceLabel + ' cannot be assigned ';
                            var title = data.messageDetail;
                            notificationService.notify(scope, title, message);
                        };
                        var userId = $window.piecework.context.user.userId;
                        taskService.assignTask(scope, scope.form, userId, success, failure);
                    };
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