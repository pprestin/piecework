define([ 'chaplin', 'views/base/collection-view', 'views/runtime/search-result-view' ],
		function(Chaplin, CollectionView, SearchResultView) {
	'use strict';

	var SearchResultsView = CollectionView.extend({
		autoRender: true,
		container: '.search-results',
		itemView: SearchResultView,
		tagName: 'tbody',
		listen: {
		    'search mediator': '_onSearch',
		},
		_onSearch: function(data) {
		    var clean = {};
		    if (data.keyword !== undefined && data.keyword != 'none')
                clean['keyword'] = data.keyword;
            if (data.status !== undefined && data.status != 'undefined')
                clean['processStatus'] = data.status;
            if (data.processDefinitionKey !== undefined && data.processDefinitionKey != 'all')
                clean['processDefinitionKey'] = data.processDefinitionKey;

            this.collection.fetch({data: clean});
		}
	});

	return SearchResultsView;
});