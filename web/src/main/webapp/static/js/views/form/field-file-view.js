define([ 'views/form/base-field-view', 'text!templates/form/field-file.hbs'],
		function(View, template) {
	'use strict';

	var FieldFileView = View.extend({
		template: template,
		render: function() {
            View.__super__.render.apply(this);

            var accept = this.model.get("accept");
            var re = RegExp("image/");
            if (accept != null && re.test(accept)) {
                var values = this.model.get("values");
                if (values != null) {
                    this.model.set("images", values);
                }
                //this.$el.before('<img class="file" src=""/>');
            }
            return this;
        }
	});

	return FieldFileView;
});