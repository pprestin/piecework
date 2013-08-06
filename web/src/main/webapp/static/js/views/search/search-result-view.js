define([ 'chaplin', 'views/base/view', 'text!templates/search/search-result.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var SearchResultView = View.extend({
		autoRender : true,
		className: 'search-result',
		container: '.search-results',
		tagName: 'tr',
		template: template,
		events: {
		    'change .result-checkbox': '_onChangeResultSelection'
		},
	    _onChangeResultSelection: function(event) {
	        if (event.target.checked) {
                Chaplin.mediator.publish('resultSelected', this.model);
	        } else {
	            Chaplin.mediator.publish('resultUnselected', this.model);
	        }
	    }
	});

	return SearchResultView;
});