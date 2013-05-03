define([ 'chaplin', 'models/process', 'views/base/collection-view', 'views/process-detail-view', 'views/process-item-view' ], 
		function(Chaplin, Process, CollectionView, ProcessDetailView, ProcessItemView) {
	'use strict';

	var ProcessListView = CollectionView.extend({
		autoRender: true,
		className: "nav nav-list",
		container: '.sidebar-content',
		itemView: ProcessItemView,
		tagName: 'ul',
		initialize: function(options) {
			CollectionView.__super__.initialize.apply(this, options);
			Chaplin.mediator.subscribe('newProcess', this.onProcessDesign);
		},
		onProcessDesign: function() {
			// Create a new process and pass it to the detail view, but don't add it to the collection
			var process = new Process();
			this.subview('dialog', new ProcessDetailView({model: process}));
			this.listenTo(process, 'change', this.onProcessChanged);
	   	},
		onProcessChanged: function(process) {
			// The process is only added to the collection when it changes
			this.collection.set([process]);
			this.renderItem(process);
		},
		
	});

	return ProcessListView;
});



