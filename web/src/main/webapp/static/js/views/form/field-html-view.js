define([ 'views/form/base-field-view'],
		function(View) {
	'use strict';

	var FieldHtmlView = View.extend({
		render: function(options) {
            View.__super__.render.apply(this, options);
            this.$el.html(this.model.get("defaultValue"));
            return this;
        },
	});

	return FieldHtmlView;
});