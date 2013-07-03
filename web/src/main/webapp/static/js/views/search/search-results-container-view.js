define([ 'backbone', 'chaplin', 'views/base/view', 'text!templates/search/search-results-container.hbs' ],
		function(Backbone, Chaplin, View, template) {
	'use strict';

	var SearchResultsContainerView = View.extend({
		autoRender : true,
		container: '.main-content',
	    template: template,
	});

	return SearchResultsContainerView;
});