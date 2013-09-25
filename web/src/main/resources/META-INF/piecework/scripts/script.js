/*!
 * script.js 0.1.0
 * https://github.com/piecework/piecework
 * Copyright 2013 University of Washington; Licensed Educational Community License 2.0
 */

(function($) {
    var VERSION = "0.1.0";
    var utils = {
        decorateFieldForm: function(field) {
            var fieldFormSelector = 'form[data-process-form="' + field.name + '"]';
            var $fieldForm = $(fieldFormSelector);
            $fieldForm.attr('action', field.link);
            $fieldForm.attr('method', 'POST');
            $fieldForm.attr('enctype', 'multipart/form-data');
        },
        decorateForms: function(model) {
            $('form').each(function(index, element) {
                $form = $(element);
                var formType = $form.attr('data-process-form');
                var isExcluded = $form.attr('data-process-exclude');
                if (isExcluded)
                    return;

                if (formType == undefined || formType == 'main') {
                    $form.attr('action', model.action);
                    $form.attr('method', 'POST');
                    $form.attr('enctype', 'multipart/form-data');
                } else if (formType == 'attachments') {
                      $form.attr('action', model.attachment);
                      $form.attr('method', 'POST');
                } else if (formType == 'cancellation') {
                    $form.attr('action', model.cancellation);
                    $form.attr('method', 'POST');
                }
            })
        },
        decorateScreen: function(screen, data) {
            var $inputs = $(':input');
            var $variables = $('[data-process-variable]');
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
            var variableSelector = '[data-process-variable="' + name + '"]';
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
            } else {
                if ($input.length > 0 && type != 'file')
                    $input.val(values);
                if ($variable.length > 0)
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
                var subfieldVariableSelector = '[data-process-variable="' + subfieldName + '"]';
                var subfieldValues = values[0][subfield];
                var $subfieldInputs = $inputs.filter(subfieldInputSelector);
                var $subfieldVariables = $variables.filter(subfieldVariableSelector);
                $inputs.filter(subfieldInputSelector).val(subfieldValues);
                $variables.filter(subfieldVariableSelector).text(subfieldValues);
            }
        },
        populate: function(model) {
            var data = model.data;
            var screen = model.screen;
            this.decorateForms(model);
            this.decorateScreen(screen, data);
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