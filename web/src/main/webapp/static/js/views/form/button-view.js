define([ 'chaplin', 'views/base/view', 'text!templates/form/button.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var ButtonView = View.extend({
		autoRender : true,
		tagName: 'li',
	    template: template,
	    render: function(options) {
            View.__super__.render.apply(this, options);
            if (this.model.get("type") == 'submit' || this.model.get("alt") == 'Next')
                this.$el.find(".btn").addClass('btn-primary');

            return this;
        },
	});

	return ButtonView;
});