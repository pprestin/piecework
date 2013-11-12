/*!
 * script.js 0.1.0
 * https://github.com/piecework/piecework
 * Copyright 2013 University of Washington; Licensed Educational Community License 2.0
 */
(function($) {
    var VERSION = "0.1.0";
    var utils = {
        decorateFieldForm: function(field) {
            var fieldFormSelector = 'form.process-variable[data-element="' + field.name + '"]';
            var $fieldForm = $(fieldFormSelector);
            if ($fieldForm.length > 0) {
                $fieldForm.attr('action', field.link);
                $fieldForm.attr('method', 'POST');
                $fieldForm.attr('enctype', 'multipart/form-data');
                utils.includeVariableButtons(field, $fieldForm);
                utils.includeVariableItems(field, $fieldForm);
            }
        },
        decorateForms: function(model) {
            var utils = this;
            $('form').each(function(index, element) {
                $form = $(element);
                var isExcluded = $form.hasClass('process-exclude-form');
                if (isExcluded)
                    return;

                var isAttachmentForm = $form.hasClass('process-attachment-form');
                var isCancellationForm = $form.hasClass("process-cancellation-form");
                var isVariableForm = $form.hasClass('process-variable');

                if (isAttachmentForm) {
                    utils.$attachmentForm = $form;
                    $form.attr('action', model.attachment);
                    $form.attr('method', 'POST');
                    $form.submit(utils.submitAttachment);
                } else if (isCancellationForm) {
                    $form.attr('action', model.cancellation);
                    $form.attr('method', 'POST');
                } else if (!isVariableForm) {
                    $form.attr('action', model.action);
                    $form.attr('method', 'POST');
                    $form.attr('enctype', 'multipart/form-data');
//                    $form.submit(utils.submitMain);
                }
            })
        },
        decorateContainer: function(container, data, validation, $arg_inputs, $arg_variables) {
            var $inputs = typeof($arg_inputs) == 'undefined' ? $(':input') : $arg_inputs;
            var $variables = typeof($arg_variables) == 'undefined' ? $('.process-variable') : $arg_variables;
            if (typeof(container) != 'undefined') {
                var children = container.children;
                var fields = container.fields;
                if (typeof(children) != 'undefined') {
                    for (var i=0;i<children.length;i++) {
                        this.decorateContainer(children[i], data, validation, $inputs, $variables);
                    }
                }
                if (typeof(fields) != 'undefined') {
                    for (var i=0;i<fields.length;i++) {
                        this.decorateField(fields[i], data, validation, $inputs, $variables);
                    }
                }
            }
        },
        decorateField: function(field, data, validation, $inputs, $variables) {
            var name = field.name;
            var type = field.type;
            var values = data[name];
            var messages = validation != null ? validation[name] : null;
            var inputSelector = '[name="' + name + '"]';
            var variableSelector = '[data-element="' + name + '"]';
            var alertSelector = '.process-alert[data-element="' + name + '"]';
            var $input = $inputs.filter(inputSelector);
            var $variable = $variables.filter(variableSelector);
            var $alert = $(alertSelector);

            if (type == 'person') {
                this.decoratePersonFields(field, data, $inputs, $variables);
            } else if (type == 'file') {
                this.decorateFieldForm(field);

                var $images = $variable.filter('img');
                var $others = $variable.filter(':not(img,form)');
                if (values != null && values.length > 0) {
                    var first = values[0];
                    if (typeof first === 'object') {
                        $images.attr('src', first.link);
                        $others.text(first.name);
                    }
                }
            } else {
                var isEmpty = true;
                for (var x=0;x<values.length;x++) {
                    if (values[x] != null && typeof values[x] == 'object' || values[x] != '') {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty) {
                    if ($input.length > 0 && type != 'file')
                        $input.val(values);

                    if ($variable.length > 0 && !$variable.is('form'))
                        $variable.text(values);
                }
            }

            if (messages != null && messages.length > 0) {
                var messageText = messages[0].text;
                $alert.show();
                $alert.text(messageText);
            }

            $input.on('change', function() {
                $alert.hide();
            });
        },
        decoratePersonFields: function(field, data, $inputs, $variables) {
            var name = field.name;
            var type = field.type;
            var values = data[name];
            var subfields = ['displayName', 'visibleId'];
            if (values != null && values.length > 0) {
                for (var s=0;s<subfields.length;s++) {
                    var subfield = subfields[s];
                    var subfieldName = name + '.' + subfield;
                    var subfieldInputSelector = '[name="' + subfieldName + '"]';
                    var subfieldVariableSelector = '[data-element="' + subfieldName + '"]';

                    var subfieldValues = values[0][subfield];
                    var $subfieldInputs = $inputs.filter(subfieldInputSelector);
                    var $subfieldVariables = $variables.filter(subfieldVariableSelector);
                    $inputs.filter(subfieldInputSelector).val(subfieldValues);
                    $variables.filter(subfieldVariableSelector).text(subfieldValues);
                }
            }
        },
        deleteAttachment: function(event) {
            var $target = $(event.target);
            var $attachment = $target.closest('li');
            var $anchor = $attachment.find('.process-attachment-link');
            var attachmentUrl = $anchor.attr('href') + ".json";
            $.ajax({
                url : attachmentUrl,
                type : 'DELETE',
                success: function() {
                    var $attachmentList = $attachment.closest('ul');
                    $attachment.remove();
                    if ($attachmentList.find('li').length == 0)
                        utils.$attachmentFallback.show();
                }
            });
        },
        deleteVariableItem: function(event) {
            var $target = $(event.target);
            var $attachment = $target.closest('li');
            var $anchor = $attachment.find('.process-variable-link');
            var attachmentUrl = $anchor.attr('href') + ".json";
            $.ajax({
                url : attachmentUrl,
                type : 'DELETE',
                success: function() {
                    var $attachmentList = $attachment.closest('ul');
                    $attachment.remove();
                }
            });
        },
        includeAttachments: function(model) {
            var $attachmentList = $('ul.process-attachment-list');
            utils.$attachmentFallback = $('.process-attachment-fallback');
            if ($attachmentList.length > 0) {
                if (utils.$liTemplate == undefined) {
                    utils.$liTemplate = $attachmentList.find('li:first');
                    utils.$liTemplate.remove();
                }

                var $newAttachmentList = $attachmentList.clone();
                $newAttachmentList.empty();
                var attachmentUrl = utils.model.attachment + ".json";
                $.get(attachmentUrl, function(data) {
                    if (data.list == null || data.list.length == 0) {
                        utils.$attachmentFallback.show();
                        return;
                    }
                    utils.$attachmentFallback.hide();
                    for (var index=0;index<data.list.length;index++) {
                        var $li = utils.$liTemplate.clone();
                        var attachment = data.list[index];
                        var $anchor = $li.find('.process-attachment-link');
                        $anchor.attr('href', attachment.link);
                        $anchor.text(attachment.label);
                        $li.attr('data-process-attachment-modified', attachment.lastModified);
                        $li.find('.process-attachment-description').text(attachment.description);
                        $li.find('.process-attachment-date').text(moment(attachment.lastModified).format('MMMM Do YYYY, h:mm:ss a'));
                        $li.find('.process-attachment-owner').text(attachment.user.displayName);
                        $newAttachmentList.append($li);
                    }

                    var $deleteButtons = $newAttachmentList.find('.process-attachment-delete');
                    $deleteButtons.off('click');
                    $deleteButtons.on('click', utils.deleteAttachment);

                    var $container = $attachmentList.parent();
                    $attachmentList.replaceWith($newAttachmentList);

                    if ($container.length > 0)
                        $container.scrollTop($container[0].scrollHeight);
                });
            }
        },
        includeVariableButtons: function(field, $form) {
            var selector = ':input[type="file"][name="' + field.name + '"]';
            var descriptionSelector = ':input.process-variable-description[data-element="' + field.name + '"]';
            $form.find(selector).change(function(event) {
                var files = $(event.target)[0].files;
                var data = new FormData();
                var description = $(descriptionSelector).val();
                if (typeof(description) !== 'undefined')
                    data.append(field.name + '!description', description);

                $.each(files, function(i, file) {
                    data.append(field.name, file);
                });

                var url = $form.attr('action') + '.json';
                $.ajax({
                    url : url,
                    data : data,
                    processData : false,
                    contentType : false,
                    type : 'POST',
                    success: function() {
                        utils.onVariableItemAdded(field, $form);
                        $(descriptionSelector).val('');
                    }
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    var data = $.parseJSON(jqXHR.responseText);
                    var selector = '.process-alert[data-element="' + field.name + '"]';
                    var message = data.messageDetail;
                    var $alert = $(selector);
                    $alert.show();
                    $alert.text(message);
                });

                return false;
            });
            $form.submit(function(event) {
                var data = new FormData();
                var $form = $(this);
                var isEmpty = true;

                $form.find(':input').each(function(i, element) {
                    var value = $(element).val();
                    if (value != '')
                        isEmpty = false;
                    data.append(element.name, value);
                });

                if (isEmpty)
                    return false;

                var url = $form.attr('action') + '.json';
                $.ajax({
                    url : url,
                    data : data,
                    processData : false,
                    contentType : false,
                    type : 'POST',
                    success: function(data) {
                        utils.onVariableItemAdded(field, $form, data);
                    }
                });
                return false;
            });
        },
        includeVariableItems: function(field, $form, data) {
            var $attachmentList = $form.find('ul.process-variable-list');
            if ($attachmentList.length > 0) {
                if (utils.$variableLiTemplates == undefined) {
                    utils.$variableLiTemplates = {};
                }
                if (utils.$variableLiTemplates[field.name] == undefined) {
                    utils.$variableLiTemplates[field.name] = $attachmentList.find('li:first');
                    utils.$variableLiTemplates[field.name].remove();
                }

                var $newAttachmentList = $attachmentList.clone();
                $newAttachmentList.empty();
                var attachmentUrl = $form.attr('action') + ".json";
                $.get(attachmentUrl, function(data) {
                    if (data.list == null || data.list.length == 0) {
                        return;
                    }
                    for (var index=0;index<data.list.length;index++) {
                        var $li = utils.$variableLiTemplates[field.name].clone();
                        var attachment = data.list[index];
                        var $anchor = $li.find('.process-variable-link');
                        var $description = $li.find('.process-variable-description');
                        $anchor.attr('href', attachment.link);
                        $anchor.text(attachment.name);
                        if (attachment.description != null)
                            $description.text(attachment.description);
                        $newAttachmentList.append($li);
                    }

                    var $deleteButtons = $newAttachmentList.find('.process-variable-delete');
                    $deleteButtons.off('click');
                    $deleteButtons.on('click', utils.deleteVariableItem);

                    var $container = $attachmentList.parent();
                    if ($container.length > 0)
                        $container.scrollTop($container[0].scrollHeight);
                });
                $attachmentList.replaceWith($newAttachmentList);
            }
        },
        onAttachmentAdded: function() {
            utils.$attachmentForm.find(':input[name="description"]').val('');
            utils.includeAttachments(utils.model);
        },
        onVariableItemAdded: function(field, $form, data) {
            utils.includeVariableItems(field, $form, data);
            $.get(field.link + ".json", function(data) {
                var fieldData = {};
                fieldData[field.name] = data.list;
                utils.decorateField(field, fieldData, null, $form.find(":input"), $(".process-variable"));
            });
        },
        onValid: function($form) {

        },
        onInvalid: function($form) {

        },
        populate: function(model) {
            var data = model.data;
            var container = model.container;
            var task = model.task;

            if (task != null) {
                var created = task.startTime;
                $('body').attr('data-process-task-started', created);
            }

            var validation = model.validation;
            this.model = model;
            this.decorateForms(model);
            this.decorateContainer(container, data, validation);
            this.includeAttachments(model);
        },
        submitAttachment: function(event) {
            event.preventDefault();
            event.stopPropagation();
            var $form = $(event.target);
            var description = $form.find(':input[name="description"]').val();
            var data = new FormData();

            data.append('description', description);
            var attachmentUrl = $form.attr('action') + '.json';
            $.ajax({
                url : attachmentUrl,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                success: utils.onAttachmentAdded
            });

            return false;
        },
        submitMain: function(event) {
            event.preventDefault();
            event.stopPropagation();
            var $form = $(event.target);
            var data = new FormData();
            $('.process-alert').hide();
            $form.find(':input')
                .each(function(index, element) {
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

            var attachmentUrl = $form.attr('action') + '.json';
            $.ajax({
                url : attachmentUrl,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                success: function() {
                    utils.onValid($form);
                },
                failure: function() {
                    utils.onInvalid($form);
                },
            })
            .done(function(data, textStatus, errorThrown) {
                $('html').load(data);
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
                switch (jqXHR.status) {
                case 303:
                    var location = jqXHR.getResponseHeader('location');
                    if (location != null)
                        window.location.href = location;
                    break;
                case 400:
                    var data = $.parseJSON(jqXHR.responseText);
                    var items = data.items;
                    if (items != null && items.length > 0) {
                        for (var i=0;i<items.length;i++) {
                            var item = items[i];
                            var selector = '.process-alert[data-element="' + item.propertyName + '"]';
                            var message = item.message;
                            var $alert = $(selector);
                            $alert.show();
                            $alert.text(message);
                        }
                    }

                    break;
                }
            });

            return false;
        }
    };
    (function() {
        var cache = {}, viewKey = "pwView", methods;
        methods = {
            initialize: function() {
                return this.each(initialize);
                function initialize() {
                    var model = {{{model}}};
                    utils.populate(model);
                }
            },
            destroy: function() {
                return this.each(destroy);
                function destroy() {

                }
            }
        };

        jQuery.fn.webform = function(method) {
            if (methods[method]) {
                return methods[method].apply(this, [].slice.call(arguments, 1));
            } else {
                return methods.initialize.apply(this, arguments);
            }
        };
    })();
})(jQuery);


$(function() {

    $(window).webform();

});