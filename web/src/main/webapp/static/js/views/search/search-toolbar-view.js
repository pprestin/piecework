define([ 'backbone', 'chaplin', 'views/base/view', 'text!templates/search/search-toolbar.hbs' ],
		function(Backbone, Chaplin, View, template) {
	'use strict';

	var SearchView = View.extend({
		autoRender : true,
		container: '.main-navbar',
		tagName: 'div',
		className: 'main-toolbar',
	    template: template,
	    events: {
	        'submit form': '_onFormSubmit',
	    },
	    listen: {
            'addedToDOM': '_onAddedToDOM',
            'search mediator': '_onSearch',
	    },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onFormSubmit: function(event) {
	        event.preventDefault();
	        event.stopPropagation();
	        var status = this.$(':checkbox[name="processStatus"]:checked').val();
            var keyword = this.$('#keyword').val();
            var processDefinitionKey = $(':checkbox[name="processDefinitionKey"]:checked').val();

            var data = { status : status, keyword : keyword, processDefinitionKey : processDefinitionKey };

            Chaplin.mediator.publish('search', data);
            return false;
	    },
	    _onSearch: function(data) {
            var queryString = '';
            if (data.status != undefined && data.status != '')
                queryString += 'processStatus=' + data.status;
            if (data.processDefinitionKey != undefined && data.processDefinitionKey != '') {
                if (queryString.length > 0)
                    queryString += '&';
                queryString += 'processDefinitionKey=' + data.processDefinitionKey;
            }
            if (data.keyword != undefined && data.keyword != '') {
                if (queryString.length > 0)
                    queryString += '&';
                queryString += 'keyword=' + data.keyword;
            }

            var pattern = window.location.pathname + '?' + queryString; //'piecework/secure/form?' + queryString;
            Chaplin.mediator.publish('!router:changeURL', pattern);
	    }
	});

	return SearchView;
});