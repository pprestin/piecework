define([ 'chaplin', 'views/base/view', 'text!templates/form/form.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var FormView = View.extend({
		autoRender : true,
		container: '.main-content',
		id: 'main-form',
		tagName: 'form',
	    template: template,
	    events: {
	        'submit': '_onFormSubmit',
	    },
	    _onFormSubmit: function(event) {
            var type = this.model.get("type");

            if (type == 'WIZARD') {


                return false;
            }

	        return true;
	    },
	});

	return FormView;
});