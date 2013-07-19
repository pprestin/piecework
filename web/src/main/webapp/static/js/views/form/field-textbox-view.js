define([ 'views/form/base-field-view', 'text!templates/form/field-textbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldTextboxView = View.extend({
		template: template,
		events: {
		    'click button.add-input-button': '_onAddInputButtonClick',
		},
		render: function(options) {
            View.__super__.render.apply(this, options);

            return this;
        },
		_onAddInputButtonClick: function(event) {
            var $controlGroup = this.$(event.target).closest('.control-group');
            var $input = $controlGroup.find(':input[type="text"]:last');
            var $clone = $input.clone();
            $clone.val();
            $controlGroup.append("<br/>");
            $controlGroup.append($clone);
		}
	});

	return FieldTextboxView;
});