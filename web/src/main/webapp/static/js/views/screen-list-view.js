define([ 'chaplin', 'models/screen', 'views/base/collection-view', 'views/screen-item-view' ], 
		function(Chaplin, Screen, CollectionView, ScreenItemView) {
	'use strict';

	var ScreenListView = CollectionView.extend({
		autoRender: true,
		className: "nav",
		region: 'screen-list',
		itemView: ScreenItemView,
		tagName: 'ul',
		initialize: function(options) {
			CollectionView.__super__.initialize.apply(this, options);
		},
		render: function(options) {
			CollectionView.__super__.render.apply(this, options);
		},
		onScreenChanged: function(screen) {
			if (screen === undefined)
				return;
			
			this.collection.add(screen, {merge:true});
			this.renderItem(screen);
		},
	});

	return ScreenListView;
});