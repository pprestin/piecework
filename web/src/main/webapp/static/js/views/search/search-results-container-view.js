define([ 'backbone', 'chaplin', 'models/base/collection', 'views/search/search-results-view', 'views/search/search-toolbar-view',
         'views/base/view', 'text!templates/search/search-results-container.hbs' ],
		function(Backbone, Chaplin, Collection, SearchResultsView, SearchToolbarView, View, template) {
	'use strict';

	var SearchResultsContainerView = View.extend({
		autoRender : true,
		className: 'col-lg-12',
		container: '.main-content',
	    template: template,
	    listen: {
	        'addedToDOM': '_onAddedToDOM',
	    },
	    initialize: function(model, options) {
            View.__super__.initialize.apply(this, options);
            this.subview('searchToolbarView', new SearchToolbarView({model: this.model}));
        },
        _onAddedToDOM: function() {
            setTimeout(function() {
                var $tooltips = $('.use-tooltip');
                $tooltips.tooltip();
            }, 1000);
        }
	});

	return SearchResultsContainerView;
});