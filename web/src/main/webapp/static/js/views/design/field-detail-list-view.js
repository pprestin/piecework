define([ 'chaplin', 'models/field', 'views/base/collection-view', 'views/field-detail-view', 'views/field-checkbox-layout-view', 
         'views/field-listbox-layout-view', 'views/field-radio-layout-view', 'views/field-textarea-layout-view', 'views/field-textbox-layout-view' ], 
		function(Chaplin, Field, CollectionView, FieldDetailView, CheckboxLayoutView, ListboxLayoutView, RadioLayoutView, TextareaLayoutView, 
				TextboxLayoutView) {
	'use strict';

	var FieldDetailListView = CollectionView.extend({
		autoRender: true,
		className: "field-list nav",
		tagName: 'ul',
		listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
		initItemView: function(field) {
			var fieldId = field.cid;
			var type = field.attributes.type;
			if (type == 'checkbox')
				return new CheckboxLayoutView({id: fieldId, model: field});
	    	else if (type == 'listbox')
	    		return new ListboxLayoutView({id: fieldId, model: field});
	    	else if (type == 'radio')
	    		return new RadioLayoutView({id: fieldId, model: field});
	    	else if (type == 'textarea')
	    		return new TextareaLayoutView({id: fieldId, model: field});
	    	else
	    		return new TextboxLayoutView({id: fieldId, model: field});
	    },
	    _onAddedToDOM: function() {
	    	this.$el.sortable({ handle: ".drag-handle" });
	    }
	});

	return FieldDetailListView;
});