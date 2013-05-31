define([ 'chaplin', 'views/base/view' ],
		function(Chaplin, View) {
	'use strict';

	var SearchResultView = View.extend({
		autoRender : true,
		className: 'search-result',
		container: '.search-results',
		tagName: 'tr',
	    render: function() {
            this.$el.html("<td>Test</td>");
	    }
	});

	return SearchResultView;
});