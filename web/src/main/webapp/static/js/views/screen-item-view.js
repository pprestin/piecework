define([ 'views/base/view', 'text!templates/screen-item.hbs' ], function(View, template) {
	'use strict';

	var ScreenItemView = View.extend({
		tagName: 'li',
	    template: template,
	    render: function(options) {
			View.__super__.render.apply(this, options);
			// Ensure that the input elements are populated
			var model = this.model;
			this.$(':input').each(function(i, element) {
				var name = element.name;
				if (name !== undefined && name != '')
					element.value = model.attributes[name];
			});
		},
	});

	return ScreenItemView;
});