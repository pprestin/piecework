define([ 'views/base/view', 'text!templates/screen-item.hbs' ], 
		function(View, template) {
	'use strict';

	var ScreenItemView = View.extend({
		className: 'screen-item',
		tagName: 'li',
	    template: template,
//	    render: function(options) {
//			var view = View.__super__.render.apply(this, options);
//			// Ensure that the input elements are populated
//			var model = this.model;
//			this.$(':input').each(function(i, element) {
//				var name = element.name;
//				var value = model.get(name);
//				if (name !== undefined && name != '' && value !== undefined)
//					element.value = value;
//			});
//			
//			return view;
//		},
	});

	return ScreenItemView;
});