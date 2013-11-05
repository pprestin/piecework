define([ 'views/form/base-field-view', 'text!templates/form/field-radio.hbs'],
		function(View, template) {
	'use strict';

	var FieldRadioView = View.extend({
		template: template,
                initialize: function (options) {
            View.__super__.initialize.apply(this, options);

            var modelValue = this.model.get("value");
            var modelOptions = this.model.get("options");
            if (modelOptions != null && modelOptions.length > 0) {
                for (var i=0;i<modelOptions.length;i++) {
                    var option = modelOptions[i];
                    if (modelValue != null && modelValue.length > 0 && option.value == modelValue) {
                        option.selected = true;
                    }   
                }   

            }   

            return this;
        }   

	});

	return FieldRadioView;
});
