define([ 'chaplin', 'views/base/view' ],
		function(Chaplin, View) {
	'use strict';

	var SearchResultView = View.extend({
		autoRender : true,
		className: 'search-result',
		container: '.search-results',
		tagName: 'tr',
	    render: function() {
            var html = '';
            var link = this.model.get("link");
            var processInstanceLabel = this.model.get('processInstanceLabel');
            var processDefinitionLabel = this.model.get('processDefinitionLabel');
	        if (processInstanceLabel != undefined)
	            html += '<td><a href="' + link + '.html">' + processInstanceLabel + '</a></td>';
	        if (processDefinitionLabel != undefined)
	            html += '<td>' + processDefinitionLabel + '</td>';
            var execution = this.model.get('execution');
            if (execution != undefined) {
                var startTime = execution['startTime'];
                if (startTime != undefined) {
                    var startTimeDate = new Date(startTime);
                    html += '<td>' + startTimeDate.toLocaleString() + '</td>';
                }
            }
            this.$el.html(html);
	    }
	});

	return SearchResultView;
});