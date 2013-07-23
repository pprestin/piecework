define([ 'chaplin', 'views/base/collection-view', 'views/form/notification-view', 'views/search/search-result-view' ],
		function(Chaplin, CollectionView, NotificationView, SearchResultView) {
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
		    if (data.keyword !== undefined && data.keyword != 'none' && data.keyword != '')
                clean['keyword'] = data.keyword;
            if (data.processStatus !== undefined && data.processStatus != 'undefined' && data.processStatus != '')
                clean['processStatus'] = data.processStatus;
            if (data.processDefinitionKey !== undefined && data.processDefinitionKey != 'all' && data.processDefinitionKey != '')
                clean['processDefinitionKey'] = data.processDefinitionKey;

            this.collection.fetch({data: clean,
                success: function(model, response, options) {
                    Chaplin.mediator.publish('searched', response);
                },
                error: function(model, response, options) {
                    var explanation = response;
                    var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                    new NotificationView({container: '.main-content', model: notification});
                }
            });
		}
	});

	return SearchResultsView;
});