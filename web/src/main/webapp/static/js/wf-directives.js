(function(window, document, angular) {

    'use strict';

    var wfInputDirective = ['$browser', '$compile', '$document', '$sniffer', 'wfUtils',
        function($browser, $document, $compile, $sniffer, wfUtils) {
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
                        "       <li data-ng-repeat=\"candidateAssignee in form.task.candidateAssignees\"><a data-ng-click=\"assignTo(candidateAssignee.userId)\" class=\"candidate-assignee\" id=\"{{candidateAssignee.userId}}\">Assign to {{candidateAssignee.userId == application.currentUser.userId ? 'me' : candidateAssignee.displayName}}</a></li>\n" +
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
        .directive('wfColumnHeader', [
            function() {
                return {
                    restrict: 'AE',
                    scope: {
                        application: '=',
                        facet: '='
                    },
                    link: function(scope, element) {
                        scope.clearFilter = function(facet) {
                            if (facet != null && facet.name != null) {
                                delete scope.application.criteria[facet.name];
                                scope.application.search();
                            }
                        };
                        scope.doFilter = function() {
                            scope.application.search();
                        };
                        scope.doChangeFilter = function(facet) {
                            console.log(facet.name);
                            if (scope.application.criteria[facet.name] != null) {
                                scope.application.search();
                            }
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

                            scope.application.criteria.sortBy = [];
                            scope.application.criteria.sortBy.push(facet.name + ":" + facet.direction);
                            scope.application.search();
                        };
                        scope.hasFilter = function(facet) {
                            if (facet == null)
                                return false;
                            var filterValue = scope.application.criteria[facet.name];
                            return typeof(filterValue) !== 'undefined' && filterValue != null && filterValue != '' && filterValue.length > 0;
                        };
                        scope.isSorting = function(facet) {
                            if (facet == null)
                                return false;

                            var pattern = '^' + facet.name + ':';
                            var regex = new RegExp(pattern);
                            var isSorting = false;
                            angular.forEach(scope.application.criteria.sortBy, function(sortBy) {
                                isSorting = regex.test(sortBy);
                                if (isSorting)
                                    return true;
                            });
                            return isSorting;
                        };
                        scope.onDateChange = function(facet) {
                            scope.doChangeFilter(facet);
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
                    },
                    template:
                        '<div data-ng-if="facet.type == \'date\' || facet.type == \'datetime\'">' +
                        '   <label class="control-label"><a href="#" data-ng-click="doSort(facet)"><b>{{facet.label}}</b> <i data-ng-show="isSorting(facet)" data-ng-class="facet.direction == \'asc\' ? \'fa-caret-up\' : \'fa-caret-down\'" class="fa"></i></a></label>\n' +
                        '   <div data-ng-show="application.state.filtering" class="wf-filter">' +
                        '       <div data-wf-date-range data-name="facet.name" data-application="application" />' +
                        '   </div>' +
                        '</div>' +
                        '<div data-ng-if="facet.type !== \'date\' && facet.type !== \'datetime\'" class="form-group has-feedback">\n' +
                        '   <label class="control-label"><a href="#" data-ng-click="doSort(facet)"><b>{{facet.label}}</b> <i data-ng-show="isSorting(facet)" data-ng-class="facet.direction == \'asc\' ? \'fa-caret-up\' : \'fa-caret-down\'" class="fa"></i></a></label>\n' +
                        '   <div data-ng-show="application.state.filtering" class="wf-filter">' +
                        '       <input data-ng-keyup="onFilterKeyUp(facet, $event)" data-ng-hide="facet.type == \'date\' || facet.type == \'datetime\'" data-ng-model="application.criteria[facet.name]" autocomplete="off" type="text" class="form-control input-sm natural" placeholder="{{facet.label}}">\n' +
                        '       <span data-ng-click="clearFilter(facet)" data-ng-show="hasFilter(facet)" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span>\n' +
                        '   </div>' +
                        '</div>'
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

                        scope.onKeyUp = function(event) {
                            if (event == null)
                                return;

                            if (event.keyCode == 27) {
                                event.preventDefault();
                                event.stopPropagation();
                                scope.opened = false;
                            } else if (event.keyCode == 13) {
                                event.preventDefault();
                                event.stopPropagation();
                                scope.today();
                                scope.opened = false;
                            }
                        };

                        scope.open = function($event) {
                            if ($event != null) {
                                $event.preventDefault();
                                $event.stopPropagation();
                            }
                            scope.opened = true;
                        };

                        scope.clickDate = function($event) {
                            scope.today();
                            scope.open($event);
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
                        '   <input data-ng-keypress="onKeyUp($event)" data-ng-required="{{required}}" datepicker-popup="MM/dd/yyyy" data-ng-model="dt" datepicker-options="dateOptions" size="10" type="text" class="form-control wf-datepicker"  is-open="opened" min="minDate" max="maxDate" close-text="Close" show-weeks="false"/>' +
                        '   <input data-ng-value="dt|date:\'yyyy-MM-ddTHH:mm:ssZ\'" name="{{name}}" type="hidden">' +
                        '   <span class="input-group-addon">' +
                        '       <i class="fa fa-calendar" ng-click="clickDate($event)"></i>' +
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
                        application : '=',
                        name : '='
                    },
                    transclude: true,
                    link: function (scope, element) {
                        var afterName = scope.name + 'After';
                        var beforeName = scope.name + 'Before';

                        scope.after = scope.application.criteria[afterName];
                        scope.before = scope.application.criteria[beforeName];

                        scope.afterChange = function() {
                            if (scope.application.criteria[afterName] != scope.after) {
                                scope.before = null;
                                scope.application.criteria[beforeName] = null;
                                scope.beforeMinDate = scope.after;

                                if (scope.after != null)
                                    scope.application.criteria[afterName] = $filter('date')(scope.after, 'yyyy-MM-ddT00:00:00-0000');
                                else
                                    delete scope.application.criteria[afterName];

                                scope.application.search();
                            }
                        };

                        scope.beforeChange = function() {
                            if (scope.application.criteria[beforeName] != scope.before) {
                                if (scope.before != null)
                                    scope.application.criteria[beforeName] = $filter('date')(scope.before, 'yyyy-MM-ddT00:00:00-0000');
                                else
                                    delete scope.application.criteria[beforeName];

                                scope.application.search();
                            }
                        };

                        scope.today = function() {
                            scope.dt = new Date();
                        };

                        scope['show-weeks'] = false;
                        scope.toggleWeeks = function () {
                            scope.showWeeks = ! scope.showWeeks;
                        };

                        scope.clear = function () {
                            scope.dt = null;
                            scope.after = null;
                            scope.before = null;
                            scope.application.criteria[afterName] = null;
                            scope.application.criteria[beforeName] = null;
                        };

                        scope.toggleMin = function() {
                            scope.minDate = ( scope.minDate ) ? null : new Date();
                        };

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

                        scope.onAfterKeyUp = function(event) {
                            if (event.keyCode == 27 || event.keyCode == 13) {
                                event.preventDefault();
                                event.stopPropagation();
                                scope.clearAfterFilter();
                                if (scope.afterOpened && event.keyCode == 13) {
                                    scope.after = new Date();
                                    scope.application.criteria[afterName] = scope.after;
                                    scope.application.search();
                                }
                                scope.afterOpened = event.keyCode == 13;
                                return;
                            }
                        };

                        scope.onBeforeKeyUp = function(event) {
                            if (event.keyCode == 27 || event.keyCode == 13) {
                                event.preventDefault();
                                event.stopPropagation();
                                scope.clearBeforeFilter();

                                if (scope.beforeOpened && event.keyCode == 13) {
                                    scope.before = new Date();
                                    scope.application.criteria[beforeName] = scope.before;
                                    scope.application.search();
                                }

                                scope.beforeOpened = event.keyCode == 13;
                                return;
                            }
                        };

                        scope.clearAfterFilter = function() {
                            var didClear = false;

                            if (scope.application.criteria[afterName] != null) {
                                scope.after = null;
                                delete scope.application.criteria[afterName];
                                didClear = true;
                            }

                            if (didClear)
                                scope.application.search();
                        };

                        scope.clearBeforeFilter = function() {
                            var didClear = false;

                            if (scope.application.criteria[beforeName] != null) {
                                scope.before = null;
                                delete scope.application.criteria[beforeName];
                                didClear = true;
                            }

                            if (didClear)
                                scope.application.search();
                        };
                    },
                    template:
                        '<span class="form-group has-feedback wf-date-range">' +
                        '   <input data-ng-change="afterChange()" data-ng-keyup="onAfterKeyUp($event)" size="10" type="text" class="form-control wf-datepicker input-sm" datepicker-popup="MM/dd/yyyy" data-ng-model="after" datepicker-options="dateOptions"  is-open="afterOpened" min="afterMinDate" max="afterMaxDate" close-text="Close" placeholder="After" show-weeks="false"/>' +
                        '   <span data-ng-click="clearAfterFilter()" data-ng-show="after != null" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span> ' +
                        '</span>' +
                        '<span class="form-group has-feedback wf-date-range">' +
                        '   <input data-ng-change="beforeChange()" data-ng-keyup="onBeforeKeyUp($event)" size="10" type="text" class="form-control wf-datepicker input-sm" datepicker-popup="MM/dd/yyyy" data-ng-model="before" datepicker-options="dateOptions"  is-open="beforeOpened" min="beforeMinDate" max="beforeMaxDate" close-text="Close" placeholder="Before" show-weeks="false"/>' +
                        '   <span data-ng-click="clearBeforeFilter()" data-ng-show="before != null" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span> ' +
                        '</span>'
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
        .directive('wfFile', ['$http', '$sce', '$window', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wfUtils', 'wizardService',
            function($http, $sce, $window, attachmentService, dialogs, notificationService, taskService, wfUtils, wizardService) {
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
                        scope.duplicating = false;
                        scope.editing = false;
                        scope.files = [];
                    }],
                    link: function (scope, element, attr) {
                        scope.$on('wfEvent:form-loaded', function(event, form) {
                            scope.form = form;
                            if (form != null) {
                                var data = form.data;
                                var values = typeof(data) !== 'undefined' ? data[scope.name] : null;
                                scope.files = [];
                                angular.forEach(values, function(file) {
                                    file.detailed = false;
                                    scope.files.push(file);
                                });

                                scope.disabled = form.state !== 'open' && form.state !== 'assigned';
                            }
                        });
                        scope.cancelSendFile = function() {
                            this.data = null;
                            this.duplicating = false;
                        };
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
                        scope.sendFile = function() {
                            scope.sending = true;
                            if (scope.data != null)
                                scope.data.submit();

                            this.data = null;
                            this.duplicating = false;
                        };
                        scope.showDetails = function(file) {
                            file.detailed = !file.detailed;
                        };
                        scope.$on('wfEvent:invalid', function(event, validation) {
                            if (validation != null && validation[scope.name] != null) {
                                scope.error = validation[scope.name];
                            }
                        });
                        scope.$on('wfEvent:fileuploadstart', function(event, data) {
                            if (data.paramName == scope.name) {
                                scope.data = data;

                                var fileNameMap = {};
                                angular.forEach(scope.files, function(file) {
                                    fileNameMap[file.name] = file;
                                });

                                angular.forEach(data.files, function(file) {
                                    var duplicate = fileNameMap[file.name];
                                    if (duplicate != null) {
                                        scope.duplicating = true;
                                        scope.duplicateFileName = duplicate.name;
                                    }
                                });

                                if (!scope.duplicating)
                                    scope.sendFile();
                            }
                        });
                        scope.$on('wfEvent:fileuploadstop', function(event, data) {
                                scope.sending = false;
                        });
                    },
                    template:
                        '       <ul class="list-group"> ' +
                        '           <li data-ng-show="error" class="list-group-item list-group-item-danger">' +
                        '               <button type=\"button\" class=\"close\" type=\"button\" data-ng-click=\"error = null\" aria-hidden=\"true\">&times;</button>' +
                        '               <div>{{error}}</div>' +
                        '           </li>' +
                        '           <li data-ng-hide="files" class="list-group-item"><span class="text-muted">No documents</span></li>' +
                        '           <li data-ng-show="duplicating" class="list-group-item list-group-item-danger">' +
                        '               <div>Uploading {{duplicateFileName}} will permanently overwrite the existing version of this file. Are you sure?</div>' +
                        '               <div class="btn-toolbar pull-right">' +
                        '                   <button data-ng-click="sendFile()" class="btn btn-danger btn-xs" type="button">Yes, overwrite it</button>' +
                        '                   <button data-ng-click="cancelSendFile()" class="btn btn-default btn-xs" type="button">Cancel</button>' +
                        '               </div>' +
                        '               <div class="clearfix"></div>' +
                        '           </li>' +
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
                        '                   <a data-ng-href="{{file.link}}" data-ng-class="checkedOut ? \'text-danger\' : \'\'" class="wf-file-link"><i data-ng-hide="editing" class="fa fa-cloud-download"></i> {{file.name}}</a>&nbsp;&nbsp;&nbsp;' +
                        '                   <div data-ng-click="checkinFile(file)" data-ng-hide="editing || !checkedOut" class="btn btn-default btn-xs">' +
                        '                       <span class="fa-stack"><i class="fa fa-key fa-stack-1x"></i><i class="fa fa-ban fa-stack-2x text-danger"></i></span></div>' +
                        '                   <div data-ng-click="checkoutFile(file)" data-ng-hide="cannotCheckout || editing || checkedOut" class="btn btn-default btn-xs"><i class="fa fa-key"></i> Checkout</div>' +
                        '                   <i data-ng-click="deleteFile(file)" data-ng-show="editing" class=\"fa fa-times text-danger wf-delete-item pull-right\" title=\"Delete item\" style=\"font-size:14px;padding-top: 4px\"></i>' +
                        '               </div>' +
                        '               <div data-ng-show="file.detailed"><p/>' +
                        '                   <table class="table table-condensed">' +
                        '                       <thead><tr><th>Version</th><th>Upload date</th><th>Filer</th></tr></thead>' +
                        '                       <tbody>' +
                        '                           <tr data-ng-repeat="version in file.versions"><td><a data-ng-href="{{version.link}}" class="wf-file-link">{{version.label}}</a></td><td>{{version.createDate|date:\'medium\'}}</td><td>{{version.createdByUser.displayName}}</td></tr>' +
                        '                       </tbody>' +
                        '                   </table>' +
                        '                   <button data-ng-click="verifyDeleteFile(file)" data-ng-hide="disabled" class="btn btn-default btn-xs" role="button" type="button"><i class="fa fa-ban"></i> Delete</button>' +
                        '                   <div class="clearfix"></div>' +
                        '               </div>' +
                        '           </li>' +
                        '           <li data-ng-hide="disabled" class="list-group-item">' +
                        '               <p data-ng-show="checkedOut" class="pull-left">One or more files are checked out</p>' +
                        '               <div class="pull-left">' +
                        '                   <span data-wf-alert="{{name}}" class="text-danger"></span>' +
                        '               </div>' +
                        '               <div class="btn-toolbar pull-right">' +
                        '                   <div data-ng-class="checkedOut ? \'btn-danger\' : \'\'" class="btn btn-default btn-xs fileinput-button" role="button" type="button">' +
                        '                       <i ng-class="sending ? \'fa-spinner fa-spin\' : \'fa-cloud-upload\'" class="fa"></i> Add' +
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
        .directive('wfForm', ['$http', '$location', '$sce', '$window', 'formPageUri', 'formResourceUri', 'notificationService', 'wizardService',
            function($http, $location, $sce, $window, formPageUri, formResourceUri, notificationService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        form : '='
                    },
                    link: function (scope, element, attr) {
                        // Hide this form initially
                        scope.$root.$broadcast('wfEvent:start-loading');
                        scope.initialize = function(form) {
                            element.show();
                            scope.$root.$broadcast('wfEvent:stop-loading');
                            element.attr("action", form.action);
                            element.attr("method", "POST");
                            element.attr("enctype", "multipart/form-data");

                            if (form.state != null)
                                element.addClass('wf-state-' + form.state);

                            var created = form.task != null ? form.task.startTime : null;
                            element.attr('data-wf-task-started', created);
                        };
                        scope.$watch('form', function(modified, original) {
                            var form = modified;
                            if (form != null)
                                scope.initialize(form);
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
        .directive('wfSearchResponse', ['$filter', 'attachmentService', 'dialogs', 'localStorageService', 'notificationService', 'taskService', 'wizardService',
            function($filter, attachmentService, dialogs, localStorageService, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        application: '='
                    },
                    link: function (scope, element) {scope.doSort = function(facet) {
                        // If already sorting by this facet then switch the direction
                        if (scope.isSorting(facet)) {
                            if (facet.direction == 'desc')
                                facet.direction = 'asc';
                            else
                                facet.direction = 'desc';
                        } else {
                            facet.direction = 'desc';
                        }

                        scope.application.criteria.sortBy = [];
                        scope.application.criteria.sortBy.push(facet.name + ":" + facet.direction);
                        scope.application.search();
                    };
                        scope.isSorting = function(facet) {
                            if (facet == null)
                                return false;

                            var pattern = '^' + facet.name + ':';
                            var regex = new RegExp(pattern);
                            var isSorting = false;
                            angular.forEach(scope.application.criteria.sortBy, function(sortBy) {
                                isSorting = regex.test(sortBy);
                                if (isSorting)
                                    return true;
                            });
                            return isSorting;
                        };
                        scope.isSingleProcessSelected = function() {
                            return scope.application.criteria.processDefinitionKey != null && scope.application.criteria.processDefinitionKey != '';
                        };
                        scope.selectForm = function(form) {
                            form.checked = !form.checked;
                            if (!form.checked)
                                scope.application.state.selectAll = false;
                            scope.application.state.selectedForms = [];
                            angular.forEach(scope.application.forms, function(form) {
                                if (form.checked)
                                    scope.application.state.selectedForms.push(form);
                            });
                        };
                        scope.selectAllForms = function(checked) {
                            if (scope.application.forms != null) {
                                scope.application.state.selectAll = !scope.application.state.selectAll;
                                scope.application.state.selectedForms = [];
                                angular.forEach(scope.application.forms, function(form) {
                                    form.checked = scope.application.state.selectAll;
                                    if (form.checked)
                                        scope.application.state.selectedForms.push(form);
                                });
                            }
                        };

                    },
                    controller: ['$scope', function(scope) {
                        scope.dialogs = dialogs;
                    }],
                    template:
                        '       <div class="pull-right"><div class="wf-task-count">{{application.paging.total}} task{{application.paging.total != 1 ? \'s\' : \'\'}}</div></div>' +
//                        '       <h3 data-ng-bind="isSingleProcessSelected() ? application.processDefinitionDescription[application.criteria.processDefinitionKey] : \'\'" class="wf-search-header"></h3>' +
                        '       <div data-wf-form-toolbar data-application="application" class=""></div>' +
                        '       <div class="row"></div>' +
                        '       <table data-ng-hide="application.state.organizing" class="table table-hover">\n' +
                        '            <thead>\n' +
                        '            <tr>' +
                        '               <th><input data-ng-click="selectAllForms()" data-ng-checked="application.state.selectAll" type="checkbox" class="result-checkbox"/></th>\n' +
                        '               <th style="white-space:nowrap">' +
                        '                   <div data-wf-column-header data-application="application" data-facet="application.facetMap[\'processInstanceLabel\']"></div>' +
                        '               </th>' +
                        '               <th data-ng-class="facet.required ? \'\' : \'hidden-sm hidden-xs\'" data-ng-repeat="facet in application.state.selectedFacets" style="white-space:nowrap">' +
                        '                   <div data-wf-column-header data-application="application" data-facet="facet"></div>' +
                        '               </th>' +
                        '            </tr>\n' +
                        '            </thead>\n' +
                        '            <tbody>\n' +
                        '            <tr data-ng-repeat="form in application.forms">\n' +
                        '                <td><input data-ng-click="selectForm(form)" data-ng-checked="form.checked" type="checkbox" class="result-checkbox"/></td>\n' +
                        '                  <td><a href="{{form.link}}" target="_self" rel="external">{{form.processInstanceLabel}}</a></td>' +
                        '                  <td data-ng-class="facet.required ? \'\' : \'hidden-sm hidden-xs\'" data-ng-repeat="facet in application.state.selectedFacets">{{form[facet.name]}}</td>' +
                        '            </tr>\n' +
                        '            </tbody>' +
                        '           \n' +
                        '       </table>' +
                        '       <ul data-ng-show="application.paging.required" class="pagination pull-right"> ' +
                        '           <li><a data-ng-click="application.paging.previousPage()">&larr; Previous</a></li> ' +
                        '           <li data-ng-class="pageNumber == application.paging.pageNumber ? \'active\' : \'\'" data-ng-repeat="pageNumber in application.paging.pageNumbers"><a data-ng-click="application.paging.toPage(pageNumber)">{{pageNumber}}</a></li> ' +
                        '           <li><a data-ng-click="application.paging.nextPage()">Next &rarr;</a></li> ' +
                        '       </ul>'
                }
            }
        ])
        .directive('wfFormToolbar', ['dialogs', 'instanceService', 'notificationService', 'taskService', 'wizardService',
            function(dialogs, instanceService, notificationService, taskService, wizardService) {
                return {
                    restrict: 'AE',
                    scope: {
                        application: '='
                    },
                    controller: ['$scope', function(scope) {


                    }],
                    link: function (scope, element) {
                        scope.dialogs = dialogs;
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
                        scope.getFormsSelected = function(taskStatuses) {
                            var selectedForms = [];
                            var unlimited = typeof(taskStatuses) === 'undefined' || taskStatuses == null;
                            var acceptableTaskStatuses = {};
                            angular.forEach(taskStatuses, function(taskStatus) {
                                acceptableTaskStatuses[taskStatus] = true;
                            });
                            angular.forEach(scope.application.state.selectedForms, function(form) {
                                if (typeof(form) !== 'undefined' && form != null) {
                                    if (unlimited || acceptableTaskStatuses[form.taskStatus] != null)
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

                            return scope.application.state.selectedForms.length === 1;
                        };

                        scope.isFormSelected = function(taskStatuses) {
                            if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                                var selectedForms = scope.getFormsSelected(taskStatuses);
                                return selectedForms.length !== 0 && selectedForms.length === scope.application.state.selectedForms.length;
                            }

                            return scope.application.state.selectedForms.length !== 0;
                        };
                    },
                    template:
                        '<div class="btn-toolbar" >' +
                        '   <button data-ng-click="dialogs.openAssignModal(getFormsSelected([\'Open\']))" data-ng-disabled="!isFormSelected([\'Open\'])" class="btn btn-default navbar-btn incomplete-selected-result-btn" id="assign-dialog-button" title="Assign task" type="button"><i class="fa fa-user fa-white"></i> Assign</button>\n' +
                        '   <button data-ng-click="dialogs.openHistoryModal(getFormsSelected())" data-ng-disabled="!isSingleFormSelected()" data-ng-disabled="!isSingleFormSelected()" class="btn btn-default navbar-btn selected-result-btn" id="history-dialog-button" title="History" type="button"><i class="fa fa-calendar-o fa-white"></i> History</button>\n' +
                        '   <button data-ng-click="dialogs.openActivateModal(getFormsSelected([\'Suspended\']))" data-ng-show="isFormSelected([\'Suspended\'])" class="btn btn-default navbar-btn" id="activate-dialog-button" title="Activate process" type="button"><i class="fa fa-play fa-white"></i> Reactivate</button>\n' +
                        '   <button data-ng-click="dialogs.openSuspendModal(getFormsSelected([\'Open\']))" data-ng-show="isFormSelected([\'Open\'])" class="btn btn-default navbar-btn" id="suspend-dialog-button" title="Suspend process" type="button"><i class="fa fa-pause fa-white"></i> Suspend</button>\n' +
                        '   <button data-ng-click="dialogs.openCancelModal(getFormsSelected([\'Open\',\'Suspended\']))" data-ng-show="isFormSelected([\'Open\',\'Suspended\'])" class="btn btn-danger navbar-btn incomplete-selected-result-btn" id="delete-dialog-button" title="Cancel process" type="button"><i class="fa fa-trash-o fa-white"></i> Delete</button>\n' +
                        '   <button data-ng-click="dialogs.openRestartModal(getFormsSelected([\'Queued\',\'Cancelled\',\'Complete\']))" data-ng-show="isFormSelected([\'Queued\',\'Cancelled\',\'Complete\'])" class="btn btn-default navbar-btn" title="Restart process" type="button"><i class="fa fa-rotate-left"></i></button>\n' +
                        '   <span data-ng-if="application.bucketList.buckets.length > 0" class="dropdown">\n' +
                        '       <button class="btn btn-default navbar-btn dropdown-toggle" data-ng-disabled="!isFormSelected()" data-toggle="dropdown" data-target="new-form-dropdown" id="new-form-button" type="button" title="Assign to bucket"><i class="fa fa-tag"></i><b class="caret"></b> Bucket</button>\n' +
                        '           <ul id="new-form-dropdown" class="dropdown-menu scroll" role="menu" aria-labelledby="new-form-button">\n' +
                        '               <li class="presentation dropdown-header" role="menuitem">Change Bucket</li>\n' +
                        '               <li data-ng-repeat="bucket in application.bucketList.buckets" data-ng-click="changeBucket(getFormsSelected(), bucket)" class="presentation" role="menuitem"><a>{{bucket}}</a></li>\n' +
                        '           </ul>\n' +
                        '   </span>\n ' +
                        '</div>'
                }
            }
        ])
        .directive('wfSearchToolbar', ['$window', 'attachmentService', 'dialogs', 'localStorageService', 'notificationService', 'taskService', 'wizardService', 'instanceService',
            function($window, attachmentService, dialogs, localStorageService, notificationService, taskService, wizardService, instanceService) {
                return {
                    restrict: 'AE',
                    scope: {
                        application: '='
                    },
                    controller: ['$scope', function(scope) {


                    }],
                    link: function (scope, element) {
                        scope.clearFilters = function() {
                            var didClear = false;
                            angular.forEach(scope.application.facets, function(facet) {
                                if (facet.type == 'date' || facet.type == 'datetime') {
                                    var beforeName = facet.name + 'Before';
                                    var afterName = facet.name + 'After';

                                    if (scope.application.criteria[beforeName] != null) {
                                        delete scope.application.criteria[beforeName];
                                        didClear = true;
                                    }
                                    if (scope.application.criteria[afterName] != null) {
                                        delete scope.application.criteria[afterName];
                                        didClear = true;
                                    }
                                } else if (scope.application.criteria[facet.name] != null && scope.application.criteria[facet.name] != '') {
                                    delete scope.application.criteria[facet.name];
                                    didClear = true;
                                }
                            });

                            if (didClear)
                                scope.application.search();

                            scope.application.state.filtering = false;
                        };

                        scope.exportCsv = function() {
                            var url = "/workflow/ui/instance.xls?processDefinitionKey=" + scope.application.criteria.processDefinitionKey;
                            if (scope.application.criteria.lastModifiedAfter != null)
                                url += '&startedAfter=' + scope.application.criteria.lastModifiedAfter;
                            if (scope.application.criteria.lastModifiedBefore != null)
                                url += '&startedBefore=' + scope.application.criteria.lastModifiedBefore;
                            $window.location.href = url;
                        };

                        scope.getFormsSelected = function(taskStatuses) {
                            var selectedForms = [];
                            var acceptableTaskStatuses = new Object();
                            angular.forEach(taskStatuses, function(taskStatus) {
                                acceptableTaskStatuses[taskStatus] = true;
                            });
                            angular.forEach(scope.application.state.selectedForms, function(form) {
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

                            return scope.application.state.selectedForms.length === 1;
                        };

                        scope.isFormSelected = function(taskStatuses) {
                            if (typeof(taskStatuses) !== 'undefined' && taskStatuses != null) {
                                var selectedForms = scope.getFormsSelected(taskStatuses);
                                return selectedForms.length !== 0;
                            }

                            return scope.application.state.selectedForms.length !== 0;
                        };

                        scope.isSingleProcessSelected = function() {
                            return scope.application.criteria.processDefinitionKey != null && scope.application.criteria.processDefinitionKey != '';
                        };

                        scope.isSingleProcessSelectable = function() {
                            return typeof(scope.application.definitions) !== 'undefined' && scope.application.definitions.length == 1;
                        };

                        scope.onSearchKeyUp = function(event) {
                            if (event.keyCode == 27) {
                                scope.clearSearch();
                                return;
                            }
                            if (scope.searchTimeout != null)
                                clearTimeout(scope.searchTimeout);
                            scope.searchTimeout = setTimeout(scope.application.search, 300);
                        };

                        scope.refreshSearch = function() {
                            scope.application.search();
                        };
                        scope.clearSearch = function() {
                            scope.application.criteria.keyword = '';
                            scope.application.criteria.keywords = '';
                            scope.application.search();
                        };

                        scope.showReportPanel = function() {

                        };

                        scope.toggleColumns = function() {
                            dialogs.openColumnsModal(scope.application);
                        };

                        scope.toggleFilter = function() {
                            scope.application.state.filtering = !scope.application.state.filtering;

                            if (!scope.application.state.filtering) {
                                scope.clearFilters();
                            }
                        };

                        scope.toggleCollapse = function() {
                            scope.application.state.collapsed = !scope.application.state.collapsed;
                        };
                    },
                    template:
                        '<nav class="navbar navbar-default navbar-ex1-collapse" style="margin-bottom: 0px;border-radius: 0px">\n' +
                        '        <div class="navbar-header">\n' +
                        '            <button data-ng-click="toggleCollapse()" type="button" class="navbar-toggle">\n' +
                        '                <span class="sr-only">Toggle</span>\n' +
                        '                <span class="icon-bar"></span>\n' +
                        '                <span class="icon-bar"></span>\n' +
                        '                <span class="icon-bar"></span>\n' +
                        '            </button>\n' +
                        '        </div>\n' +
                        '        <div data-ng-class="application.state.collapsed ? \'\' : \'collapse\'" class="navbar-collapse navbar-ex1-collapse">\n' +
                        '            <div class="container">\n' +
                        '                <div class="row"><form class="navbar-form navbar-left form-inline" role="search">\n' +
                        '                    <div class="row">\n' +
                        '                       <div class="form-group has-feedback">\n' +
                        '                           <input data-ng-keyup="onSearchKeyUp($event)" style="width: 400px" title="Search by keyword" role="" class="form-control searchField" data-ng-model="application.criteria.keywords" placeholder="Search" id="keyword" type="text">\n' +
                        '                           <span data-ng-click="clearSearch()" data-ng-show="application.criteria.keywords" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span>\n' +
                        '                       </div>' +
                        '                       <button data-ng-click="refreshSearch()" class="btn btn-default navbar-btn" role="button" id="instanceSearchButton" title="Search" type="submit">&nbsp;&nbsp;<i data-ng-class="application.state.searching ? \'fa-spinner fa-spin\' : \'fa-search\'" id="searchIcon" class="fa fa-lg"></i>&nbsp;&nbsp;</button>\n' +
                        '                       <span data-ng-if="application.definitions" class="dropdown">\n' +
                        '                            <button class="btn btn-default navbar-btn dropdown-toggle" data-toggle="dropdown" data-target="new-form-dropdown" id="new-form-button" title="Start new process" type="button"><i class="fa fa-play-circle-o"></i> <b class="caret"></b></button>\n' +
                        '                            <ul id="new-form-dropdown" class="dropdown-menu" role="menu" aria-labelledby="new-form-button">\n' +
                        '                                <li data-ng-repeat="definition in application.definitions" class="presentation"><a role="menuitem" href="{{definition.link}}" target="_self">{{definition.processDefinitionLabel}}</a></li>\n' +
                        '                            </ul>\n' +
                        '                       </span>\n' +
                        '                    </div>\n' +
                        '                    <div class="row">\n' +
                        '                        <ul class="navbar-nav">\n' +
                        '                            <li ng-hide="isSingleProcessSelectable()">\n' +
                        '                                <div class="dropdown">\n' +
                        '                                    <a id="process-definition-button" class="btn btn-link btn-small dropdown-toggle" data-target="limit-dropdown" data-toggle="dropdown" role="button" type="button">\n' +
                        '                                        <span class="dropdown-toggle-text">{{application.processDefinitionDescription[application.criteria.processDefinitionKey]}}</span>\n' +
                        '                                        <b class="caret"></b>\n' +
                        '                                    </a>\n' +
                        '                                    <ul class="dropdown-menu form-inline" role="menu" aria-labelledby="process-definition-button">\n' +
                        '                                        <li role="presentation" class="dropdown-header">Processes</li>\n' +
                        '                                        <li data-ng-repeat="definition in application.definitions" role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processDefinitionKey" data-ng-true-value="{{definition.processDefinitionKey}}" role="menuitem" checked=""/> &nbsp;{{definition.processDefinitionLabel}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processDefinitionKey" data-ng-true-value="" role="menuitem"> &nbsp;{{application.processDefinitionDescription[\'\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                    </ul>\n' +
                        '                                </div>\n' +
                        '                            </li>\n' +
                        '                            <li>\n' +
                        '                                <div class="dropdown">\n' +
                        '                                    <a id="filter-button" class="btn btn-link btn-small dropdown-toggle" data-target="limit-dropdown" data-toggle="dropdown" role="button" type="button">\n' +
                        '                                        <span class="dropdown-toggle-text">{{application.processStatusDescription[application.criteria.processStatus]}}</span>\n' +
                        '                                        <b class="caret"></b>\n' +
                        '                                    </a>\n' +
                        '                                    <ul id="limit-dropdown" class="dropdown-menu form-inline" role="menu" aria-labelledby="filter-button">\n' +
                        '                                        <li role="presentation" class="dropdown-header">Process status</li>\n' +
                        '                                        <li role="presentation" class="disabled">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusOpen" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="open" role="menuitem" checked=""/> &nbsp;{{application.processStatusDescription[\'open\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusComplete" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="complete" role="menuitem"> &nbsp;{{application.processStatusDescription[\'complete\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusCancelled" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="cancelled" role="menuitem"> &nbsp;{{application.processStatusDescription[\'cancelled\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusSuspended" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="suspended" role="menuitem"> &nbsp;{{application.processStatusDescription[\'suspended\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation" class="disabled">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusQueued" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="queued" role="menuitem" checked=""/> &nbsp;{{application.processStatusDescription[\'queued\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                        <li role="presentation">\n' +
                        '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
                        '                                                <label class="checkbox">\n' +
                        '                                                    <input type="checkbox" id="statusAny" data-ng-change="refreshSearch()" data-ng-model="application.criteria.processStatus" data-ng-true-value="all" role="menuitem"> &nbsp;{{application.processStatusDescription[\'all\']}}\n' +
                        '                                                </label>\n' +
                        '                                            </div>\n' +
                        '                                        </li>\n' +
                        '                                    </ul>\n' +
                        '                                </div>\n' +
                        '                            </li>\n' +
                        '                        </ul>\n' +
                        '                    </div>\n' +
                        '                </form>\n' +
                        '                <div class="navbar-right btn-toolbar">\n' +
                        '                    <a data-ng-show="false && !isFormSelected()" href="report.html" rel="external" target="_self" class="btn btn-default navbar-btn" id="report-button"><i class="fa fa-bar-chart-o"></i></a>\n' +
                        '                    <button data-ng-click="exportCsv()" data-ng-disabled="isFormSelected()" data-ng-disabled="!isSingleProcessSelected()" class="btn btn-default navbar-btn" title="Export as xls" type="button"><i class="fa fa-download"></i> Export</button>\n' +
                        '                    <button data-ng-click="toggleColumns()" data-ng-disabled="isFormSelected()" class="btn btn-default navbar-btn" title="Select columns"><i class="fa fa-columns fa-1x"></i> Columns</button>' +
                        '                    <button data-ng-click="toggleFilter()" data-ng-disabled="isFormSelected()" class="btn btn-default navbar-btn" title="Toggle filter">' +
                        '                       <i data-ng-class="application.state.filtering ? \'fa-ban\' : \'fa-filter\'" class="fa"></i> Filter' +
                        '                    </button>' +
                        '                </div>\n' +
                        '            </div></div>\n' +
                        '        </div>\n' +
                        '    </nav>'
                }
            }
        ])
        .directive('wfStatus', ['$rootScope', '$window', 'notificationService', 'taskService', 'wfUtils', 'wizardService',
             function($rootScope, $window, notificationService, taskService, wfUtils, wizardService) {
                 return {
                     restrict: 'AE',
                     scope: {
                        form : '='
                     },
                     link: function (scope, element) {
                        wfUtils.attachForm(scope);

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
                     },
                     template:
                         '   <div class="container"><div class="row" data-ng-show="form.container.readonly" data-ng-switch="form.state">\n' +
                         '        <div data-ng-switch-when="assigned" class="alert alert-info">\n' +
                         '            <strong>This form is assigned to {{form.task.assignee ? form.task.assignee.displayName : \'Nobody\'}}</strong> - to take action, you will need to assign it to yourself.\n' +
                         '            <button data-ng-click="claim()" class="btn btn-default pull-right" type="button">Assign to me</button>\n' +
                         '            <div class="clearfix"></div>\n' +
                         '        </div>\n' +
                         '        <div data-ng-switch-when="unassigned" class="alert alert-info">\n' +
                         '            <strong>This form is not currently assigned</strong> - to modify it, you will need to assign it to yourself.\n' +
                         '            <button data-ng-click="claim()" class="btn btn-default pull-right" type="button">Assign to me</button>\n' +
                         '            <div class="clearfix"></div>\n' +
                         '        </div>\n' +
                         '        <div data-ng-switch-when="completed" class="alert alert-info"><strong>This form can no longer be modified</strong> - it was completed by {{form.task.assignee.displayName}} on {{form.task.endTime|date:\'MMM d, y H:mm\'}}</div>\n' +
                         '        <div data-ng-switch-when="suspended" class="alert alert-info"><strong>This form can no longer be modified</strong> - it has been suspended</div>\n' +
                         '        <div data-ng-switch-when="cancelled" class="alert alert-info"><strong>This form can no longer be modified</strong> - it has been cancelled</div>\n' +
                         '    </div>\n' +
                         '    <div data-ng-if="form.applicationStatusExplanation != null && form.applicationStatusExplanation != \'\'" class="row">\n' +
                         '        <div class="alert alert-danger">\n' +
                         '        <button type="button" class="close" data-ng-click="form.applicationStatusExplanation = null" aria-hidden="true">&times;</button>\n' +
                         '           {{form.applicationStatusExplanation}}\n' +
                         '    </div>\n' +
                         '    </div><div class="row"><div data-ng-if="form.explanation != null && form.explanation.message != null && form.explanation.message != \'\'" class="alert alert-danger">\n' +
                         '        <h4 data-ng-if="form.explanation.message">{{form.explanation.message}}</h4>\n' +
                         '        <p>{{form.explanation.messageDetail}}</p>\n' +
                         '    </div></div></div>'
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
                        scope.$watch('form', function(modified, original) {
                            var form = modified;
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
