define([ 'chaplin', 'views/base/view', 'text!templates/runtime/search.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var SearchView = View.extend({
		autoRender : true,
		className: 'search-criteria',
		container: '.main-toolbar',
		tagName: 'ul',
	    template: template,
	});

	return SearchView;
});