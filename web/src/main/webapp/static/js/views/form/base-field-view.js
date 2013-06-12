define([ 'chaplin', 'views/base/view'],
		function(Chaplin, View) {
	'use strict';

	var FieldView = View.extend({
		autoRender: true,
		className: 'control-group',
		tagName: 'div',
		events: {
		    'change': '_onValueChange',
		},
		listen: {
		    'addedToParent': '_onAddedToDOM',
		    'formAddedToDOM mediator': '_onFormAddedToDOM',
		},
		initialize: function(options) {
            View.__super__.initialize.apply(this, options);

            var defaultValue = this.model.get("defaultValue");
            var fieldOptions = this.model.get("options");
            var visible = this.model.get("visible");

            if (defaultValue != undefined && fieldOptions != undefined) {
                for (var i=0;i<fieldOptions.length;i++) {
                    var fieldOption = fieldOptions[i];

                    if (fieldOption.value == defaultValue)
                        fieldOption.selected = true;
                }
            }

            var visibilityConstraints = this._findVisibilityConstraints();
            if (visibilityConstraints == undefined || visibilityConstraints.length <= 0)
                return this;

            for (var i=0;i<visibilityConstraints.length;i++) {
                var constraint = visibilityConstraints[i];
                if (constraint.name != undefined) {
                    Chaplin.mediator.subscribe('value:' + constraint.name, this._onDependencyValueChange, this);
                }
            }

            if (!visible)
                this.$el.addClass('hide');

            return this;
        },
        render: function(options) {
            View.__super__.render.apply(this, options);
            return this;
        },
        _checkConstraints: function() {
            var visibilityConstraints = this._findVisibilityConstraints();
            if (visibilityConstraints == undefined || visibilityConstraints.length <= 0)
                return;

            var allSatisfied = true;
            for (var i=0;i<visibilityConstraints.length;i++) {
                var selector = ':input[name="' + visibilityConstraints[i].name + '"]';
                var $element = $(selector);
                if ($element.is(':checkbox') || $element.is(":radio"))
                    $element = $element.filter(':checked');
                var value = $element.val();
                var pattern = new RegExp(visibilityConstraints[i].value);
                if (pattern.test(value))
                    visibilityConstraints[i].satisfied = true;
                else {
                    visibilityConstraints[i].satisfied = false;
                    allSatisfied = false;
                    break;
                }
             }

             if (allSatisfied)
                 this.$el.removeClass('hide');
             else
                 this.$el.addClass('hide');
        },
        _onAddedToDOM: function() {
            var defaultValue = this.model.get("defaultValue");
            var fieldOptions = this.model.get("options");
            if (defaultValue != undefined && fieldOptions != undefined) {
                for (var i=0;i<fieldOptions.length;i++) {
                    var fieldOption = fieldOptions[i];

                    if (fieldOption.value == defaultValue) {
                        var selector = '#' + fieldOption.optionId;
                        var $element = $(selector);
                        $element.prop('checked', true);
                        fieldOption.selected = true;
                    }
                }
            }
        },
        _onFormAddedToDOM: function(event) {
            this._checkConstraints();
        },
        _onValueChange: function(event) {
            var name = event.target.name;
            var value = event.target.value;
            Chaplin.mediator.publish('value:' + name, name, value);
        },
        _onDependencyValueChange: function(name, value) {
            this._checkConstraints();
        },
        _findVisibilityConstraints: function(name) {
            var constraints = this.model.get('constraints');
            var visibilityConstraints = [];

            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];

                    if (constraint != undefined && constraint.type != undefined) {

                        if (constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
                            if (name == undefined || constraint.name == name)
                                visibilityConstraints.push(constraint);
                            }
                        } else if (constraint.type == 'AND') {

                        }
                    }
                }
            }

            return visibilityConstraints;
        }

	});

	return FieldView;
});