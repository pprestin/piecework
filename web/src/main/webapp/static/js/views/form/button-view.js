define([ 'chaplin', 'views/base/view', 'text!templates/form/button.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var ButtonView = View.extend({
		autoRender : true,
		container: '.screen-buttons',
		tagName: 'li',
	    template: template,
	    render: function(options) {
            View.__super__.render.apply(this, options);
            if (this.model.get("type") == 'submit')
                this.$el.find(".btn").addClass('btn-primary');

            return this;
        },
	});

	return ButtonView;
});