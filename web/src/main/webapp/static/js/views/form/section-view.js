define([ 'models/base/collection', 'views/form/fields-view', 'views/base/view', 'text!templates/form/section.hbs'],
		function(Collection, FieldsView, View, template) {
	'use strict';

	var SectionView = View.extend({
		autoRender: false,
		className: 'section',
		tagName: 'li',
		template: template,
//        render: function(options) {
//            View.__super__.render.apply(this, options);
//
//            var view;
//            view = this.subview("fieldsView");
//            if (!view) {
//                var collection = new Collection(this.model.get("fields"));
//                view = new FieldsView({container: '.section-content', collection: collection});
//                this.subview("fieldsView", view);
//            }
//            view.render();
//
//            return this;
//        }
	});

	return SectionView;
});