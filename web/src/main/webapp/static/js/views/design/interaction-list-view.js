define([ 'chaplin', 'models/design/interaction', 'views/base/collection-view', 'views/design/interaction-detail-view', 'views/design/screen-list-view' ],
		function(Chaplin, Interaction, CollectionView, InteractionDetailView, ScreenListView) {
	'use strict';

	var InteractionListView = CollectionView.extend({
		autoRender: true,
		className: "interaction-list nav",
		container: '.user-interaction-content',
		itemView: InteractionDetailView,
		tagName: 'ul',
//		onScreenChanged: function(screen) {
//			if (screen === undefined)
//				return;
//			
//			this.collection.add(screen, {merge:true});
//			this.renderItem(screen);
//		},
	});

	return InteractionListView;
});