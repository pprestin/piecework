define([ 'views/form/base-field-view', 'text!templates/form/field-checkbox.hbs'],
		function(View, template) {
	'use strict';

	var FieldCheckboxView = View.extend({
		template: template,
		initialize: function(options) {
            View.__super__.initialize.apply(this, options);

            var modelName = this.model.get("name");
            var modelOptions = this.model.get("options");
            var values = this.model.get("values");
            if (modelName != null && modelOptions != null && modelOptions.length > 0) {
                for (var i=0;i<modelOptions.length;i++) {
                    var option = modelOptions[i];
                    if (option.name == null)
                        option.name = modelName;
                    if (option.value in values)
                        option.selected = true;

                }

            }

            return this;
        }

	});

	return FieldCheckboxView;
});