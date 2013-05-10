define([ 'chaplin', 'models/process', 'views/base/collection-view', 'views/process-detail-view', 'views/process-item-view' ], 
		function(Chaplin, Process, CollectionView, ProcessDetailView, ProcessItemView) {
	'use strict';

	var ProcessListView = CollectionView.extend({
		autoRender: true,
		className: "nav nav-list",
		region: 'sidebar-content',
		itemView: ProcessItemView,
		tagName: 'ul',
		initialize: function(options) {
			CollectionView.__super__.initialize.apply(this, options);
	   		if (options.test === undefined)
	   			this.collection.fetch();
		},
		onProcessDefinitionKeyChanged: function(process, processDefinitionKey) {
			this.collection.fetch();
		},
	});

	return ProcessListView;
});



