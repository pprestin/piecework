angular.module('wf',
    [
        'ngRoute',
        'ngSanitize',
        'blueimp.fileupload',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'wf.directives',
        'wf.services'
    ])
    .config(['$httpProvider', '$routeProvider', '$locationProvider', '$logProvider','$provide','$sceDelegateProvider',
        function($httpProvider, $routeProvider, $locationProvider, $logProvider, $provide, $sceDelegateProvider) {
            {{DYNAMIC_CONFIGURATION}}
        }
    ])
    .controller('FormController', ['$http', '$location', '$log', '$sce', '$scope', '$window', 'fileUpload', 'formPageUri', 'formResourceUri', 'wizardService',
        function($http, $location, $log, $sce, scope, $window, fileUpload, formPageUri, formResourceUri, wizardService) {

            scope.isFormControllerScope = true;

            scope.$root.$broadcast('wfEvent:start-loading');
            if (typeof(scope.state) == 'undefined') {
                scope.state = {};
                scope.state.isCollapsed = false;
                scope.state.isViewingAttachments = false;
            }

            scope.$on('wfEvent:toggle-attachments', function(event, isViewingAttachments) {
                scope.state.isViewingAttachments = isViewingAttachments;
                scope.$root.isViewingAttachments = isViewingAttachments;
            });

            scope.addFields = function(fields, form, container, isRoot) {
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
                    scope.addFields(fields, form, child, false);
                });
            };

            scope.handleField = function(form, data, validation, field, readonly) {
                var values = data[field.name];
                field.optionMap = new Object();
                if (field.options != null) {
                    angular.forEach(field.options, function(option) {
                        if (option.value != null) {
                            field.optionMap[option.value] = option;
                        }
                    });
                }
                if (values != null && values.length > 0) {
                    if (values.length == 1) {
                        var value = values[0];
                        if (value != null) {
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
                }
                field.values = new Array(field.maxInputs);
                if (values != null) {
                    for (var i=0;i<values.length;i++) {
                        var value = values[i];
                        field.values[i] = value;
                    }
                }

                if (typeof(validation) !== 'undefined' && validation[field.name] != null) {
                    field.messages = validation[field.name];
                    field.cssClass = "has-error";
                    if (field.parent != null && (form.layout == 'review' || field.parent.ordinal < form.activeStepOrdinal)) {
                        form.activeStepOrdinal = field.parent.ordinal;
                        field.parent.breadcrumbCssClass = "invalid";
                    }
                } if (readonly)
                    field.editable = false;
            };
            scope.markLeaves = function(container) {
                if (container.children != null && container.children.length > 1) {
                    angular.forEach(container.children, function(child) {
                        scope.markLeaves(child);
                        child.parent = container;
                    });
                } else {
                    container.leaf = true;
                }
            };
            scope.wizard = wizardService;
            scope.hasDatePickerSupport = function() {
                var elem = document.createElement('input');
                elem.setAttribute('type','date');
                elem.value = 'foo';
                return (elem.type == 'date' && elem.value != 'foo');
            }
            scope.queryParams = function() {
                var urlParams = {};
                (function () {
                    var e,
                        a = /\+/g,  // Regex for replacing addition symbol with a space
                        r = /([^&;=]+)=?([^&;]*)/g,
                        d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
                        q = $window.location.search.substring(1);

                    while (e = r.exec(q))
                        urlParams[d(e[1])] = d(e[2]);
                })();
                return urlParams;
            };
            scope.doRedirect = function(formResourceUri, queryParams) {
                var redirectUrl = formResourceUri;
                var redirectCount = 1;
                var separator = '?';
                angular.forEach(queryParams, function(value, name) {
                    if (name == 'redirectCount') {
                        value = parseInt(value) + 1;
                        redirectCount = value;
                    } else {
                        redirectUrl += separator + name + '=' + value;
                    }
                    separator = '&';
                });
                redirectUrl = redirectUrl + separator + 'redirectCount=' + redirectCount;
                if (redirectCount < 2)
                    $window.location.href = redirectUrl;
                else
                    scope.$root.$broadcast('wfEvent:fallback');
            };
            scope.reloadForm = function(form) {
                var data = form.data;
                var validation = form.validation;
                var readonly = form.container != null ? form.container.readonly : false;
                var rootContainer = form.container;
                var fields = new Array();
                if (rootContainer != null) {
                    scope.markLeaves(rootContainer);
                    if (rootContainer.children != null && rootContainer.children.length > 1 && rootContainer.activeChildIndex != -1) {
                        form.steps = rootContainer.children;
                        form.activeStepOrdinal = rootContainer.activeChildIndex;
                        scope.addFields(fields, form, rootContainer, true);
                    } else {
                        fields = form.container != null ? form.container.fields : [];
                    }
                }
                form.fieldMap = new Object();
                angular.forEach(fields, function(field) {
                    form.fieldMap[field.name] = field;
                    scope.handleField(form, data, validation, field, readonly);
                });

                if (form.task != null) {
                    if (form.task.active) {
                        if (form.task.assignee != null)
                            form.state = 'assigned';
                        else
                            form.state = 'unassigned';
                    } else if (form.task.taskStatus == 'Suspended') {
                        form.state = 'suspended';
                    } else if (form.task.taskStatus == 'Cancelled') {
                        form.state = 'cancelled';
                    } else if (form.task.taskStatus == 'Complete' && (form.task.assignee == null || form.task.assignee.userId != form.currentUser.userId)) {
                        form.state = 'completed';
                    }
                }

                scope.form = form;
                scope.$root.$broadcast('wfEvent:form-loaded', form);

                if (typeof(form.activeStepOrdinal) !== 'undefined')
                    scope.$root.$broadcast('wfEvent:step-changed', form.activeStepOrdinal);
            };

            scope.$root.$on('wfEvent:refresh', function(event, message) {
                scope.$root.refreshing = true;
                var link = formResourceUri;
                var absUrl = $location.absUrl();
                var indexOf = absUrl.indexOf('?');
                var query = indexOf != -1 ? absUrl.substring(indexOf) : "";
                var queryParams = scope.queryParams();

                var url = link;
                if (typeof(link) === 'undefined' || link == '') {
                    if (indexOf != -1)
                        url = absUrl.substring(0, indexOf) + '.json' + query;
                    else
                        url = absUrl + '.json';
                } else {
                    url += '.json' + query;
                }

                $http.get($sce.trustAsResourceUrl(url), { withCredentials: true })
                    .error(function(data, status, headers, config) {
                        if (status == 0 || status == 302) {
                            $window.setTimeout(function() {
                                scope.$root.$broadcast('wfEvent:stop-loading');
                                scope.doRedirect(formResourceUri, queryParams);
                            }, 3000);
                        }
                    })
                    .success(scope.reloadForm);
            });

            scope.$root.$broadcast('wfEvent:refresh', 'form');


            scope.uploadOptions = {
                autoUpload: false,
                dataType: 'json',
                formData: {},
                sequentialUploads: true,
                fileInput: $('input:file'),
                xhrFields: {
                    withCredentials: true
                }
            };
            scope.$on('fileuploadadd', function(event, data) {
                if (data.fileInput.context.id == null || data.fileInput.context.id !== 'attachmentFile') {
                    $log.debug(data);

                    if (data.files != null && data.files.length > 0) {
                        scope.$root.$broadcast('wfEvent:fileuploadstart', data);
                    }
                }
            })
            scope.$on('fileuploaddone', function(event, data) {
                var form = data.result;
                if (form != null && form.data != null)
                    scope.reloadForm(form);
            });
            scope.$on('fileuploadfail', function(event, data) {
                var message = angular.fromJson(data.jqXHR.responseText);
                if (message != null && message.items != null) {
                    var validation = {};
                    angular.forEach(message.items, function(item) {
                        validation[item.propertyName] = item.message;
                    });
                    scope.$root.$broadcast('wfEvent:invalid', validation);
                }
//                        notificationService.notify(scope.$root, message.messageDetail);
            });
            scope.$on('fileuploadstart', function(event, data) {

                scope.state.sending = true;
            });
            scope.$on('fileuploadstop', function(event, data) {
                scope.$root.$broadcast('wfEvent:fileuploadstop', data);
                scope.state.sending = false;
            });
        }
    ])
    .directive('wfKeypressEvents', ['$document', '$rootScope',
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
              var displayName = matchItem.displayName != null ? matchItem.displayName : matchItem;
              return query ? displayName.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : displayName;
          };

    })
    .value('hostUri', '{{HOST_URI}}')
    .value('formResourceUri', '{{FORM_RESOURCE_URI}}')
    .value('formPageUri', '{{FORM_PAGE_URI}}');