(function(window, document, angular) {

    'use strict';

    var wfInputDirective = ['$browser', '$compile', '$sniffer', 'wfUtils',
        function($browser, $compile, $sniffer, wfUtils) {
            return {
                priority: -1,
                restrict: 'E',
                replace: true,
                require: '?ngValue',
                compile: function compile(tElement, tAttrs, transclude) {
                    var modelName = 'form.data[\'' + tAttrs.name + '\'][0]';
                    if (tAttrs.ngModel == null)
                        tElement.attr('ng-value', modelName);

                    return {
                        pre: function preLink(scope, iElement, iAttrs, controller) {

                        },
                        post: function postLink(scope, iElement, iAttrs, controller) {
                            wfUtils.attachForm(scope);
                            wfUtils.linkInputs(scope, iElement, iAttrs);
                            wfUtils.linkMask(scope, iElement, iAttrs);
                        }
                    }
                },
                scope: {
                    name: '@'
                }
        //        template: '<span><input data-ng-model="inputModel" name="{{name}}"></span>'
            };
        }
    ];

    angular.module('wf.directives',
        ['ui.bootstrap', 'ui.bootstrap.alert', 'ui.bootstrap.modal', 'wf.services', 'wf.templates', 'LocalStorageModule'])
        .controller('wfToolbarController', ['$scope', 'attachmentService', 'notificationService',
            function(scope, attachmentService, notificationService) {
                scope.fileUploadOptions = {
                    autoUpload: true,
                    dataType: 'json',
                    formData: {},
                    sequentialUploads: true,    // to avoid race conditions on workflow server with multiple-file upload
                    fileInput: $('#attachmentFile'),
                    xhrFields: {
                        withCredentials: true
                    }
                };

                scope.$on('fileuploaddone', function(event, data) {
                    attachmentService.refreshAttachments(scope.form);
                });
                scope.$on('fileuploadfail', function(event, data) {
                    var message = angular.fromJson(data.jqXHR.responseText);

                    notificationService.notify(scope.$root, message.messageDetail);
                });
//                scope.$on('fileuploadstart', function() {
//                    scope.state.sending = true;
//                });
//                scope.$on('fileuploadstop', function() {
//                    scope.state.sending = false;
//                });
                scope.toggleCollapse = function() {
                    scope.state.isCollapsed = !scope.state.isCollapsed;
                };
                scope.viewAttachments = function() {
                    scope.$root.$broadcast('wfEvent:view-attachments');
                };
            }
        ])
        .directive('input', wfInputDirective)
        .directive('select', wfInputDirective)
        .directive('textarea', wfInputDirective)
    //    .directive('wfActive', [
    //        function() {
    //            return {
    //                restrict: 'A',
    //                link: function (scope, element, attr) {
    //                    var step = attr.wfActive;
    //                    if (typeof(step) === 'undefined')
    //                        step = '-1';
    //
    //                    var indexOf = step.indexOf('+');
    //                    var upwards = false;
    //                    if (indexOf == (step.length-1)) {
    //                        step = step.substring(0, indexOf);
    //                        upwards = true;
    //                    }
    //
    //                    step = parseInt(step);
    //
    //                    scope.$root.$on('wfEvent:form-loaded', function(event, form) {
    //                        scope.form = form;
    //                        var isDisabled = false;
    //
    //                        // Check to see if the current user is the assigned user
    //                        if (typeof(form) !== 'undefined' && form.task != null) {
    //                            isDisabled = (form.currentUser == null || form.task.assignee == null || form.currentUser.userId !== form.task.assignee.userId)
    //                        }
    //
    //                        var ordinal = form.activeStepOrdinal;
    //
    //                        if (form.actionType == 'VIEW')
    //                            ordinal = -1;
    //
    //                        if (upwards) {
    //                            if (typeof(ordinal) !== 'undefined' && step < ordinal)
    //                                isDisabled = true;
    //                        } else if (step != ordinal) {
    //                            isDisabled = true;
    //                        }
    //
    //                        var $input = element.is(":input") ? element : element.find(":input");
    //                        scope.disabled = isDisabled;
    //                        if (isDisabled) {
    //                            $input.attr('disabled', 'disabled');
    //                            if ($input.attr('type') == 'radio') {
    //                                $input.attr('type', 'checkbox');
    //                                $input.addClass('wf-was-radio');
    //                            }
    //                        } else {
    //                            $input.removeAttr('disabled');
    //                            if ($input.hasClass('wf-was-radio')) {
    //                                $input.removeClass('wf-was-radio');
    //                                $input.attr('type', 'radio');
    //                            }
    //                        }
    //                    });
    //                }
    //            }
    //        }
    //    ])
        .directive('wfAlert', [
            function() {
                return {
                    restrict: 'A',
                    scope: {

                    },
                    link: function (scope, element, attr) {
                        var fieldName = attr.wfAlert;

                        if (fieldName == null || fieldName == '')
                            fieldName = element.closest('.form-group').find(':input').attr('name');

                        if (fieldName != null && fieldName != '') {
                            scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                                scope.form = form;
                                var validation = form.validation;
                                var subFieldName = null;
                                var indexOfPeriod = fieldName.indexOf('.');
                                if (indexOfPeriod != -1) {
                                    subFieldName = fieldName.substring(indexOfPeriod+1);
                                    fieldName = fieldName.substring(0, indexOfPeriod);
                                }
                                var messages = typeof(validation) !== 'undefined' ? validation[fieldName] : null;
                                if (messages != null) {
                                    var html = '';
                                    angular.forEach(messages, function(message) {
                                        if (message != null) {
                                            element.text(message.text);
                                            element.closest('.form-group').addClass('has-error');
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        ])
        .directive('wfAssignmentButton', ['dialogs', 'notificationService', 'taskService',
            function(dialogs, notificationService, taskService) {
                return {
                    restrict: 'AE',
                    link: function (scope, element) {
                        scope.dialogs = dialogs;
                        scope.assignmentFailure = function(scope, data, status, headers, config, form, assignee) {
                            form._assignmentStatus = 'error';
                            var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                            var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be assigned to <em>' + displayName + '</em>';
                            var title = data.messageDetail;
                            notificationService.notify(scope.$root, message, title);
                        };
                        scope.assignmentSuccess = function(scope, data, status, headers, config, form, assignee) {
                            scope.$root.$broadcast('wfEvent:refresh', 'assignment');
                        };
                        scope.assignTo = function(userId) {
//                            angular.forEach(scope.forms, function(form) {
                                taskService.assignTask(scope, scope.form, userId, scope.assignmentSuccess, scope.assignmentFailure);
//                            });
                        };
                        scope.openModal = function() {
                            dialogs.openAssignModal(scope.forms);
                        };
                    },
                    controller: ['$scope', function(scope) {
                        scope.$watch('form', function(modified, original) {
                            var form = modified;

                            if (form != null && form.task != null) {
                                scope.assigneeDisplayName = form.task.assignee != null ? form.task.assignee.displayName : null;
                                scope.showButton = form.task.active;
                                scope.showAssignment = form.task.active && form.history != null && form.task.assignee != null;
                            }
                        });
                        scope.showAssignment = false;
                        scope.showButton = false;
                    }],
                    scope: {
                        form: '='
                    },
                    template:
                        "<p data-ng-show=\"showAssignment\" class=\"navbar-text text-primary\">Assigned to {{assigneeDisplayName}}</p>\n" +
                        "<div data-ng-show=\"form.task.active\" class=\"btn-group\">\n" +
                        "   <button data-ng-click=\"openModal()\" class=\"btn btn-default navbar-btn\" id=\"assign-dialog-button\" data-target=\"#assign-dialog\" data-backdrop=\"false\" data-toggle=\"modal\" title=\"Assign task\" type=\"button\"><i class=\"fa fa-user\"></i></button>\n" +
                        "   <button type=\"button\" class=\"btn btn-default navbar-btn dropdown-toggle\" data-toggle=\"dropdown\">\n" +
                        "       <span class=\"caret\"></span>\n" +
                        "   </button>\n" +
                        "   <ul class=\"dropdown-menu\">\n" +
                        "       <li><a data-ng-click=\"assignTo('')\">Unassign</a></li>\n" +
                        "       <li data-ng-show=\"form.task.candidateAssignees\" role=\"presentation\" class=\"divider\"></li>\n" +
                        "       <li data-ng-repeat=\"candidateAssignee in form.task.candidateAssignees\"><a data-ng-click=\"assignTo(candidateAssignee.userId)\" class=\"candidate-assignee\" id=\"{{candidateAssignee.userId}}\">Assign to {{candidateAssignee.userId == context.user.userId ? 'me' : candidateAssignee.displayName}}</a></li>\n" +
                        "   </ul>\n" +
                        "</div>\n"
                }
            }
        ])
        .directive('wfAttachments', ['attachmentService', '$rootScope',
            function(attachmentService, $rootScope) {
                return {
                    restrict: 'AE',
                    scope: {
                        form: '='
                    },
                    templateUrl: 'templates/attachments.html',
                    link: function (scope, element) {
                        console.log("Initialized wfAttachments");
                        if (typeof(scope.state) == 'undefined') {
                            scope.state = {};
                            scope.state.isViewingAttachments = false;
                        }
                        if (typeof(scope.form) !== 'undefined') {
                            scope.state.attachments = scope.form.attachments;
                        }
                        scope.deleteAttachment = function(attachment) {
                            if (scope.state.isEditingAttachments) {
                                console.log('wfAttachment deleteAttachment clicked');
                                attachmentService.deleteAttachment(scope.form, attachment);
                            }
                        };
                        scope.editAttachments = function() {
                            console.log('wfAttachment editAttachments clicked');
                            scope.state.isEditingAttachments = !scope.state.isEditingAttachments;
                        };
                        scope.$on('wfEvent:attachments', function(event, attachments) {
                            console.log("wfAttachments caught an attachments event!");
                            scope.state.attachments = attachments;
                        });
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            console.log("wfAttachments attached form to its scope");
                            if (typeof(form) !== 'undefined') {
                                if (form.loadedBy == null)
                                    form.loadedBy = [];
                                form.loadedBy.push('wfAttachments');
                                scope.form = form;
                            }
                        });
                        scope.$root.$on('wfEvent:view-attachments', function() {
                            console.log("wfAttachments toggling visibility of attachments");
                            if (typeof(scope.form) === 'undefined')
                                scope.form = $rootScope.form;
                            if (typeof(scope.form) !== 'undefined' && !scope.state.isViewingAttachments)
                                attachmentService.refreshAttachments(scope.form);
                            scope.state.isViewingAttachments = !scope.state.isViewingAttachments;
                            scope.$root.$broadcast('wfEvent:toggle-attachments', scope.state.isViewingAttachments);
                        });
                    }
                }
            }
        ])
         .directive('wfBreadcrumbs', ['attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
            function(attachmentService, dialogs, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/breadcrumbs.html',
                    link: function (scope, element) {
                        scope.wizard = wizardService;
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            console.log("wfBreadcrumbs attached form to its scope");
                            if (typeof(form) !== 'undefined') {
                                if (form.loadedBy == null)
                                    form.loadedBy = [];
                                form.loadedBy.push('wfBreadcrumbs');
                                scope.form = form;
                            }
                        });
                    }
                }
            }
        ])
        .directive('wfButtonbar', ['wizardService',
            function(wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        form : '=',
                        container : '='
                    },
                    templateUrl: 'templates/buttonbar.html',
                    link: function (scope, element) {
                        scope.wizard = wizardService;
                    }
                }
            }
        ])
        .directive('wfContainer', [
            function() {
                return {
                    restrict: 'AE',
                    scope: {
                        container : '='
                    },
                    templateUrl: 'templates/container.html',
                    link: function (scope, element) {
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            console.log("wfContainer attached form to its scope");
                            if (typeof(form) !== 'undefined') {
                                if (form.loadedBy == null)
                                    form.loadedBy = [];
                                form.loadedBy.push('wfContainer');
                                scope.form = form;
                            }
                        });
                        if (typeof(scope.state) == 'undefined') {
                            scope.state = {};
                            scope.state.isViewingAttachments = false;
                        }
                        scope.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                            scope.state.isViewingAttachments = isViewingAttachments;
                        });
                    }
                }
            }
        ])
        .directive('wfDate', [
            function() {
                return {
                    restrict: 'AE',
                    scope: {
                        name : '@',
                        required : '@'
                    },
                    transclude: true,
                    controller: ['$scope', function(scope) {

                    }],
                    link: function (scope, element, attr) {

                        scope.onChange = function() {

                        };

                        scope.today = function() {
                            scope.dt = new Date();
                        };
    //                    scope.today();

                        scope['show-weeks'] = false;
                        scope.toggleWeeks = function () {
                            scope.showWeeks = ! scope.showWeeks;
                        };

                        scope.clear = function () {
                            scope.dt = null;
                        };

                        scope.toggleMin = function() {
                            scope.minDate = ( scope.minDate ) ? null : new Date();
                        };

                        scope.open = function($event) {
                            $event.preventDefault();
                            $event.stopPropagation();
                            scope.opened = true;
                        };

                        scope.dateOptions = {
                            'year-format': "'yy'",
                            'show-weeks': false,
                            'starting-day': 0
                        };

                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            console.log("wfDate attached form to its scope");
                            if (typeof(form) !== 'undefined') {
                                if (form.loadedBy == null)
                                    form.loadedBy = [];
                                form.loadedBy.push('wfDate');
                                scope.form = form;
                                if (scope.form.data[scope.name] != null)
                                    scope.dt = new Date(scope.form.data[scope.name]);
                            }
                        });
                    },
                    template:
                        '<div class="input-group wf-datepicker-group">' +
                        '   <input data-ng-required="{{required}}" datepicker-popup="MM/dd/yyyy" data-ng-model="dt" datepicker-options="dateOptions" size="10" type="text" class="form-control wf-datepicker"  is-open="opened" min="minDate" max="maxDate" close-text="Close" show-weeks="false"/>' +
                        '   <input data-ng-value="dt|date:\'yyyy-MM-ddTHH:mm:ssZ\'" name="{{name}}" type="hidden">' +
                        '   <span class="input-group-addon">' +
                        '       <i class="fa fa-calendar"></i> ' +
                        '   </span> ' +
                        '</div>'
                }
            }
        ])
        .directive('wfDateRange', ['$filter',
            function($filter) {
                return {
                    restrict: 'AE',
                    scope: {
                        criteria : '=',
                        name : '='
                    },
                    templateUrl: 'templates/daterange.html',
                    transclude: true,
                    link: function (scope, element) {
                        var afterName = scope.name + 'After';
                        var beforeName = scope.name + 'Before';

                        scope.after = scope.criteria[afterName];
                        scope.before = scope.criteria[beforeName];

                        scope.afterChange = function() {
                            if (scope.criteria[afterName] != scope.after) {
                                scope.before = null;
                                scope.criteria[beforeName] = null;
                                scope.beforeMinDate = scope.after;

                                if (scope.after != null)
                                    scope.criteria[afterName] = $filter('date')(scope.after, 'yyyy-MM-ddT00:00:00-0000');
                                else
                                    delete scope.criteria[afterName];

                                scope.$root.$broadcast('wfEvent:search', scope.criteria);
                            }
                        };

                        scope.beforeChange = function() {
                            if (scope.criteria[beforeName] != scope.before) {
                                if (scope.before != null)
                                    scope.criteria[beforeName] = $filter('date')(scope.before, 'yyyy-MM-ddT00:00:00-0000');
                                else
                                    delete scope.criteria[beforeName];

                                scope.$root.$broadcast('wfEvent:search', scope.criteria);
                            }
                        };

                        scope.today = function() {
                            scope.dt = new Date();
                        };
    //                    scope.today();

                        scope['show-weeks'] = false;
                        scope.toggleWeeks = function () {
                            scope.showWeeks = ! scope.showWeeks;
                        };

                        scope.clear = function () {
                            scope.dt = null;
                            scope.after = null;
                            scope.before = null;
                            scope.criteria[afterName] = null;
                            scope.criteria[beforeName] = null;
                        };

    //                    // Disable weekend selection
    //                    scope.disabled = function(date, mode) {
    //                        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    //                    };

                        scope.toggleMin = function() {
                            scope.minDate = ( scope.minDate ) ? null : new Date();
                        };
    //                    scope.toggleMin();

                        scope.afterOpen = function($event) {
                            $event.preventDefault();
                            $event.stopPropagation();
                            scope.afterOpened = true;
                        };

                        scope.beforeOpen = function($event) {
                            $event.preventDefault();
                            $event.stopPropagation();
                            scope.beforeOpened = true;
                        };

                        scope.dateOptions = {
                            'year-format': "'yy'",
                            'show-weeks': false,
                            'starting-day': 0
                        };

                        scope.$on('wfEvent:clear-filters', function() {
                            scope.after = null;
                            scope.before = null;
                            var didClear = false;

                            if (scope.criteria[beforeName] != null) {
                                delete scope.criteria[beforeName];
                                didClear = true;
                            }
                            if (scope.criteria[afterName] != null) {
                                delete scope.criteria[afterName];
                                didClear = true;
                            }

                            if (didClear)
                                scope.$root.$broadcast('wfEvent:search', scope.criteria);

                            scope.isFiltering = false;
                        });
                    }
                }
            }
        ])
        .directive('wfField', ['personService',
            function(personService) {
                return {
                    restrict: 'AE',
                    scope: {
                        field : '='
                    },
                    templateUrl: 'templates/field.html',
                    transclude: true,
                    link: function (scope, element) {
                        scope.getInlineUrl = function(value) {
                            var url = typeof(value.link) !== 'undefined' ? value.link : value;
                            url += '?inline=true';
                            return url;
                        };
                        scope.getPeople = personService.getPeople;
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
                    templateUrl: 'templates/fieldset.html',
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
        .directive('wfFile', ['$http', '$sce', '$window', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
            function($http, $sce, $window, attachmentService, dialogs, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        'name': '@',
                        'label': '@',
                        'image': '@',
                        'disabled': '@',
                        'required': '@',
                        'state': '='
                    },
                    controller: ['$scope', function(scope) {
                        scope.cannotCheckout = true;
                        scope.checkedOut = false;
                        scope.deleting = false;
                        scope.editing = false;
                        scope.files = [];
                    }],
                    link: function (scope, element, attr) {
                        scope.checkoutFile = function(file) {
                            var url = file.link + '/checkout';
                            $http.post($sce.trustAsResourceUrl(url), null, {
                                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                                transformRequest: angular.identity
                            })
                            .success(function(data, status, headers, config) {
                                scope.checkedOut = true;
                                $window.location.href = file.link;
                            });
                        };
                        scope.checkinFile = function(file) {
                            scope.checkedOut = false;
                        };
                        scope.deleteFile = function(file) {
                            var url = file.link + '/removal.json';
                            $http.post($sce.trustAsResourceUrl(url), null, {
                                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                                transformRequest: angular.identity
                            })
                            .success(function(data, status, headers, config) {
                                var files = [];
                                angular.forEach(scope.files, function(current) {
                                    if (current != null && current.name != file.name) {
                                        files.push(current);
                                    }
                                });
                                scope.files = files;
                                scope.deleting = false;
                                scope.fileToDelete = null;
                            })
                            .error(function(message, status, headers, config) {
                                scope.error = message.messageDetail;
                                scope.doNotDeleteFile();
                            });
                        };
                        scope.fileChange = function() {


                            console.log('New file');
                        };
                        scope.doNotDeleteFile = function() {
                            scope.deleting = false;
                            scope.fileToDelete = null;
                        };
                        scope.verifyDeleteFile = function(file) {
                            scope.deleting = true;
                            file.detailed = false;
                            scope.fileToDelete = file;
                        };
                        scope.edit = function() {
                            scope.editing = !scope.editing;
                        };
                        scope.showDetails = function(file) {
                            file.detailed = !file.detailed;
                        };
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            var data = form.data;
                            var values = typeof(data) !== 'undefined' ? data[scope.name] : null;
                            scope.files = [];
                            angular.forEach(values, function(file) {
                                file.detailed = false;
                                scope.files.push(file);
                            });
                        });
                        scope.$on('wfEvent:invalid', function(event, validation) {
                            if (validation != null && validation[scope.name] != null) {
                                scope.error = validation[scope.name];
                            }
                        });

                        scope.$on('fileuploadstart', function(event, data) {
                            scope.sending = true;
                        });
                        scope.$on('fileuploadstop', function(event, data) {
                            scope.sending = false;
                        });
                        scope.$watch('files', function(original, modified) {
                            console.log('Files changed');
                        });
                    },
                    template:
                        '       <ul class="list-group"> ' +
                        '           <li data-ng-show="error" class="list-group-item list-group-item-danger">' +
                        '               <button type=\"button\" class=\"close\" type=\"button\" data-ng-click=\"error = null\" aria-hidden=\"true\">&times;</button>' +
                        '               <div>{{error}}</div>' +
                        '           </li>' +
                        '           <li data-ng-hide="files" class="list-group-item"><span class="text-muted">No documents</span></li>' +
                        '           <li data-ng-show="deleting" class="list-group-item list-group-item-danger">' +
                        '               <div>Deleting {{fileToDelete.name}} will permanently remove it from the repository. Are you sure?</div>' +
                        '               <div class="btn-toolbar pull-right">' +
                        '                   <button data-ng-click="deleteFile(fileToDelete)" class="btn btn-danger btn-xs" type="button">Yes, delete it</button>' +
                        '                   <button data-ng-click="doNotDeleteFile()" class="btn btn-default btn-xs" type="button">Cancel</button>' +
                        '               </div>' +
                        '               <div class="clearfix"></div>' +
                        '           </li>' +
                        '           <li data-ng-repeat="file in files" class="list-group-item">' +
                        '               <div>' +
                        '                   <i data-ng-click="showDetails(file)" data-ng-class="file.detailed ? \'fa-angle-up\' : \'fa-angle-down\'" data-ng-hide="editing" class="fa pull-right" style="cursor:pointer;padding-top: 4px"></i>' +
                        '                   <div data-ng-if="image"><img data-wf-image="{{name}}" class="" data-ng-src="{{file.link}}" alt="" /><br /></div>' +
                        '                   <a data-ng-href="{{file.link}}" data-ng-class="checkedOut ? \'text-danger\' : \'\'"><i data-ng-hide="editing" class="fa fa-cloud-download"></i> {{file.name}}</a>&nbsp;&nbsp;&nbsp;' +
                        '                   <div data-ng-click="checkinFile(file)" data-ng-hide="editing || !checkedOut" class="btn btn-default btn-xs">' +
                        '                       <span class="fa-stack"><i class="fa fa-key fa-stack-1x"></i><i class="fa fa-ban fa-stack-2x text-danger"></i></span></div>' +
                        '                   <div data-ng-click="checkoutFile(file)" data-ng-hide="cannotCheckout || editing || checkedOut" class="btn btn-default btn-xs"><i class="fa fa-key"></i> Checkout</div>' +
                        '                   <i data-ng-click="deleteFile(file)" data-ng-show="editing" class=\"fa fa-times text-danger wf-delete-item pull-right\" title=\"Delete item\" style=\"font-size:14px;padding-top: 4px\"></i>' +
                        '               </div>' +
                        '               <div data-ng-show="file.detailed"><p/>' +
                        '                   <table class="table table-condensed">' +
                        '                       <thead><tr><th>Version</th><th>Upload date</th><th>Filer</th></tr></thead>' +
                        '                       <tbody>' +
                        '                           <tr data-ng-repeat="version in file.versions"><td><a data-ng-href="{{version.link}}">{{version.label}}</a></td><td>{{version.createDate|date:\'medium\'}}</td><td>{{version.createdByUser.displayName}}</td></tr>' +
                        '                       </tbody>' +
                        '                   </table>' +
                        '                   <button data-ng-click="verifyDeleteFile(file)" class="btn btn-default btn-xs" role="button" type="button"><i class="fa fa-ban"></i> Delete</button>' +
                        '                   <div class="clearfix"></div>' +
                        '               </div>' +
                        '           </li>' +
                        '           <li data-ng-hide="disabled" class="list-group-item">' +
                        '               <p data-ng-show="checkedOut" class="pull-left">One or more files are checked out</p>' +
                        '               <div class="pull-left">' +
    //                    '                   <span data-ng-click="edit()" data-ng-show="files" class="btn btn-default btn-xs" title="{{editing ? \'Done\' : \'Click to delete files\'}}">' +
    //                    '                       <i data-ng-class="editing ? \'fa-folder\' : \'fa-edit\'" class="fa"></i>' +
    //                    '                   </span>' +
                        '                   <span data-wf-alert="{{name}}" class="text-danger"></span>' +
                        '               </div>' +
                        '               <div class="btn-toolbar pull-right">' +
                        '                   <div data-ng-class="checkedOut ? \'btn-danger\' : \'\'" class="btn btn-default btn-xs fileinput-button" role="button" type="button">' +
                        '                       <i ng-class="state.sending ? \'fa-spinner fa-spin\' : \'fa-cloud-upload\'" class="fa"></i> Add' +
                        '                       <input type="file" name="{{name}}" multiple="multiple" data-ng-change="fileChange()" data-ng-disabled="disabled || form.currentUser.userId !== form.task.assignee.userId" data-ng-model="files">' +
                        '                   </div>' +
                        '               </div>' +
                        '               <div class="clearfix"></div>' +
                        '           </li>' +
                        '       </ul> '
                }
            }
        ])
        .directive('wfFileUpload', ['$sce', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
            function($sce, attachmentService, dialogs, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/fileupload.html',
                    link: function (scope, element) {
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                        scope.fileUploadOptions = {
                            sequentialUploads: true,    // to avoid race conditions on workflow server with multiple-file upload
                            autoUpload: true
                        };
                        scope.getAttachmentUrl = function() {
                            if (typeof(scope.form) === 'undefined' || scope.form == null) {
                                return '/';
                            }
                            return $sce.getTrustedResourceUrl(scope.form.attachment);
                        };
                        scope.$on('fileuploaddone', function(event, data) {
                            attachmentService.refreshAttachments(scope.form);
                        });
                        scope.$on('fileuploadfail', function(event, data) {
                            var message = angular.fromJson(data.jqXHR.responseText);

                            notificationService.notify(scope.$root, message.messageDetail);
                        });
//                        scope.$on('fileuploadstart', function() {
//                            scope.state.sending = true;
//                        });
//                        scope.$on('fileuploadstop', function() {
//                            scope.state.sending = false;
//                        });
                    }
                }
            }
        ])
        .directive('wfFormFallback', [
            function() {
                return {
                    restrict: 'AE',
                    link: function(scope, element, attr) {
                        element.hide();
                        scope.$on('wfEvent:fallback', function(event) {
                            element.show();
                        });
                    }
                }

            }
        ])
        .directive('wfFormLoading', [
            function() {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    link: function(scope, element, attr) {
                        scope.$on('wfEvent:start-loading', function(event) {
                            console.log("Loading...");
                            element.show();
                        });
                        scope.$on('wfEvent:stop-loading', function(event) {
                            console.log("Done loading");
                            element.hide();
                        });
                    }
                }

            }
        ])
    //    .directive('wfInputMask', [
    //         function() {
    //             return {
    //                 restrict: 'AE',
    //                 link: function(scope, element, attr) {
    //                     scope.hasDatePickerSupport = function() {
    //                         var elem = document.createElement('input');
    //                         elem.setAttribute('type','date');
    //                         elem.value = 'foo';
    //                         return (elem.type == 'date' && elem.value != 'foo');
    //                     }
    //
    //                     var type = element.attr('type');
    //                     var options = {};
    //                     if (typeof(attr.wfInputMask) !== 'undefined' && attr.wfInputMask != '')
    //                        options['mask'] = attr.wfInputMask;
    //                     else if (typeof(attr.wfMask) !== 'undefined')
    //                        options['mask'] = attr.wfMask;
    //                     if (typeof(attr.wfPlaceholder) !== 'undefined')
    //                        options['placeholder'] = attr.wfPlaceholder;
    //
    //                     options['showMaskOnHover'] = false;
    //
    //                     if (type == 'text')
    //                        element.inputmask(options);
    //                     else if ((type == 'date' || type == 'datetime' || type == 'datetime-local') && !scope.hasDatePickerSupport())
    //                        element.inputmask(options);
    //                 }
    //             }
    //
    //         }
    //     ])
        .directive('wfForm', ['$http', '$location', '$sce', '$window', 'formPageUri', 'formResourceUri', 'notificationService', 'wizardService',
            function($http, $location, $sce, $window, formPageUri, formResourceUri, notificationService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        form : '='
                    },
//                    compile: function compile(tElement, tAttrs, transclude) {
//                        var modelName = 'form.data[\'' + tAttrs.name + '\'][0]';
//                        if (tAttrs.ngModel == null)
//                            tElement.attr('ng-value', modelName);
//                        tElement.attr('ng-controller', 'FormController');
//                        return {
//                            pre: function preLink(scope, iElement, iAttrs, controller) {
//
//                            },
//                            post: function postLink(scope, element, attrs, controller) {
//                                scope.$root.$broadcast('wfEvent:start-loading');
//                                scope.initialize = function(form) {
//                                    element.show();
//                                    scope.$root.$broadcast('wfEvent:stop-loading');
//                                    element.attr("action", form.action);
//                                    element.attr("method", "POST");
//                                    element.attr("enctype", "multipart/form-data");
//
//                                    var created = form.task != null ? form.task.startTime : null;
//                                    element.attr('data-wf-task-started', created);
//                                };
//                                scope.$watch('form', function(oldValue, newValue) {
//                                    if (newValue != null || oldValue != null) {
//                                        var form = newValue != null ? newValue : oldValue;
//                                        scope.initialize(form);
//                                    }
//                                });
//                            }
//                        }
//                    }
//                    controller:
//                        ['$compile', '$scope',
//                        function($compile, scope) {
//
//                        }
//                    ],
                    link: function (scope, element, attr) {
                        // Hide this form initially
    //                    element.hide();
                        scope.$root.$broadcast('wfEvent:start-loading');
                        scope.initialize = function(form) {
                            element.show();
                            scope.$root.$broadcast('wfEvent:stop-loading');
                            element.attr("action", form.action);
                            element.attr("method", "POST");
                            element.attr("enctype", "multipart/form-data");

                            var created = form.task != null ? form.task.startTime : null;
                            element.attr('data-wf-task-started', created);
                        };
                        scope.$watch('form', function(oldValue, newValue) {
                            if (newValue != null || oldValue != null) {
                                var form = newValue != null ? newValue : oldValue;
                                scope.initialize(form);
                            }
                        });
                    }
                }
            }
        ])
        .directive('wfImage', [
            function() {
                return {
                    restrict: 'A',
                    scope: {

                    },
                    link: function (scope, element, attr) {
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            var data = form.data;
                            var fieldName = attr.wfImage;
                            var subFieldName = null;
                            var indexOfPeriod = fieldName.indexOf('.');
                            if (indexOfPeriod != -1) {
                                subFieldName = fieldName.substring(indexOfPeriod+1);
                                fieldName = fieldName.substring(0, indexOfPeriod);
                            }
                            var values = typeof(data) !== 'undefined' ? data[fieldName] : null;
                            if (values != null) {
                                angular.forEach(values, function(value) {
                                    if (value != null) {
                                        var imageObject = value;
                                        if (subFieldName != null)
                                            imageObject = value[subFieldName];

                                        var src = typeof(value) === 'string' ? value : value.link;
                                        element.attr('src', src);
                                        scope.$on('wfEvent:value-updated:' + fieldName, function(event, value) {
                                            var src = typeof(value) === 'string' ? value : value.link;
                                            element.attr('src', src);
    //                                        element.attr('data-wf-last-modified', value.lastModified);
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            }
        ])
        .directive('wfList', ['$http', '$sce', 'attachmentService', 'dateFilter',
            function($http, $sce, attachmentService, dateFilter) {
                return {
                    restrict: 'A',
                    scope: {

                    },
                    link: function (scope, element, attr) {
                        scope.loadValue = function($listElement, value, fieldName, subFieldName, templateHtml, $fallbackHtml) {
                            if (value != null) {
                                var realValue = value;
                                if (subFieldName != null)
                                    realValue = value[subFieldName];

                                var current = templateHtml.clone();
                                current.show();
                                current.removeClass('template');

                                if (typeof(realValue) !== 'undefined') {
                                    var anchor = current.find('a[data-wf-link]');

                                    if (typeof(realValue) !== 'string') {
                                        anchor.attr('href', realValue.link);
                                        anchor.text(realValue.name);
                                    } else {
                                       anchor.attr('href', realValue);
                                       anchor.text(realValue);
                                    }
                                }

                                if (typeof(realValue.description) !== 'undefined') {
                                    var descriptionTag = current.find('[data-wf-description]');
                                    descriptionTag.text(realValue.description);
                                }

                                if (typeof(realValue.lastModified) !== 'undefined' && realValue.lastModified != null) {
                                    var lastModifiedTag = current.find('[data-wf-last-modified]');
                                    lastModifiedTag.attr('data-wf-last-modified', realValue.lastModified);
                                    lastModifiedTag.text(dateFilter(realValue.lastModified, 'MMM d, y H:mm a'));
                                }

                                if (typeof(realValue.user) !== 'undefined' && typeof(realValue.user.displayName) !== 'undefined') {
                                    var ownerTag = current.find('[data-wf-owner]');
                                    ownerTag.text(realValue.user.displayName);
                                }

                                var deleteBtn = current.find('[data-wf-delete]').click(function(event) {
                                    var $target = $(event.target).closest('[data-wf-list]');
                                    var $listElement = $target.find('ul');
                                    var $listItem = $(event.target).closest('li');
                                    var $fallbackHtml = $target.find('[data-wf-fallback]');
                                    var url = realValue.link + '/removal.json';
                                    $http.post($sce.trustAsResourceUrl(url), null, {
                                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                                        transformRequest: angular.identity
                                    })
                                    .success(function(data, status, headers, config) {
                                        $listItem.remove();
                                        if ($listElement.find('li').length == 1)
                                            $fallbackHtml.show();
                                    })
                                    .error(function(data, status, headers, config) {

                                    });
                                });

                                $listElement.show();
                                $listElement.append(current);
                                if (element.length > 0)
                                    element.scrollTop(element[0].scrollHeight);

                                var $listItems = $listElement.find('li');
                                if ($listItems.length > 0)
                                    $fallbackHtml.hide();
                            }
                        };
                        scope.loadMultipleValues = function(element, values, fieldName, subFieldName, childHtml) {
                            var fallbackHtml = element.find('[data-wf-fallback]');
                            var listElement = element.find('ul');

                            // If the element doesn't have a ul child then just use the element itself
                            if (listElement.length == 0)
                                listElement = element;

                            listElement.hide();
                            listElement.empty();
                            listElement.append(childHtml);

                            var eventName = 'wfEvent:value-updated:' + fieldName;

                            if (scope.valueUpdatedListener == null)
                                scope.valueUpdatedListener = scope.$on(eventName, function(event, updatedValue) {
                                    scope.loadValue(listElement, updatedValue, fieldName, subFieldName, childHtml, fallbackHtml);
                                });

                            if (values != null && values.length > 0) {
                                angular.forEach(values, function(value) {
                                    scope.loadValue(listElement, value, fieldName, subFieldName, childHtml, fallbackHtml);
                                });
                            } else {
                                fallbackHtml.show();
                            }
                        };
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            var data = form.data;
                            var fieldName = attr.wfList;
                            var subFieldName = null;
                            var indexOfPeriod = fieldName.indexOf('.');
                            if (indexOfPeriod != -1) {
                                subFieldName = fieldName.substring(indexOfPeriod+1);
                                fieldName = fieldName.substring(0, indexOfPeriod);
                            }
                            var values = typeof(data) !== 'undefined' ? data[fieldName] : null;

    //                        scope.some.files = values;
                            var childHtml = element.find('li:first');
                            var fallbackHtml = element.find('[data-wf-fallback]');
                            if (!childHtml.hasClass('template')) {
                                childHtml.addClass('template');
                                childHtml.hide();
                            }

                            if (fieldName == 'attachments') {
                                scope.$on('wfEvent:attachments', function(event, attachments) {
                                    scope.loadMultipleValues(element, attachments, fieldName, subFieldName, childHtml);
                                });
                                attachmentService.refreshAttachments(scope.form);
                            } else {
                                scope.loadMultipleValues(element, values, fieldName, subFieldName, childHtml);
                            }
                        });
                    }
                }
            }
        ])
        .directive('wfLogin', ['attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
            function(attachmentService, dialogs, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/form-login.html',
                    link: function (scope, element) {
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                    }
                }
            }
        ])
        .directive('wfMultipage', ['wizardService',
             function(wizardService) {
                 return {
                     restrict: 'AE',
                     scope: {
                         form : '='
                     },
                     templateUrl: 'templates/multipage.html',
                     link: function (scope, element) {
                         if (typeof(scope.state) == 'undefined') {
                             scope.state = {};
                             scope.state.isViewingAttachments = false;
                         }
                         scope.$root.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                             scope.state.isViewingAttachments = isViewingAttachments;
                         });
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
                         form : '='
                     },
                     templateUrl: 'templates/multistep.html',
                     link: function (scope, element) {
                        if (typeof(scope.state) == 'undefined') {
                             scope.state = {};
                             scope.state.isViewingAttachments = false;
                         }
                         scope.$root.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                             scope.state.isViewingAttachments = isViewingAttachments;
                         });
                        scope.wizard = wizardService;
                     }
                 }
             }
        ])
        .directive('wfNamebar', ['attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
            function(attachmentService, dialogs, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/form-namebar.html',
                    link: function (scope, element) {
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                    }
                }
            }
        ])
        .directive('wfNotifications', ['wizardService',
             function(wizardService) {
                 return {
                     restrict: 'AE',
                     scope: {

                     },
                     templateUrl: 'templates/notifications.html',
                     link: function (scope, element) {
                        scope.$root.$on('wfEvent:notification', function(event, notification) {
                            // Ensure that our notifications array exists in this scope
                            if (typeof(scope.notifications) === 'undefined')
                                scope.notifications = new Array();
                            // Add this notification to the array
                            scope.notifications.push(notification);
                        });
                     }
                 }
             }
        ])
        .directive('wfPage', [
            function() {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/page.html',
                    link: function (scope, element) {
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                    }
                }
            }
        ])
        .directive('wfPerson', ['personService',
            function(personService) {
                return {
                    restrict: 'AE',
                    scope: {
                        name : '@',
                        required : '@'
                    },
                    templateUrl: 'templates/person.html',
                    transclude: true,
                    link: function (scope, element, attr) {
    //                    scope.person = {};
                        scope.disabled = typeof(attr.disabled) !== 'undefined' && attr.disabled == 'true';
                        scope.enabled = !scope.disabled;
                        scope.required = typeof(attr.required) !== 'undefined' && attr.required == 'true';
                        scope.getPeople = personService.getPeople;
                        scope.isDisabled = function() {
                            return scope.disabled;
                        };
                    }
                }
            }
        ])
        .directive('wfReview', ['wizardService',
             function(wizardService) {
                 return {
                     restrict: 'AE',
                     scope: {
                         form : '='
                     },
                     templateUrl: 'templates/review.html',
                     link: function (scope, element) {
                        if (typeof(scope.state) == 'undefined') {
                            scope.state = {};
                            scope.state.isViewingAttachments = false;
                        }
                        scope.$root.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                            scope.state.isViewingAttachments = isViewingAttachments;
                        });
                        scope.wizard = wizardService;
                     }
                 }
             }
        ])
        .directive('wfScreen', [
            function() {
                return {
                    restrict: 'A',
                    scope: {

                    },
                    link: function (scope, element, attr) {

                        var step = attr.wfScreen;
                        if (typeof(step) === 'undefined')
                            step = '-1';

                        var indexOf = step.indexOf('+');
                        var upwards = false;
                        if (indexOf == (step.length-1)) {
                            step = step.substring(0, indexOf);
                            upwards = true;
                        }

                        step = parseInt(step);

                        scope.isCompletion = function(form) {
                            return form != null && form.actionType != 'VIEW' && ((form.actionType == 'COMPLETE' && form.task == null) || (form.task != null && !form.task.active));
                        };

                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            if (scope.isCompletion(form)) {
                                if (scope.form.task == null || scope.form.task.taskAction == 'COMPLETE') {
                                    element.toggle(attr.wfScreen == 'confirmation');
                                } else if (scope.form.task.taskAction == 'REJECT') {
                                    element.toggle(attr.wfScreen == 'rejection');
                                }
                            }

                            // From wfActive (below)
                            var isDisabled = false;

                            // Check to see if the current user is the assigned user
                            if (typeof(form) !== 'undefined' && form.task != null) {
                                isDisabled = (form.currentUser == null || form.task.assignee == null || form.currentUser.userId !== form.task.assignee.userId)
                            }

                            var ordinal = form.activeStepOrdinal;

                            if (form.actionType == 'VIEW')
                                ordinal = -1;

                            if (upwards) {
                                if (typeof(ordinal) !== 'undefined' && step < ordinal)
                                    isDisabled = true;
                            } else if (step != ordinal) {
                                isDisabled = true;
                            }

                            var $input = element.find(":input");
                            scope.disabled = isDisabled;
                            if (isDisabled) {
                                $input.each(function(index, input) {
                                    $(input).attr('disabled', 'disabled');
                                    if ($(input).attr('type') == 'radio') {
                                        $(input).attr('type', 'checkbox');
                                        $(input).addClass('wf-was-radio');
                                    }
                                });

                            } else {
                                $input.each(function(index, input) {
                                    $(input).removeAttr('disabled');
                                    if ($(input).hasClass('wf-was-radio')) {
                                        $(input).removeClass('wf-was-radio');
                                        $(input).attr('type', 'radio');
                                    }
                                });
                            }

                        });

                        element.hide();

                        if (attr.wfScreen != 'confirmation' && attr.wfScreen != 'rejection') {
                            scope.$root.$on('wfEvent:step-changed', function(event, ordinal) {
                                if (scope.isCompletion(scope.form)) {
                                    element.hide();
                                } else if (upwards) {
                                    element.toggle(ordinal >= step);
                                } else {
                                    element.toggle(step == ordinal);
                                }
                            });
                        }
                    }
                }
            }
        ])
    //    .filter('selectedFacets', function(facets) {
    //
    //        return facets;
    //    })
        .directive('wfSearchResponse', ['$filter', '$resource', 'attachmentService', 'dialogs', 'localStorageService', 'notificationService', 'taskService', 'wizardService',
            function($filter, $resource, attachmentService, dialogs, localStorageService, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/searchresponse.html',
                    link: function (scope, element) {
                        scope.clearFilter = function(facet) {
                            delete scope.criteria[facet.name];
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
    //                        scope.isFiltering = false;
                        };
                        scope.doFilter = function() {
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
                        scope.doChangeFilter = function(facet) {
                            console.log(facet.name);
    //                        scope.criteria[facet.name] = facet.model;
                            if (scope.criteria[facet.name] != null) {
                                scope.$root.$broadcast('wfEvent:search', scope.criteria);
                            }
                        };
                        scope.hasFilter = function(facet) {
                            var filterValue = scope.criteria[facet.name];
                            return typeof(filterValue) !== 'undefined' && filterValue != null && filterValue != '' && filterValue.length > 0;
                        };
                        scope.onFilterKeyUp = function(facet, event) {
                            if (event.keyCode == 27) {
                                scope.clearFilter(facet);
                                return;
                            }
                            if (scope.filterTimeout != null)
                                clearTimeout(scope.filterTimeout);
                            scope.filterTimeout = setTimeout(scope.doChangeFilter, 300, facet);
                        };
                        scope.doSort = function(facet) {
                            // If already sorting by this facet then switch the direction
                            if (scope.isSorting(facet)) {
                                if (facet.direction == 'desc')
                                    facet.direction = 'asc';
                                else
                                    facet.direction = 'desc';
                            } else {
                                facet.direction = 'desc';
                            }

                            scope.criteria.sortBy = [];
                            scope.criteria.sortBy.push(facet.name + ":" + facet.direction);
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
                        scope.getFacetValue = function(form, facet) {
                            if (facet.type !== 'user' && form[facet.name] == null)
                                return null;

                            if (facet.type == 'datetime')
                                return $filter('date')(form[facet.name], 'MMM d, y H:mm');
                            else if (facet.type == 'date')
                                return $filter('date')(form[facet.name], 'MMM d, y');
                            else if (facet.type == 'user')
                                return form[facet.name] != null && form[facet.name].displayName != null ? form[facet.name].displayName : 'Nobody';
                            if (scope.criteria[facet.name] != null && scope.criteria[facet.name] != '')
                                scope.isFiltering = true;

                            return form[facet.name];
                        };
                        scope.isSingleProcessSelected = function() {
                            return scope.criteria.processDefinitionKey != null && scope.criteria.processDefinitionKey != '';
                        };
                        scope.isSorting = function(facet) {
                            var pattern = '^' + facet.name + ':';
                            var regex = new RegExp(pattern);
                            var isSorting = false;
                            angular.forEach(scope.criteria.sortBy, function(sortBy) {
                                isSorting = regex.test(sortBy);
                                if (isSorting)
                                    return true;
                            });
                            return isSorting;
                        };
                        scope.onDateChange = function(facet) {
                            scope.doChangeFilter(facet);
                        };
                        scope.processSearchResults = function(results) {
                            scope.$root.$broadcast('wfEvent:found', results);
                        };
                        scope.selectForm = function(form) {
                            form.checked = !form.checked;
                            if (!form.checked)
                                scope.allChecked = false;
                            if (form.checked && scope.selectedFormMap[form.formInstanceId] == null)
                                scope.selectedFormMap[form.formInstanceId] = form;
                            else if ( !form.checked && scope.selectedFormMap[form.formInstanceId] != null)
                                delete scope.selectedFormMap[form.formInstanceId];

                            scope.$root.$broadcast('wfEvent:change-selection', scope.selectedFormMap);
                        };
                        scope.selectAllForms = function(forms, checked) {
                            if (forms != null) {
                                scope.allChecked = !scope.allChecked;
                                angular.forEach(forms, function(form) {
                                    form.checked = scope.allChecked;
                                    if (form.checked && scope.selectedFormMap[form.formInstanceId] == null)
                                        scope.selectedFormMap[form.formInstanceId] = form;
                                    else if ( !form.checked && scope.selectedFormMap[form.formInstanceId] != null)
                                        delete scope.selectedFormMap[form.formInstanceId];
                                });
                                scope.$root.$broadcast('wfEvent:change-selection', scope.selectedFormMap);
                            }
                        };
                        scope.SearchResponse = $resource('./form', {processStatus:'@processStatus'});
                        scope.$on('wfEvent:clear-filters', function() {
                            var didClear = false;
                            angular.forEach(scope.facets, function(facet) {
                                if (scope.criteria[facet.name] != null && scope.criteria[facet.name] != '') {
                                    delete scope.criteria[facet.name];
                                    didClear = true;
                                }
                            });
                            if (didClear)
                                scope.$root.$broadcast('wfEvent:search', scope.criteria);
                            scope.isFiltering = false;
                        });
                        scope.$on('wfEvent:columns-toggle', function(event) {
                            dialogs.openColumnsModal(scope.facets);
                        });
                        scope.$on('wfEvent:filter-toggle', function(event) {
                            scope.isFiltering = !scope.isFiltering;
                            if (!scope.isFiltering)
                                scope.$root.$broadcast('wfEvent:clear-filters');
                        });
                        scope.$on('wfEvent:facet-changed', function(event, facet) {
                            console.log("Storing facets");
                            scope.facetMap[facet.name].selected = facet.selected;
                            localStorageService.set("facetMap", scope.facetMap);

                            scope.selectedFacets = [];
                            angular.forEach(scope.facets, function(facet, index) {
                                if (facet.selected && facet.name != 'processInstanceLabel')
                                    scope.selectedFacets.push(facet);
                            });
                        });
                        scope.$on('wfEvent:search', function(event, criteria) {
                            if (typeof(criteria) !== 'undefined') {
                                scope.criteria = criteria;
                            }
                            if (scope.criteria.keywords != null && typeof(scope.criteria.keywords) == 'string') {
                                scope.criteria.keyword = scope.criteria.keywords.split(' ');
                            }
                            if (scope.facets != null) {
                                angular.forEach(scope.facets, function(facet) {
                                    var isFilteringThisFacet = false;
                                    if (facet.type == 'date') {
                                        var afterName = facet.name + 'After';
                                        var beforeName = facet.name + 'Before';
                                        var afterCriterion = scope.criteria[afterName];
                                        var beforeCriterion = scope.criteria[beforeName];
                                        if (afterCriterion != null && afterCriterion != '')
                                            isFilteringThisFacet = true;
                                        else if (beforeCriterion != null && beforeCriterion != '')
                                            isFilteringThisFacet = true;
                                    } else {
                                        var criterion = scope.criteria[facet.name];
                                        if (criterion != null && criterion != '')
                                            isFilteringThisFacet = true;
                                    }

                                    if (isFilteringThisFacet) {
                                        facet.selected = true;
                                        scope.isFiltering = true;
                                    }

                                    // special handling for applicationStatusExplanation
                                    // @see https://jira.cac.washington.edu/browse/EDMSIMPL-177
                                    if ( facet.name == 'applicationStatusExplanation' ) {
                                        if ( scope.criteria != null && scope.criteria.processStatus == 'suspended' ) {
                                            facet.selected = true;  // show suspension reason for suspended process instances
                                        } else {
                                            facet.selected = false;  // hide suspension reason otherwise
                                        }
                                    }
                                });
                            }
                            scope.SearchResponse.get(scope.criteria, scope.processSearchResults);
                        });
                        scope.$on('wfEvent:found', function(event, results) {
                            scope.forms = results.data;

                            scope.paging.total = results.total;
                            scope.paging.pageNumber = results.pageNumber + 1;
                            scope.paging.pageSize = results.pageSize;

                            scope.paging.required = (scope.paging.pageNumber > 1 || scope.paging.total > scope.paging.pageSize);

                            scope.paging.pageNumbers = [];
                            var numberOfPages = scope.paging.total / scope.paging.pageSize + 1;
                            for (var i=1;i<=numberOfPages;i++) {
                                scope.paging.pageNumbers.push(i);
                            }

                            if (scope.processDefinitionDescription == null)
                                scope.processDefinitionDescription = {};

                            angular.forEach(results.metadata, function(definition) {
                                scope.processDefinitionDescription[definition.processDefinitionKey] = definition.processDefinitionLabel;
                            });

                            if (true) {
                                scope.selectedFormMap = new Object();
                                scope.allChecked = false;
                                scope.criteria.sortBy = results.sortBy;

                                scope.facetMap = localStorageService.get("facetMap");
                                if (scope.facetMap == null || scope.facetMap.length == 0) {
                                    scope.facetMap = {};
                                }

                                var includeFacets = false;
                                if (scope.facets == null) {
                                    includeFacets = true;
                                    scope.facets = [];
                                }

                                scope.selectedFacets = [];
                                angular.forEach(results.facets, function(facet) {
                                    facet.link = facet.name == 'processInstanceLabel';
                                    var localFacet = scope.facetMap[facet.name];
                                    if (localFacet != null)
                                        facet.selected = localFacet.selected;
                                    else
                                        facet.selected = facet.required;

                                    if (facet.selected && facet.name != 'processInstanceLabel')
                                        scope.selectedFacets.push(facet);

                                    scope.facetMap[facet.name] = facet;
                                    if (includeFacets)
                                        scope.facets.push(facet);
                                });

                                localStorageService.set("facetMap", scope.facetMap);

                                angular.forEach(scope.criteria.sortBy, function(sortBy) {
                                    var indexOf = sortBy.indexOf(':');
                                    if (indexOf != -1) {
                                        var name = sortBy.substring(0, indexOf);
                                        var direction = sortBy.substring(indexOf+1);
                                        var facet = scope.facetMap[name];
                                        if (facet != null) {
                                            facet.direction = direction;
                                        }
                                    }
                                });

                                var specialFields = ['activation', 'assignment', 'attachment', 'cancellation', 'history', 'restart', 'suspension', 'bucketUrl'];
                                scope.displayedForms = [];
                                angular.forEach(results.data, function(form) {
                                    var displayedForm = {'formInstanceId': form.formInstanceId, 'link' : form.link };
                                    angular.forEach(specialFields, function(field) {
                                        displayedForm[field] = form[field];
                                    });
                                    angular.forEach(scope.facets, function(facet) {
                                        var key = facet.name;
                                        var value = scope.getFacetValue(form, facet);
                                        displayedForm[key] = value;
                                    });
                                    scope.displayedForms.push(displayedForm);
                                });

                                scope.$root.$broadcast('wfEvent:change-selection', scope.selectedFormMap);
                            }
                        });
                        scope.$root.$broadcast('wfEvent:results-linked');
                    },
                    controller: ['$scope', function(scope) {
                        scope.$watch(scope.forms, function(oldValue, newValue) {

                        });

                        if (typeof(scope.criteria) === 'undefined')
                            scope.criteria = {};
                        if (scope.paging == null)
                            scope.paging = {};

                        scope.dialogs = dialogs;

                        if (scope.paging == null)
                            scope.paging = {};

                        scope.paging.pageNumbers = [1,2,3,4,5];
                        scope.paging.changePageSize = function(event) {
                            scope.criteria.pageSize = scope.paging.pageSize;
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
                        scope.paging.previousPage = function() {
                            scope.criteria.pageNumber = scope.paging.pageNumber >= 2 ? scope.paging.pageNumber - 2 : 0;
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
                        scope.paging.toPage = function(pageNumber) {
                            scope.criteria.pageNumber = pageNumber - 1;
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
                        scope.paging.nextPage = function() {
                            scope.criteria.pageNumber = scope.paging.pageNumber;
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };

                        scope.isFiltering = false;
                    }]
                }
            }
        ])
        .directive('wfSearchToolbar', ['$window', 'attachmentService', 'dialogs', 'localStorageService', 'notificationService', 'taskService', 'wizardService', 'instanceService',
            function($window, attachmentService, dialogs, localStorageService, notificationService, taskService, wizardService, instanceService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    templateUrl: 'templates/searchtoolbar.html',
                    link: function (scope, element) {
                        if (typeof(scope.selectedFormMap) === 'undefined')
                            scope.selectedFormMap = {};
                        scope.context = $window.piecework.context;
                        scope.state = new Object();
                        scope.state.isCollapsed = false;
                        scope.state.toggleCollapse = function() {
                            scope.state.isCollapsed = !scope.state.isCollapsed;
                        };

                        scope.processDefinitionDescription = new Object();
                        scope.processDefinitionDescription[''] = 'Any process';

                        scope.criteria = localStorageService.get("criteria");
                        if (scope.criteria == null) {
                            console.log("New criteria");
                            scope.criteria = new Object();
                            scope.criteria.keywords = [];
                            scope.criteria.processDefinitionKey = '';
                            scope.criteria.processStatus = 'open';
                            scope.criteria.taskStatus = 'all';
                        }

                        scope.exportCsv = function(selectedForms) {
                            var url = "/workflow/ui/instance.xls?processDefinitionKey=" + scope.criteria.processDefinitionKey;
                            if (scope.criteria.startedAfter != null)
                                url += '&startedAfter=' + scope.criteria.startedAfter;
                            if (scope.criteria.startedBefore != null)
                                url += '&startedBefore=' + scope.criteria.startedBefore;
                            $window.location.href = url;
                        };

                        scope.processStatusDescription = {
                            'open': 'Active',
                            'complete': 'Completed',
                            'cancelled': 'Cancelled',
                            'suspended': 'Suspended',
                            'queued': 'Queued',
                            'all': 'Any status'
                        };
                        scope.taskStatusDescription = {
                            'Open': 'Open tasks',
                            'Complete': 'Completed tasks',
                            'Cancelled': 'Cancelled tasks',
                            'Rejected': 'Rejected tasks',
                            'Suspended': 'Suspended tasks',
                            'all': 'All tasks'
                        };

                        scope.showReportPanel = function() {

                        };

                        scope.toggleColumns = function() {
                            scope.$root.$broadcast('wfEvent:columns-toggle');
                        };

                        scope.toggleFilter = function() {
                            scope.$root.$broadcast('wfEvent:filter-toggle');
                        };

                        scope.getFormsSelected = function(taskStatuses) {
                            var formIds = Object.keys(scope.selectedFormMap);
                            var selectedForms = new Array();
                            var acceptableTaskStatuses = new Object();
                            angular.forEach(taskStatuses, function(taskStatus) {
                                acceptableTaskStatuses[taskStatus] = true;
                            });
                            angular.forEach(formIds, function(formId) {
                                var form = scope.selectedFormMap[formId];
                                if (typeof(form) !== 'undefined' && form != null) {
                                    if (typeof(taskStatuses) === 'undefined' || taskStatuses == null ||
                                        acceptableTaskStatuses[form.taskStatus] != null)
                                        selectedForms.push(form);
                                }
                            });
                            return selectedForms;
                        };

                        scope.isSingleFormSelected = function(taskStatuses) {
                            if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                                var selectedForms = scope.getFormsSelected(taskStatuses);
                                return selectedForms.length === 1;
                            }

                            return Object.keys(scope.selectedFormMap).length === 1;
                        };

                        scope.isFormSelected = function(taskStatuses) {
                            if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                                var selectedForms = scope.getFormsSelected(taskStatuses);
                                return selectedForms.length !== 0;
                            }

                            return Object.keys(scope.selectedFormMap).length !== 0;
                        };

                        scope.isSingleProcessSelected = function() {
                            return scope.criteria.processDefinitionKey != null && scope.criteria.processDefinitionKey != '';
                        };

                        scope.isSingleProcessSelectable = function() {
                            return typeof(scope.metadata) !== 'undefined' && scope.metadata.length == 1;
                        };

                        scope.$on('wfEvent:change-selection', function(event, selectedFormMap) {
                            scope.selectedFormMap = selectedFormMap;
                        });

                        scope.$on('wfEvent:found', function(event, results) {
                            console.log('Found');
                            scope.searching = false;
                            if (scope.definitions == null) {
                                scope.definitions = results.metadata;
                                scope.selectedFormMap = {};
                                scope.$root.currentUser = results.currentUser;
                                angular.forEach(results.metadata, function(definition) {
                                    scope.processDefinitionDescription[definition.processDefinitionKey] = definition.processDefinitionLabel;
                                });
                                if (results.metadata != null && results.metadata.length == 1)
                                    scope.criteria.processDefinitionKey = results.metadata[0].processDefinitionKey;
                            }
                            scope.bucketList = results.bucketList;
                            scope.criteria.pg = results.processGroup;
                        });
                        scope.$on('wfEvent:search', function(event, criteria) {
                            console.log('Searching');
                            scope.forms = null;
                            scope.searching = true;
                            console.log("Storing criteria");
                            localStorageService.set("criteria", criteria);
                        });
                        scope.showSearchingIcon = function() {
    //                        $('#searchIcon').addClass('fa-spinner fa-spin').removeClass('fa-search');
                        };

                        scope.dialogs = dialogs;

                        scope.onSearchKeyUp = function(event) {
                            if (event.keyCode == 27) {
                                scope.clearSearch();
                                return;
                            }
                            if (scope.searchTimeout != null)
                                clearTimeout(scope.refreshSearch);
                            scope.searchTimeout = setTimeout(scope.refreshSearch, 300);
                        };

                        scope.clearSearch = function() {
                            scope.criteria.keyword = '';
                            scope.criteria.keywords = '';
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };

                        scope.refreshSearch = function() {
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        };
    //                    scope.$root.$broadcast('wfEvent:search', scope.criteria);

                        scope.model = $window.piecework.model;
                        if (typeof(scope.model) !== 'undefined' && typeof(scope.model.total) !== 'undefined') {
                            scope.$on('wfEvent:results-linked', function(event) {
                                delete scope.model['data'];
                                scope.$root.$broadcast('wfEvent:found', scope.model);
                                delete $window.piecework['model'];
                                scope.$root.$broadcast('wfEvent:search', scope.criteria);
                            });
                        } else {
                            scope.$root.$broadcast('wfEvent:search', scope.criteria);
                        }

                        scope.changeBucket = function(selectedForms, bucket) {
                            var success = function(scope, data, status, headers, config, form) {
                                form.Bucket = bucket;
                            };

                            var failure = function(scope, data, status, headers, config, form) {
                                dialogs.alert(data.messageDetail);
                            };

                            angular.forEach(selectedForms, function(form) {
                                if (form.Bucket !== bucket) {
                                   instanceService.changeBucket(scope, form, bucket, success, failure);
                                }
                            });
                        };
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
                     templateUrl: 'templates/status.html',
                     link: function (scope, element) {
                        if (typeof(scope.form) === 'undefined')
                            scope.form = scope.$root.form;

                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            console.log("wfStatus attached form to its scope");
                            if (typeof(form) !== 'undefined') {
                                if (form.loadedBy == null)
                                    form.loadedBy = [];
                                form.loadedBy.push('wfStatus');
                                scope.form = form;
                            }
                        });
                        scope.claim = function() {
                            var success = function(scope, data, status, headers, config, form, assignee) {
                                $rootScope.$broadcast('wfEvent:refresh', 'assignment');
                            };

                            var failure = function(scope, data, status, headers, config, form, assignee) {
                                form._assignmentStatus = 'error';
                                var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                                var message = form.processInstanceLabel + ' cannot be assigned ';
                                var title = data.messageDetail;
                                notificationService.notify(scope, message, title);
                            };
                            taskService.assignTask(scope, scope.form, scope.form.currentUser.userId, success, failure);
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
                    templateUrl: 'templates/step.html',
                    //transclude: true,
                    link: function (scope, element) {
                        scope.wizard = wizardService;
                    }
                }
            }
        ])
        .directive('wfToolbar', ['$sce', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wfUtils', 'wizardService',
            function($sce, attachmentService, dialogs, notificationService, taskService, wfUtils, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {

                    },
                    controller: 'wfToolbarController',
                    link: function (scope, element) {
                        wfUtils.attachForm(scope);
                        scope.$watch('form', function(original, updated) {
                            var form = updated != null ? updated : original;
                            if (form != null && form.fileUploadOptions != null) {
                                scope.fileUploadOptions.url = scope.form.attachment;
                            }
                        });

                        if (typeof(scope.state) == 'undefined') {
                            scope.state = {};
                            scope.state.isCollapsed = false;
                            scope.state.isViewingAttachments = false;
                        }

                        scope.$root.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                            scope.state.isViewingAttachments = isViewingAttachments;
                        });
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                        scope.dialogs = dialogs;
                        scope.wizard = wizardService;

                        scope.getAttachmentUrl = function() {
                            if (typeof(scope.form) === 'undefined' || scope.form == null) {
                                return '/';
                            }
                            return $sce.getTrustedResourceUrl(scope.form.attachment);
                        };
                    },
                    template:
                        '     <nav class="navbar navbar-default navbar-ex1-collapse" style="border-radius: 0px">\n' +
                        '         <div class="navbar-header">\n' +
                        '             <button data-ng-click="toggleCollapse()" type="button" class="navbar-toggle">\n' +
                        '                 <span class="sr-only">Toggle</span>\n' +
                        '                 <span class="icon-bar"></span>\n' +
                        '                 <span class="icon-bar"></span>\n' +
                        '                 <span class="icon-bar"></span>\n' +
                        '             </button>\n' +
                        '         </div>\n' +
                        '         <div data-ng-class="state.isCollapsed ? \'\' : \'collapse\'" class="navbar-collapse navbar-ex1-collapse">\n' +
                        '             <div class="container"><div class="row">\n' +
                        '                 <div class="navbar-left btn-toolbar">\n' +
                        '                     <button data-ng-click="dialogs.openHistoryModal([form])" data-ng-show="form.history" class="btn btn-default selected-result-btn navbar-btn" id="history-dialog-button" title="History" type="button"><i class="fa fa-calendar-o fa-white"></i></button>\n' +
                        '                     <button data-ng-click="dialogs.openSuspendModal([form])" data-ng-show="form.history && form.task.active" class="btn btn-default navbar-btn" title="Suspend process" type="button"><i class="fa fa-pause fa-white"></i></button>\n' +
                        '                     <button data-ng-click="dialogs.openCancelModal([form])" data-ng-show="form.history && form.task.active" class="btn btn-danger navbar-btn" id="delete-dialog-button" title="Delete process" type="button"><i class="fa fa-trash-o"></i></button>\n' +
                        '                     <button data-ng-click="dialogs.openActivateModal([form])" data-ng-show="form.history && form.task.taskStatus == \'Suspended\'" class="btn btn-default navbar-btn" id="activate-dialog-button" title="Activate process" type="button"><i class="fa fa-play"></i></button>\n' +
                        '                     <button data-ng-click="dialogs.openRestartModal([form])" data-ng-show="form.history && (form.task.taskStatus == \'Cancelled\' || form.task.taskStatus == \'Completed\')" class="btn btn-default navbar-btn" title="Restart process" type="button"><i class="fa fa-rotate-left"></i></button>\n' +
                        '                 </div>\n' +
                        '                 <div class="navbar-right btn-toolbar">\n' +
                        '                       <div data-wf-assignment-button data-form="form" class="navbar-nav"></div>' +
                        '                         <div class="btn-group"><a data-ng-click="dialogs.openCommentModal([form])" data-ng-show="form.allowAttachments && form.history" class="btn btn-default navbar-btn" id="comment-button" data-target="#comment-dialog" data-backdrop="false" data-toggle="modal" title="Add comment" type="button"><i class="fa fa-comment-o"></i></a></div>\n' +
                        '                         <div class="btn-group">\n' +
                        '                             <form data-ng-show="form.allowAttachments && form.history" class="navbar-left form-inline" action="{{getAttachmentUrl()}}" method="POST" enctype="multipart/form-data" data-file-upload="fileUploadOptions">\n' +
                        '                                 <span class="btn btn-default navbar-btn fileinput-button" data-ng-class="{disabled: disabled}">\n' +
                        '                                       <i ng-hide="state.sending" class="fa fa-paperclip"></i>  <i ng-show="state.sending" class=\'fa fa-paperclip fa-spin\'></i>\n' +
                        '                                       <input id="attachmentFile" type="file" name="attachment" multiple="" ng-disabled="disabled">\n' +
                        '                                 </span>\n' +
                        '                             </form>\n' +
                        '                         </div>\n' +
                        '                         <div class="btn-group"><button data-ng-click="viewAttachments()" data-ng-show="form.allowAttachments && form.history" class="btn btn-default navbar-btn" id="attachments-button" title="View comments and attachments" type="button"><i ng-class="state.isViewingAttachments ? \'fa-folder-open\' : \'fa-folder\'" class="fa fa-folder"></i>&nbsp;<span id="attachment-count">{{form.attachmentCount}}</span></button></div>\n' +
                        '                         <div class="btn-group"><a class="btn btn-default navbar-btn" href="{{form.root}}" rel="external" id="back-button" target="_self" title="Return to task list"><i class="fa fa-arrow-left"></i></a></div>\n' +
                        '                 </div>\n' +
                        '             </div></div>\n' +
                        '         </div>\n' +
                        '     </nav>'
                }
            }
        ])
        .directive('wfVariable', ['$filter',
            function($filter) {
                return {
                    restrict: 'A',
                    scope: {
                        name : '@',
                        type : '@'
                    },
                    template: '<span data-ng-repeat="value in values" data-ng-bind-html="value"></span>',
                    link: function (scope, element, attr) {
                        scope.values = [];
                        var name = attr.wfVariable;
                        var attributeName = null;
                        var indexOf = name != null ? name.indexOf('.') : -1;
                        if (indexOf != -1) {
                            attributeName = name.substring(indexOf+1);
                            name = name.substring(0, indexOf);
                        }

                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            scope.values = [];
                            if (form != null && form.data != null && form.data[name] != null && form.data[name].length > 0) {
                                var rawValues = form.data[name];
                                var values = [];
                                angular.forEach(rawValues, function(rawValue) {
                                    if (rawValue != null) {
                                        if (scope.type == 'date') {
                                            if (rawValue != '0NaN-NaN-NaNTNaN:NaN:NaNNaNNaN') {
                                                var value = $filter('date')(new Date(rawValue), 'MMM d, y');
                                                values.push(value);
                                            }
                                        } else if (attributeName != null) {
                                            var value = rawValue[attributeName];
                                            if (value != null)
                                                values.push(value);
                                        } else {
                                            values.push(rawValue);
                                        }

                                    }
                                });
                                scope.values = values;
                            }
                        });
                    }
                }
            }
        ])
        .directive('wfVariableUpload', ['attachmentService', '$rootScope',
            function(attachmentService, $rootScope) {
                return {
                    restrict: 'A',
                    scope: {
                        form : '='
                    },
                    link: function (scope, element, attr) {
                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                        });
                        var fieldName = attr.wfVariableUpload;

                        element.find('button').click(function(event) {
                            var data = new FormData();
                            var $inputs = element.find(':input');
                            $inputs.each(function(index, input) {
                                var $input = $(input);
                                data.append(input.name, $input.val());
                            });

                            var url = scope.form.attachment;
                            if (fieldName != 'attachments') {
                                url = url.replace('/attachment', '/value');
                                url += '/' + fieldName;
                            }

                            $.ajax({
                               url : url,
                               data : data,
                               processData : false,
                               contentType : false,
                               type : 'POST'
                            })
                            .done(function(data, textStatus, jqXHR) {
                               $inputs.val('');
                               if (fieldName == 'attachments')
                                    scope.$root.$broadcast('wfEvent:attachments', data.list);
                               else
                                    scope.$root.$broadcast('wfEvent:value-updated:' + fieldName, data);
                            })
                            .fail(function(jqXHR, textStatus, errorThrown) {
                               var data = $.parseJSON(jqXHR.responseText);
                               var selector = '.process-alert[data-element="' + input.name + '"]';
                               var message = data.messageDetail;
                               var $alert = $(selector);
                               $alert.show();
                               $alert.text(message);
                            });
                        });
                    }
                }
            }
        ])
        .directive('wfVisible', [
            function() {
                return {
                    restrict: 'A',
                    scope: {

                    },
                    link: function (scope, element, attr) {
                        var flowElement = attr.wfVisible;
                        if (typeof(flowElement) === 'undefined')
                            return;

                        var negate = flowElement.indexOf('!') == 0;
                        if (negate) {
                            flowElement = flowElement.substring(1);
                        }

                        scope.$root.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;

                            var task = typeof(form) !== 'undefined' ? form.task : null;
                            var active = (flowElement == 'start' && task == null) || (task != null && flowElement == task.taskDefinitionKey);
                            if (negate)
                                active = !active;
                            if (!active) {
                                element.addClass('ng-hide')
                            }
                        });
                    }
                }
            }
        ]
    );

})(window, document, angular);
