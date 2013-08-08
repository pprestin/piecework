define([ 'models/base/collection', 'models/design/fields', 'views/form/fields-view', 'views/base/view', 'text!templates/form/section.hbs'],
		function(Collection, Fields, FieldsView, View, template) {
	'use strict';

	var SectionView = View.extend({
		autoRender: false,
		className: 'section hide row',
		tagName: 'li',
		template: template,
		render: function() {
            View.__super__.render.apply(this);
            var sectionId = this.model.get("sectionId");
            this.$el.attr('id', sectionId);
//            var isSelected = this.model.get("selected");
//            if (isSelected == undefined)
//                isSelected = false;
//            this.$el.toggle(isSelected);
            var view = this.subview('fieldsView');
            if (!view) {
                var fields = new Fields(this.model.get("fields"));
                view = new FieldsView({autoRender: false, collection: fields});
                this.subview('fieldsView', view);
            }
            var item = view.render();
            var $container = this.$el.find(view.container);
            $container.addClass(view.className);
            $container.append(item.$el);
            return this;
        },
	});

	return SectionView;
});