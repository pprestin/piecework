define([ 'chaplin', 'views/base/collection-view', 'views/runtime/search-result-view' ],
		function(Chaplin, CollectionView, SearchResultView) {
	'use strict';

	var SearchResultsView = CollectionView.extend({
		autoRender: true,
		container: '.search-results',
		itemView: SearchResultView,
		tagName: 'tbody'
	});

	return SearchResultsView;
});