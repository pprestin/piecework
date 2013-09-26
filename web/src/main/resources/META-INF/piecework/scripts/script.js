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
                }
            })
        },
        decorateScreen: function(screen, data) {
            var $inputs = $(':input');
            var $variables = $('.process-variable');
            if (screen != undefined) {
                var sections = screen.sections;
                if (sections != undefined) {
                    for (var i=0;i<sections.length;i++) {
                        this.decorateSection(sections[i], data, $inputs, $variables);
                    }
                }
            }
        },
        decorateSection: function(section, data, $inputs, $variables) {
            var fields = section.fields;
            if (fields != undefined) {
                for (var j=0;j<fields.length;j++) {
                    this.decorateField(fields[j], data, $inputs, $variables);
                }
            }
        },
        decorateField: function(field, data, $inputs, $variables) {
            var name = field.name;
            var type = field.type;
            var values = data[name];
            var inputSelector = '[name="' + name + '"]';
            var variableSelector = '[data-element="' + name + '"]';
            var $input = $inputs.filter(inputSelector);
            var $variable = $variables.filter(variableSelector);

            var isValidUser = false;

            if (field.constraints != null && field.constraints.length > 0) {
                for (var c=0;c<field.constraints.length;c++) {
                    var constraint = field.constraints[c];
                    if (constraint.type == 'IS_VALID_USER') {
                        isValidUser = true;
                        break;
                    }
                }
            }

            if (isValidUser) {
                this.decoratePersonFields(field, data, $inputs, $variables);
            } else if (type == 'file') {
                this.decorateFieldForm(field);

                if ($variable.is("img") && values != null && values.length > 0) {
                    $variable.attr('src', values[0].link);
                }
            } else {
                if ($input.length > 0 && type != 'file') {
                    var isEmpty = true;
                    for (var x=0;x<values.length;x++) {
                        if (values[x] != null && values[x] != '')
                            isEmpty = false;
                    }
                    if (!isEmpty)
                        $input.val(values);
                }
                if ($variable.length > 0 && !$variable.is('form'))
                    $variable.text(values);
            }

        },
        decoratePersonFields: function(field, data, $inputs, $variables) {
            var name = field.name;
            var type = field.type;
            var values = data[name];
            var subfields = ['displayName', 'visibleId'];
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
            $form.find(':input.process-variable-file[type="file"]').change(function(event) {
                var files = $(event.target)[0].files;
                var data = new FormData();

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
                    }
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
                        $anchor.attr('href', attachment.link);
                        $anchor.text(attachment.name);
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
        },
        populate: function(model) {
            var data = model.data;
            var screen = model.screen;
            this.model = model;
            this.decorateForms(model);
            this.decorateScreen(screen, data);
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