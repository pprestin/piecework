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
		initialize: function(options) {
            View.__super__.initialize.apply(this, options);

            var visibilityConstraints = this._findVisibilityConstraints();
            if (visibilityConstraints == undefined || visibilityConstraints.length <= 0)
                return this;

            for (var i=0;i<visibilityConstraints.length;i++) {
                var constraint = visibilityConstraints[i];
                if (constraint.name != undefined) {
                    Chaplin.mediator.subscribe('value:' + constraint.name, this._onDependencyValueChange, this);
                    this.$el.addClass('hide');
                }
            }

            return this;
        },
        _onValueChange: function(event) {
            var name = event.target.name;
            var value = event.target.value;
            Chaplin.mediator.publish('value:' + name, name, value);
        },
        _onDependencyValueChange: function(name, value, constraint) {
            var visibilityConstraints = this._findVisibilityConstraints(name);
            if (visibilityConstraints == undefined || visibilityConstraints.length <= 0)
                return;

            for (var i=0;i<visibilityConstraints.length;i++) {
                if (visibilityConstraints[i].name == name) {
                    var pattern = new RegExp(visibilityConstraints[i].value);
                    if (pattern.test(value))
                        this.$el.removeClass('hide');
                    else
                        this.$el.addClass('hide');
                }
            }
        },
        _findVisibilityConstraints: function(name) {
            var constraints = this.model.get('constraints');
            var visibilityConstraints = [];

            if (constraints != undefined && constraints.length > 0) {
                for (var i=0;i<constraints.length;i++) {
                    var constraint = constraints[i];

                    if (constraint != undefined && constraint.type != undefined && constraint.type == 'IS_ONLY_VISIBLE_WHEN') {
                        if (name == undefined || constraint.name == name)
                            visibilityConstraints.push(constraint);
                    }
                }
            }

            return visibilityConstraints;
        }

	});

	return FieldView;
});