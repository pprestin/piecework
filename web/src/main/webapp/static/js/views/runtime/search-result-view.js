define([ 'chaplin', 'views/base/view' ],
		function(Chaplin, View) {
	'use strict';

	var SearchResultView = View.extend({
		autoRender : true,
		className: 'search-result',
		container: '.search-results',
		tagName: 'tr',
	    render: function(options) {
	        View.__super__.render.apply(this, options);
            var html = '';
            var link = this.model.get("link");
            var formLabel = this.model.get("formLabel");
            var processInstanceLabel = this.model.get('processInstanceLabel');
            var processDefinitionLabel = this.model.get('processDefinitionLabel');
            if (formLabel != undefined)
                html += '<td><a href="' + link + '.html">' + formLabel + '</a></td>';
	        else if (processInstanceLabel != undefined)
	            html += '<td><a href="' + link + '.html">' + processInstanceLabel + '</a></td>';
	        if (processDefinitionLabel != undefined)
	            html += '<td>' + processDefinitionLabel + '</td>';
            var startTime = this.model.get('startTime');
            if (startTime != undefined) {
                var startTimeDate = new Date(startTime);
                html += '<td>' + startTimeDate.toLocaleString() + '</td>';
            }
            this.$el.html(html);
            return this;
	    }
	});

	return SearchResultView;
});