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
            'addedToDOM': '_onAddedToDOM',
            'search mediator': '_onSearch',
	    },
	    render: function(options) {
            View.__super__.render.apply(this, options);
	        this.$el.attr('action', '');
	        this.$el.attr('method', 'GET');
	        return this;
	    },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onFormSubmit: function(event) {
	        event.preventDefault();
	        event.stopPropagation();
            Chaplin.mediator.publish('search');
            return false;
	    },
	    _onSearch: function() {
	        var status = this.$(':checkbox[name="processStatus"]:checked').val();
            var keyword = this.$('#keyword').val();
            var process = this.$(':checkbox[name="process"]:checked').val();

            if (status == '')
                status = 'all';
            if (process == '')
                process = 'all';
            if (keyword == '')
                keyword = 'none';

            var pattern = 'process/' + process + '/status/' + status + '/keyword/' + keyword;
            Chaplin.mediator.publish('!router:route', pattern);
	    }
	});

	return SearchView;
});