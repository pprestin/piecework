define([ 'chaplin', 'views/base/view'],
		function(Chaplin, View) {
	'use strict';

	var FieldView = View.extend({
		autoRender: true,
		className: 'form-group',
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

            var constraints = this.model.get('constraints');
            this._subscribeDependencies(constraints);

            if (!visible)
                this.$el.addClass('hide');

            return this;
        },
        render: function(options) {
            View.__super__.render.apply(this, options);

            this.$el.attr('data-rendered', true);
            var mask = this.model.get("mask");
            if (mask != null) {
                var $input = this.$el.find(':input');
                $input.mask(mask);
            }
            var messageType = this.model.get("messageType");
            if (messageType != null) {
                this.$el.addClass(messageType);
            }

            var editable = this.model.get("editable");
            if (editable) {
                var maxInputs = this.model.get("maxInputs");
                if (maxInputs != null && maxInputs > 1) {
                    this.$el.addClass('input-append');
                    this.$el.find(':input').after('<div class="btn-group"><button class="btn btn-default add-input-button" type="button" role="button"><i class="icon-plus-sign"></i></button></div>');
                }
            }

            return this;
        },
        _subscribeDependencies: function(constraints) {

            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];

                    if (constraint != undefined) {
                        if (constraint.type == null || constraint.type == 'IS_ONLY_VISIBLE_WHEN' || constraint.type == 'IS_ONLY_REQUIRED_WHEN') {
                            Chaplin.mediator.subscribe('value:' + constraint.name, this._onDependencyValueChange, this);
                            if (constraint.and != null)
                                this._subscribeDependencies(constraint.and);
                            if (constraint.or != null)
                                this._subscribeDependencies(constraint.or);
                        }
                    }
                }
            }
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

        },
        _onValueChange: function(event) {
            var name = event.target.name;
            var value = event.target.value;
            Chaplin.mediator.publish('value:' + name, name, value);
            this.$el.removeClass('has-error');
            this.$el.find('span.generated').remove();
        },
        _onDependencyValueChange: function(name, value) {
            var constraints = this.model.get('constraints');

            if (this._hasConstraint('IS_ONLY_VISIBLE_WHEN', constraints)) {
                if (this._checkAll('IS_ONLY_VISIBLE_WHEN', constraints))
                    this.$el.removeClass('hide');
                else
                    this.$el.addClass('hide');
            }

            if (this._hasConstraint('IS_ONLY_REQUIRED_WHEN', constraints)) {
                var $input = this.$el.find(':input');
                if (this._checkAll('IS_ONLY_REQUIRED_WHEN', constraints))
                    $input.attr('required', true);
                else
                    $input.removeAttr('required');
            }
        },
        _hasConstraint: function(type, constraints) {
            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];
                    if (type != null && constraint.type != null && constraint.type == type) {
                        return true;
                    }
                }
            }
            return false;
        },
        _evaluateConstraint: function(constraint) {
            var constraintName = constraint.name;
            var constraintValue = constraint.value;
            var pattern = new RegExp(constraint.value);
            var selector = ':input[name="' + constraint.name + '"]';
            var $element = $(selector);
            if ($element.is(':checkbox') || $element.is(":radio"))
                $element = $element.filter(':checked');

            var satisfied = false;
            var context = this;

            var value = $element.val();
            satisfied = value != null && pattern.test(value);

            if (satisfied) {
                return this._checkAll(null, constraint.and);
            } else {
                if (constraint.or != null && constraint.or.length > 0)
                    return context._checkAny(null, constraint.or);
            }

            return satisfied;
        },
        _checkAll: function(type, constraints) {
            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];
                    if (type == null || constraint.type == null || constraint.type == type) {
                        if (! this._evaluateConstraint(constraint))
                            return false;
                    }
                }
            }
            return true;
        },
        _checkAny: function(type, constraints) {
            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];
                    if (type == null || constraint.type == null || constraint.type == type) {
                        if (this._evaluateConstraint(constraint))
                            return true;
                    }
                }
                return false;
            }
            return true;
        },

//        _findVisibilityConstraints: function(name) {
//            var constraints = this.model.get('constraints');
//            var visibilityConstraints = [];
//
//            if (constraints != undefined && constraints.length > 0) {
//                for (var i=0;i<constraints.length;i++) {
//                    var constraint = constraints[i];
//
//                    if (constraint != undefined && constraint.type != undefined) {
//
//                        //if (constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
//                        if (name == undefined || constraint.name == name)
//                            visibilityConstraints.push(constraint);
//                        else if (constraint.type == 'AND' || constraint.type == 'OR') {
//                            if (constraint.subconstraints != null && constraint.subconstraints.length > 0) {
//                                for (var j=0;j<constraint.subconstraints.length;j++) {
//                                    var subconstraint = constraint.subconstraints[j];
//                                    if (subconstraint.type = 'IS_ONLY_VISIBLE_WHEN') {
//                                        visibilityConstraints.push(constraint);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            return visibilityConstraints;
//        }

	});

	return FieldView;
});