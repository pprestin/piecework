define([ 'models/base/collection', 'views/base/view', 'text!templates/form/grouping.hbs'],
		function(Collection, View, template) {
	'use strict';

	var GroupingView = View.extend({
		autoRender: false,
		className: 'grouping',
		container: '.screen-header',
		tagName: 'div',
		template: template,
	});

	return GroupingView;
});