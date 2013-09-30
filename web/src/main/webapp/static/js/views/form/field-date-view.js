define([ 'views/form/base-field-view', 'text!templates/form/field-date.hbs'],
		function(View, template) {
	'use strict';

	var FieldDateView = View.extend({
		template: template,
        render: function(options) {
            View.__super__.render.apply(this, options);
            var url = '../person.json?displayNameLike=%QUERY';
            var editable = this.model.get('editable');

            if (editable) {
                var $textbox = this.$el.find('input[type="text"]');
                $textbox.attr('data-process-user-lookup', 'true');
            }
        }
	});

	return FieldDateView;
});