define([ 'backbone', 'chaplin', 'models/base/collection', 'views/search/search-results-view', 'views/search/search-toolbar-view',
         'views/base/view', 'text!templates/search/search-results-container.hbs' ],
		function(Backbone, Chaplin, Collection, SearchResultsView, SearchToolbarView, View, template) {
	'use strict';

	var SearchResultsContainerView = View.extend({
		autoRender : true,
		container: '.main-content',
	    template: template,
	    initialize: function(model, options) {
            View.__super__.initialize.apply(this, options);
            this.subview('searchToolbarView', new SearchToolbarView({model: this.model}));
//            var resultsCollection = this.model.get('collection');
        },
        render: function(options) {
            View.__super__.render.apply(this, options);

//            var list = this.model.get("list");
//            var link = this.model.get("link");
//            if (/.html$/.test(link))
//                link = link.substring(0, url.length - 5);
//            var ResultsCollection = Collection.extend({
//                url: link,
//                parse: function(response, options) {
//                    return response.list;
//                },
//            });
//            this.subview('searchResultsView', new SearchResultsView({collection: new ResultsCollection(list)}));
            return this;
        }
	});

	return SearchResultsContainerView;
});