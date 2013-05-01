define([ 'views/base/collection-view', 'views/process-detail-view', 'views/process-item-view' ], 
		function(CollectionView, ProcessDetailView, ProcessItemView) {
	'use strict';

	var ProcessListView = CollectionView.extend({
		container: '.sidebar-nav',
		itemView: ProcessItemView,
		initialize: function(options) {
			CollectionView.__super__.initialize.apply(this, options);
			this.subview('dialog', new ProcessDetailView({model: this.collection}));
		},
		className: "nav nav-list",
		tagName: 'ul',
//		render: function() {
//			this.$el.append('<li class="nav-header">MY PROCESSES</li>');
//			CollectionView.__super__.render.apply(this);
//		}
	});

	return ProcessListView;
});



