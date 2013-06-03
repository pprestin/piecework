define([ 'backbone', 'chaplin', 'views/base/view', 'text!templates/runtime/search.hbs' ],
		function(Backbone, Chaplin, View, template) {
	'use strict';

	var SearchView = View.extend({
		autoRender : true,
		container: '.main-toolbar',
		tagName: 'form',
		id: 'search-form',
	    template: template,
	    events: {
	        'submit': '_onFormSubmit',
	    },
	    listen: {

	    },
	    _onFormSubmit: function(event) {
	        event.preventDefault();
	        event.stopPropagation();
	        var keyword = this.$('#keyword').val();
            Chaplin.mediator.publish('!router:route', 'search:' + keyword);
            return false;
	    },
	});

	return SearchView;
});