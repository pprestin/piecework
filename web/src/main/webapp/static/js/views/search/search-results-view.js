define([ 'chaplin', 'views/base/collection-view', 'views/search/search-result-view' ],
		function(Chaplin, CollectionView, SearchResultView) {
	'use strict';

	var SearchResultsView = CollectionView.extend({
		autoRender: true,
		container: '.search-results',
		itemView: SearchResultView,
		tagName: 'tbody',
		listen: {
		    'resultSelected mediator': '_onResultSelected',
            'resultUnselected mediator': '_onResultUnselected',
		    'search mediator': '_onSearch',
		},
		_onResultSelected: function(result) {
            var $checkbox = this.$el.find('.result-checkbox').not(":checked");
            $checkbox.attr('disabled', true);
        },
        _onResultUnselected: function(result) {
            var $checkbox = this.$el.find('.result-checkbox');
            $checkbox.removeAttr('disabled');
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