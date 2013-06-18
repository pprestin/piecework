define([ 'models/base/collection', 'views/base/view', 'text!templates/form/grouping.hbs'],
		function(Collection, View, template) {
	'use strict';

	var GroupingView = View.extend({
		autoRender: false,
		className: 'grouping',
		container: '.screen-header',
		tagName: 'div',
		template: template,
		render: function(options) {
            View.__super__.render.apply(this, options);
            var groupingId = this.model.get('groupingId');
            this.$el.attr('id', groupingId);
            return this;
        },
	});

	return GroupingView;
});