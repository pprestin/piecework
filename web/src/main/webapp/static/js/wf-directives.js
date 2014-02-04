angular.module('wf.directives',
    ['ui.bootstrap', 'ui.bootstrap.alert', 'ui.bootstrap.modal', 'wf.services', 'wf.templates'])
    .directive('wfActive', [
        function() {
            return {
                restrict: 'A',
                scope: {

                },
                link: function (scope, element, attr) {
                    var flowElement = attr.wfActive;
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                        var task = typeof(form) !== 'undefined' ? form.task : null;
                        var active = (flowElement == 'start' && task == null) || (task != null && flowElement == task.taskDefinitionKey);
                        if (!active) {
                            element.attr('disabled', 'disabled');
                        }
                    });
                }
            }
        }
    ])
    .directive('wfAlert', [
        function() {
            return {
                restrict: 'A',
                scope: {

                },
                link: function (scope, element, attr) {
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                        var validation = form.validation;
                        var fieldName = attr.wfAlert;
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
                                }
                            });
                        }
//                        scope.$on('event:value-updated:' + fieldName, function(event, value) {
//                            element.text('');
//                        });
                    });
                }
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
                    scope.$on('event:attachments', function(event, attachments) {
                        console.log("wfAttachments caught an attachments event!");
                        scope.state.attachments = attachments;
                    });
                    scope.$on('event:form-loaded', function(event, form) {
                        console.log("wfAttachments attached form to its scope");
                        if (typeof(form) !== 'undefined') {
                            if (form.loadedBy == null)
                                form.loadedBy = [];
                            form.loadedBy.push('wfAttachments');
                            scope.form = form;
                        }
                    });
                    scope.$root.$on('event:view-attachments', function() {
                        console.log("wfAttachments toggling visibility of attachments");
                        if (typeof(scope.form) === 'undefined')
                            scope.form = $rootScope.form;
                        if (typeof(scope.form) !== 'undefined' && !scope.state.isViewingAttachments)
                            attachmentService.refreshAttachments(scope.form);
                        scope.state.isViewingAttachments = !scope.state.isViewingAttachments;
                        scope.$root.$broadcast('event:toggle-attachments', scope.state.isViewingAttachments);
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
                    scope.$root.$on('event:form-loaded', function(event, form) {
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
                    container : '=',
                },
                templateUrl: 'templates/container.html',
                link: function (scope, element) {
                    scope.$on('event:form-loaded', function(event, form) {
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
                    scope.$on('event:toggle-attachments', function(event, isViewingAttachments) {
                        scope.state.isViewingAttachments = isViewingAttachments;
                    });
                }
            }
        }
    ])
    .directive('wfElement', [
        function() {
            return {
                restrict: 'A',
                scope: {

                },
                link: function (scope, element, attr) {
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                        var data = form.data;
                        var fieldName = attr.wfElement;
                        var subFieldName = null;
                        var indexOfPeriod = fieldName.indexOf('.');
                        if (indexOfPeriod != -1) {
                            subFieldName = fieldName.substring(indexOfPeriod+1);
                            fieldName = fieldName.substring(0, indexOfPeriod);
                        }
                        var values = typeof(data) !== 'undefined' ? data[fieldName] : null;
                        if (values != null) {
                            var html = '';
                            var href = '';
                            angular.forEach(values, function(value) {
                                if (value != null) {
                                    var current;
                                    if (subFieldName != null)
                                        current = value[subFieldName];
                                    else
                                        current = value;

                                    html += typeof(current) === 'string' ? current : current.name;

                                    if (typeof(current) !== 'string')
                                        href = current.link;
                                }
                            });
                            if (html == '')
                                html = attr.wfPlaceholder;
                            if (href != '')
                                element.attr('href', href);

                            element.html(html);
                        }
                        scope.$on('event:value-updated:' + fieldName, function(event, value) {
                            var html = typeof(value) === 'string' ? value : value.name;
                            element.html(html);
                        });
                    });
                }
            }
        }
    ])
    .directive('wfField', ['personService',
        function(personService) {
            return {
//                require: '^ngModel',
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
    .directive('wfFieldForm', ['attachmentService', '$rootScope',
        function(attachmentService, $rootScope) {
            return {
                restrict: 'A',
                scope: {
                    form : '='
                },
                link: function (scope, element, attr) {
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                    });
                    var fieldName = attr.wfFieldForm;

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
                                scope.$root.$broadcast('event:attachments', data.list);
                           else
                                scope.$root.$broadcast('event:value-updated:' + fieldName, data);
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
    .directive('wfFileUpload', ['$sce', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
        function($sce, attachmentService, dialogs, notificationService, taskService, wizardService) {
            return {
                restrict: 'AE',
                scope: {

                },
                templateUrl: 'templates/fileupload.html',
                link: function (scope, element) {
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                    });
                    scope.fileUploadOptions = {
                        autoUpload: true
                    };
                    scope.getAttachmentUrl = function() {
                        if (typeof(scope.form) === 'undefined') {
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
                    scope.$on('fileuploadstart', function() {
                        scope.state.sending = true;
                    });
                    scope.$on('fileuploadstop', function() {
                        scope.state.sending = false;
                    });
                }
            }
        }
    ])
    .directive('wfForm', ['$http', '$location', '$sce', '$window', 'formPageUri', 'formResourceUri', 'wizardService',
        function($http, $location, $sce, $window, formPageUri, formResourceUri, wizardService) {
            return {
                restrict: 'AE',
                scope: {

                },
                link: function (scope, element, attr) {
                    if (typeof(scope.state) == 'undefined') {
                        scope.state = {};
                        scope.state.isCollapsed = false;
                        scope.state.isViewingAttachments = false;
                    }
                    scope.$on('event:toggle-attachments', function(event, isViewingAttachments) {
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
                            if (field.parent != null) {
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
                    scope.$root.$on('event:refresh', function(event, message) {
                        scope.$root.refreshing = true;
                        var link = formResourceUri;
                        var absUrl = $location.absUrl();
                        var indexOf = absUrl.indexOf('?');
                        var query = indexOf != -1 ? absUrl.substring(indexOf) : "";
                        var redirectUrl = formResourceUri + query;

                        var url = link;
                        if (typeof(link) === 'undefined' || link == '') {
                            if (indexOf != -1)
                                url = absUrl.substring(0, indexOf) + '.json' + query;
                            else
                                url = absUrl + '.json';
                        } else {
                            url += '.json' + query;
                        }

                        // FIXME: Reduce cost by customizing to only use on fields that have a mask
                        window.setTimeout(function() {
                            $(':input[data-inputmask][type="text"]').inputmask();
                            if (!scope.hasDatePickerSupport()) {
                                $(':input[data-inputmask][type="date"]').inputmask();
                            }
                        }, 500);

                        $http.get($sce.trustAsResourceUrl(url), { withCredentials: true })
                            .error(function(data, status, headers, config) {
//                                if (status == 0 || status == 302)
//                                    $window.location.href = redirectUrl;
                                alert("Failed to access workflow server - probably because of single-sign-on")
                            })
                            .success(function(form) {
                                element.attr("action", form.action);
                                element.attr("method", "POST");
                                element.attr("enctype", "multipart/form-data");

                                var created = form.task != null ? form.task.startTime : null;
                                element.attr('data-wf-task-started', created);

                                var data = form.data;
                                var validation = form.validation;
                                var formElement = element[0];
                                angular.forEach(formElement.elements, function(input) {
                                    if (input.attributes['data-wf-blank'])
                                        return;
                                    if (input.name != null) {
                                        var fieldName = input.name;
                                        var subFieldName = null;
                                        var indexOfPeriod = fieldName.indexOf('.');
                                        if (indexOfPeriod != -1) {
                                            subFieldName = fieldName.substring(indexOfPeriod+1);
                                            fieldName = fieldName.substring(0, indexOfPeriod);
                                        }
                                        if (input.type == 'file') {
                                            var $input = $(input);
                                            if ($input.attr('data-auto-upload')) {
                                                $input.unbind('change');
                                                $input.on('change', function(event) {
                                                   var files = $(event.target)[0].files;
                                                   var data = new FormData();
                                                   var descriptionSelector = ':input.process-variable-description[data-element="' + input.name + '"]';
                                                   var description = $(descriptionSelector).val();
                                                   if (typeof(description) !== 'undefined')
                                                       data.append(input.name + '!description', description);

                                                   $.each(files, function(i, file) {
                                                       data.append(input.name, file);
                                                   });

                                                   var url = form.attachment;
                                                   var indexOf = url.indexOf('/attachment');
                                                   if (indexOf != -1)
                                                        url = url.substring(0, indexOf) + '/value/' + input.name;
                                                   $.ajax({
                                                       url : url,
                                                       data : data,
                                                       processData : false,
                                                       contentType : false,
                                                       type : 'POST'
                                                   })
                                                   .done(function(data, textStatus, jqXHR) {
                                                       $(descriptionSelector).val('');
                                                       scope.$root.$broadcast('event:value-updated:' + input.name, data);
                                                   })
                                                   .fail(function(jqXHR, textStatus, errorThrown) {
                                                       var data = $.parseJSON(jqXHR.responseText);
                                                       var selector = '.process-alert[data-element="' + input.name + '"]';
                                                       var message = data.messageDetail;
                                                       var $alert = $(selector);
                                                       $alert.show();
                                                       $alert.text(message);
                                                   });

                                                   return false;
                                                });
                                            }
                                        }
                                        var values = typeof(data) !== 'undefined' ? data[fieldName] : null;
                                        if (values != null) {
                                            angular.forEach(values, function(value) {
                                                if (value != null) {
                                                    if (input.type == 'checkbox' || input.type == 'radio') {
                                                        if (input.value == value)
                                                            input.checked = true;
                                                    } else if (input.type == 'select') {
                                                        angular.forEach(input.options, function(option) {
                                                           if (option.value == value)
                                                                option.selected = true;
                                                        });
                                                    } else if (input.type == 'file') {

                                                    } else {
                                                        var current;
                                                        if (subFieldName != null)
                                                            current = value[subFieldName];
                                                        else
                                                            current = value;

                                                        input.value = typeof(current) === 'string' ? current : current.name;
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                                var data = form.data;
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
                                        form.layout = 'normal';
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

                                scope.$root.form = form;
                                scope.$root.$broadcast('event:form-loaded', form);
                                scope.$root.$broadcast('event:step-changed', form.activeStepOrdinal);
                            });
                    });

                    scope.$root.$broadcast('event:refresh', 'form');
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
                    scope.$root.$on('event:form-loaded', function(event, form) {
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
                                    scope.$on('event:value-updated:' + fieldName, function(event, value) {
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
    .directive('wfList', ['attachmentService', 'dateFilter',
        function(attachmentService, dateFilter) {
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
                                $.ajax({
                                    url : realValue.link,
                                    type : 'DELETE',
                                    success: function() {
                                        $listItem.remove();
                                        if ($listElement.find('li').length == 0)
                                            $fallbackHtml.show();
                                    }
                                });
                            });

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

                        listElement.empty();

                        scope.$on('event:value-updated:' + fieldName, function(event, updatedValue) {
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
                    scope.$root.$on('event:form-loaded', function(event, form) {
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

                        var childHtml = element.find('li');
                        var fallbackHtml = element.find('[data-wf-fallback]');

                        if (fieldName == 'attachments') {
                            scope.$on('event:attachments', function(event, attachments) {
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
                    scope.$on('event:form-loaded', function(event, form) {
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
                     scope.$root.$on('event:toggle-attachments', function(event, isViewingAttachments) {
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
                     scope.$root.$on('event:toggle-attachments', function(event, isViewingAttachments) {
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
                 templateUrl: 'templates/notifications.html',
                 link: function (scope, element) {
                    scope.$root.$on('event:notification', function(event, notification) {
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
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                    });
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
                 },
                 templateUrl: 'templates/review.html',
                 link: function (scope, element) {
                    if (typeof(scope.state) == 'undefined') {
                        scope.state = {};
                        scope.state.isViewingAttachments = false;
                    }
                    scope.$root.$on('event:toggle-attachments', function(event, isViewingAttachments) {
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
                    form : '='
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

                    scope.$root.$on('event:step-changed', function(event, ordinal) {
                        if (attr.wfScreen == 'confirmation' && typeof(ordinal) === 'undefined')
                            return;
                        element.removeClass('ng-hide');

                        if (upwards) {
                            if (typeof(ordinal) === 'undefined' || step > ordinal)
                                element.addClass('ng-hide');
                        } else if (step != ordinal) {
                            element.addClass('ng-hide');
                        }
                    });
                }
            }
        }
    ])
    .directive('wfSearchbar', ['$window', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
        function($window, attachmentService, dialogs, notificationService, taskService, wizardService) {
            return {
                restrict: 'AE',
                scope: {

                },
                templateUrl: 'templates/searchbar.html',
                link: function (scope, element) {
                    if (typeof(scope.selectedFormMap) === 'undefined')
                        scope.selectedFormMap = {};
                    scope.context = $window.piecework.context;
                    scope.state = new Object();
                    scope.state.isCollapsed = false;
                    scope.state.toggleCollapse = function() {
                        scope.state.isCollapsed = !scope.state.isCollapsed;
                    };

                    scope.dates = new Object();
                    scope.dates.selectedDateRangeKey = 'any';
                    scope.dates.dateRangeKeys = ['any', '1-hour', '1-day', '1-week', '1-month', '1-year', 'custom'];
                    scope.dates.dateRanges = {
                        'any' : 'Any date',
                        '1-hour' : 'Past 1 hour',
                        '1-day' : 'Past 1 day',
                        '1-week' : 'Past 1 week',
                        '1-month' : 'Past 1 month',
                        '1-year' : 'Past 1 year',
                        'custom' : 'Custom date range'
                    };
                    scope.dates.isNonCustomDateRange = function() {
                        return scope.dates.selectedDateRangeKey != 'custom';
                    };
                    scope.dates.refreshCustomDate = function() {
                        scope.criteria.startedAfter = scope.dates.customStartedAfter;
                        scope.criteria.startedBefore = scope.dates.customStartedBefore;
                        scope.refreshSearch();
                    };
                    scope.dates.selectDateRange = function(dateRangeKey) {
                        scope.dates.selectedDateRangeKey = dateRangeKey;
                        scope.criteria.startedAfter = null;
                        scope.criteria.startedBefore = null;
                        if (dateRangeKey == '1-hour') {
                            scope.criteria.startedAfter = moment().subtract('hours', 1).toISOString();
                        } else if (dateRangeKey == '1-day') {
                            scope.criteria.startedAfter = moment().subtract('days', 1).toISOString();
                        } else if (dateRangeKey == '1-week') {
                            scope.criteria.startedAfter = moment().subtract('weeks', 1).toISOString();
                        } else if (dateRangeKey == '1-month') {
                            scope.criteria.startedAfter = moment().subtract('months', 1).toISOString();
                        } else if (dateRangeKey == '1-year') {
                            scope.criteria.startedAfter = moment().subtract('years', 1).toISOString();
                        } else if (dateRangeKey == 'custom') {
                            scope.dates.customStartedAfter = moment().subtract('years', 1).format('YYYY-MM-DDTHH:mm:ss.00');
                            scope.dates.customStartedBefore = moment().format('YYYY-MM-DDTHH:mm:ss.00');
                        }
                        if (dateRangeKey != 'custom')
                            scope.refreshSearch();
                    };
                    scope.dates.showNonCustomDateRange = function() {
                        var selectedKey = scope.dates.selectedDateRangeKey;
                        return scope.dates.dateRanges[selectedKey];
                    };
                    scope.criteria = new Object();
                    scope.criteria.processDefinitionKey = '';
                    scope.criteria.processStatus = 'open';
                    scope.criteria.taskStatus = 'all';

                    scope.exportCsv = function(selectedForms) {
                        $window.location.href = "/workflow/ui/instance.csv?processDefinitionKey=" + scope.criteria.processDefinitionKey;
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

                    scope.getFormsSelected = function(taskStatuses) {
                        var formIds = Object.keys(scope.selectedFormMap);
                        var selectedForms = new Array();
                        var acceptableTaskStatuses = new Object();
                        angular.forEach(taskStatuses, function(taskStatus) {
                            acceptableTaskStatuses[taskStatus] = true;
                        });
                        angular.forEach(formIds, function(formId) {
                            var form = scope.selectedFormMap[formId];
                            if (typeof(form) !== 'undefined' && form != null && form.task != null) {
                                if (typeof(taskStatuses) === 'undefined' || taskStatuses == null ||
                                    acceptableTaskStatuses[form.task.taskStatus] != null)
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
                        return typeof(scope.definitions) !== 'undefined' && scope.definitions.length == 1;
                    };

                    scope.$on('event:change-selection', function(event, selectedFormMap) {
                        scope.selectedFormMap = selectedFormMap;
                    });

                    scope.$on('event:found', function(event, results) {
                        scope.searching = false;
                        scope.definitions = results.definitions;
                        scope.selectedFormMap = {};
                        scope.$root.currentUser = results.currentUser;
                        scope.processDefinitionDescription = new Object();
                        angular.forEach(results.definitions, function(definition) {
                            scope.processDefinitionDescription[definition.task.processDefinitionKey] = definition.task.processDefinitionLabel;
                        });
                        if (results.definitions != null && results.definitions.length == 1)
                            scope.criteria.processDefinitionKey = results.definitions[0].task.processDefinitionKey;
                        scope.processDefinitionDescription[''] = 'Any process';
                    });
                    scope.$on('event:search', function(event, criteria) {
                        scope.searching = true;
                    });

                    scope.dialogs = dialogs;

                    scope.refreshSearch = function() {
                        scope.$root.$broadcast('event:search', scope.criteria);
                    };

                    scope.model = $window.piecework.model;
                    if (typeof(scope.model) !== 'undefined' && typeof(scope.model.total) !== 'undefined') {
                        scope.$on('event:results-linked', function(event) {
                           scope.$root.$broadcast('event:found', scope.model);
                           delete $window.piecework['model'];
                        });
                    } else {
                        scope.$root.$broadcast('event:search', scope.criteria);
                    }
                }
            }
        }
    ])
    .directive('wfSearchresults', ['$resource', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
        function($resource, attachmentService, dialogs, notificationService, taskService, wizardService) {
            return {
                restrict: 'AE',
                scope: {

                },
                templateUrl: 'templates/searchresults.html',
                link: function (scope, element) {
                    if (typeof(scope.criteria) === 'undefined')
                        scope.criteria = {};

                    scope.isSingleProcessSelected = function() {
                        return scope.criteria.processDefinitionKey != null && scope.criteria.processDefinitionKey != '';
                    };
                    scope.processSearchResults = function(results) {
                        scope.$root.$broadcast('event:found', results);
                    };
                    scope.selectForm = function(form) {
                        if (scope.selectedFormMap[form.formInstanceId] == null)
                            scope.selectedFormMap[form.formInstanceId] = form;
                        else
                            delete scope.selectedFormMap[form.formInstanceId];

                        scope.$root.$broadcast('event:change-selection', scope.selectedFormMap);
                    };

                    var SearchResults = $resource('./form', {processStatus:'@processStatus'});
                    scope.$on('event:found', function(event, results) {
                        scope.forms = results.list;
                        scope.selectedFormMap = new Object();
                    });
                    scope.$on('event:search', function(event, criteria) {
                        if (typeof(criteria) !== 'undefined')
                            scope.criteria = criteria;
                        SearchResults.get(scope.criteria, scope.processSearchResults);
                    });

                    scope.$root.$broadcast('event:results-linked');
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

                    scope.$on('event:form-loaded', function(event, form) {
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
                            $rootScope.$broadcast('event:refresh', 'assignment');
                        };

                        var failure = function(scope, data, status, headers, config, form, assignee) {
                            form._assignmentStatus = 'error';
                            var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                            var message = form.task.processInstanceLabel + ' cannot be assigned ';
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
    .directive('wfToolbar', ['$sce', 'attachmentService', 'dialogs', 'notificationService', 'taskService', 'wizardService',
        function($sce, attachmentService, dialogs, notificationService, taskService, wizardService) {
            return {
                restrict: 'AE',
                scope: {

                },
                templateUrl: 'templates/form-navbar.html',
                link: function (scope, element) {
                    if (typeof(scope.state) == 'undefined') {
                        scope.state = {};
                        scope.state.isCollapsed = false;
                        scope.state.isViewingAttachments = false;
                    }
                    scope.$root.$on('event:toggle-attachments', function(event, isViewingAttachments) {
                        scope.state.isViewingAttachments = isViewingAttachments;
                    });
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                    });
                    scope.dialogs = dialogs;
                    scope.wizard = wizardService;
                    scope.assignTo = function(userId) {
                         var success = function(scope, data, status, headers, config, form, assignee) {
                             scope.$root.$broadcast('event:refresh', 'assignment');
                         };

                         var failure = function(scope, data, status, headers, config, form, assignee) {
                             form._assignmentStatus = 'error';
                             var displayName = typeof(assignee.displayName) === 'undefined' ? assignee : assignee.displayName;
                             var message = '<em>' + form.task.processInstanceLabel + '</em> cannot be assigned to <em>' + displayName + '</em>';
                             var title = data.messageDetail;
                             notificationService.notify(scope.$root, message, title);
                         };
                         taskService.assignTask(scope, scope.form, userId, success, failure);
                    };
                    scope.fileUploadOptions = {
                        autoUpload: true,
                        xhrFields: {
                            withCredentials: true
                        }
                    };
                    scope.getAttachmentUrl = function() {
                        if (typeof(scope.form) === 'undefined') {
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
                    scope.$on('fileuploadstart', function() {
                        scope.state.sending = true;
                    });
                    scope.$on('fileuploadstop', function() {
                        scope.state.sending = false;
                    });
                    scope.toggleCollapse = function() {
                        scope.state.isCollapsed = !scope.state.isCollapsed;
                    };
                    scope.viewAttachments = function() {
                        scope.$root.$broadcast('event:view-attachments');
                    };

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
                    var flowElement = attr.wfActive;
                    scope.$root.$on('event:form-loaded', function(event, form) {
                        scope.form = form;
                        var task = typeof(form) !== 'undefined' ? form.task : null;
                        var active = (flowElement == 'start' && task == null) || (task != null && flowElement == task.taskDefinitionKey);
                        if (!active) {
                            element.addClass('ng-hide')
                        }
                    });
                }
            }
        }
    ]);

