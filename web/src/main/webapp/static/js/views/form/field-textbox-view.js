define([ 'views/form/base-field-view', 'text!templates/form/field-textbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldTextboxView = View.extend({
		template: template,
		events: {
		    'click button.add-input-button': '_onAddInputButtonClick',
		},
//		render: function(options) {
//            View.__super__.render.apply(this, options);
//
//            return this;
//        },
		_onAddInputButtonClick: function(event) {
            var $controlGroup = this.$(event.target).closest('.form-group');
            var $input = $controlGroup.find(':input[type="text"]:last');
            if ($input.length == 0)
                $input = $controlGroup.find(':input[type="email"]:last');

            var $clone = $input.clone();

            $controlGroup.find('span.generated').remove();
            $controlGroup.removeClass('has-error');
            $clone.val();
            $controlGroup.append("<br/>");
            $controlGroup.append($clone);
		}
	});

	return FieldTextboxView;
});