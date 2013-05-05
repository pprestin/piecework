define([ 'chaplin', 'models/field', 'models/screen', 'views/field-configure-view', 'views/base/view', 'text!templates/screen-configure.hbs'], 
		function(Chaplin, Field, Screen, FieldConfigureView, View, template) {
	'use strict';

	var ScreenConfigureView = View.extend({
		autoRender : true,
		container: 'body',
		listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    //template: template,
	    _onAddedToDOM: function() {
	    	var view = this;
			$(':input').each(function(i, element) {
				var $input = $(element);
				var name = element.name;
				var fieldView = new FieldConfigureView({model: new Field({name:name})});
				view.subview(name, fieldView);
				//fieldView.render();
				//$input.append(fieldView.render());
			});
		},
	});

	return ScreenConfigureView;
});