define([ 'chaplin', 'models/design/field', 'views/base/collection-view', 'views/design/field-item-view' ],
		function(Chaplin, Field, CollectionView, FieldItemView) {
	'use strict';

	var FieldListView = CollectionView.extend({
		autoRender: true,
		className: "nav nav-list",
		region: 'sidebar-content',
		itemView: FieldItemView,
		tagName: 'ul',
		onFieldChanged: function(field) {
			// The field is only added/updated to the collection when it changes
			this.collection.add(field, {merge:true});
			this.renderItem(field);
		},		
	});

	return FieldListView;
});