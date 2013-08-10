define([ 'models/base/collection', 'models/design/fields', 'views/form/fields-view', 'views/base/view', 'text!templates/form/section.hbs'],
		function(Collection, Fields, FieldsView, View, template) {
	'use strict';

	var SectionView = View.extend({
		autoRender: false,
		className: 'section hide',
		tagName: 'li',
		template: template,
		render: function() {
            View.__super__.render.apply(this);
            var sectionId = this.model.get("sectionId");
            this.$el.attr('id', sectionId);
            var view = this.subview('fieldsView');
            if (!view) {
                var readonly = this.model.get("readonly");
                var modelFields = this.model.get("fields");
                if (readonly) {
                    for (var i=0;i<modelFields.length;i++) {
                        modelFields[i].editable = false;
                    }
                }
                var fields = new Fields(modelFields);
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