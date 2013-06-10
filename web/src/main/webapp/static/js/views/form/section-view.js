define([ 'models/base/collection', 'views/form/fields-view', 'views/base/view', 'text!templates/form/section.hbs'],
		function(Collection, FieldsView, View, template) {
	'use strict';

	var SectionView = View.extend({
		autoRender: false,
		className: 'section',
		tagName: 'li',
		template: template,
	});

	return SectionView;
});