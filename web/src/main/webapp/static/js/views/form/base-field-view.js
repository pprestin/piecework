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

            this._subscribeDependencies(visibilityConstraints);

            if (!visible)
                this.$el.addClass('hide');

            return this;
        },
        render: function(options) {
            View.__super__.render.apply(this, options);
            return this;
        },
        _isVisible: function(constraints, requireAll) {
            if (constraints == undefined || constraints.length <= 0)
                return true;

            var hasVisibilityConstraints = false;
            var visible = requireAll;
            for (var i=0;i<constraints.length;i++) {
                var constraint = constraints[i];

                if (constraint.type == undefined)
                    continue;

                var satisfied = false;
                if (constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
                    var selector = ':input[name="' + constraint.name + '"]';
                    var $element = $(selector);
                    if ($element.is(':checkbox') || $element.is(":radio"))
                        $element = $element.filter(':checked');
                    var value = $element.val();
                    var pattern = new RegExp(constraint.value);
                    satisfied = value != null && pattern.test(value);
                    hasVisibilityConstraints = true;
                } else if (constraint.type == 'AND') {
                    satisfied = this._isVisible(constraint.subconstraints, true);
                } else if (constraint.type == 'OR') {
                    satisfied = this._isVisible(constraint.subconstraints, false);
                } else {
                    continue;
                }

                if (requireAll && !satisfied) {
                    visible = false;
                    break;
                } else if (!requireAll && satisfied) {
                    visible = true;
                    break;
                }
            }

            if (!hasVisibilityConstraints)
                return true;

            return visible;
        },
        _subscribeDependencies: function(constraints) {
            if (constraints == undefined || constraints.length <= 0)
                return;

            for (var i=0;i<constraints.length;i++) {
                var constraint = constraints[i];

                if (constraint.type == undefined)
                    continue;

                if (constraint.type == 'IS_ONLY_VISIBLE_WHEN' && constraint.name != undefined) {
                    Chaplin.mediator.subscribe('value:' + constraint.name, this._onDependencyValueChange, this);
                } else if (constraint.type == 'AND' || constraint.type == 'OR') {
                    this._subscribeDependencies(constraint.subconstraints);
                }
            }
        },
        _testConstraints: function(name) {
            var allSatisfied = this._isVisible(this._findVisibilityConstraints(name), true);
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
            this._testConstraints();
        },
        _onValueChange: function(event) {
            var name = event.target.name;
            var value = event.target.value;
            Chaplin.mediator.publish('value:' + name, name, value);
        },
        _onDependencyValueChange: function(name, value) {
            this._testConstraints(name);
        },
        _findVisibilityConstraints: function(name) {
            var constraints = this.model.get('constraints');
            var visibilityConstraints = [];

            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];

                    if (constraint != undefined && constraint.type != undefined) {

                        //if (constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
                        if (name == undefined || constraint.name == name)
                            visibilityConstraints.push(constraint);
                        else if (constraint.type == 'AND' || constraint.type == 'OR') {
                            if (constraint.subconstraints != null && constraint.subconstraints.length > 0) {
                                for (var j=0;j<constraint.subconstraints.length;j++) {
                                    var subconstraint = constraint.subconstraints[j];
                                    if (subconstraint.type = 'IS_ONLY_VISIBLE_WHEN') {
                                        visibilityConstraints.push(constraint);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return visibilityConstraints;
        }

	});

	return FieldView;
});