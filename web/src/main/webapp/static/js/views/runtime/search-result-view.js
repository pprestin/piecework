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

            var task = this.model.get("task");

            var processInstanceLabel = this.model.get('processInstanceLabel');
            var processDefinitionLabel = this.model.get('processDefinitionLabel');
            var taskLabel = this.model.get("taskLabel");
            var startTime = this.model.get('startTime');

            if (task != undefined) {
                if (processInstanceLabel == undefined)
                    processInstanceLabel = task.processInstanceLabel;
                if (taskLabel == undefined)
                    taskLabel = task.taskLabel;
                if (processDefinitionLabel == undefined)
                    processDefinitionLabel = task.processDefinitionLabel;
                if (startTime == undefined)
                    startTime = task.startTime;
            }

            if (processInstanceLabel != undefined)
                html += '<td><a href="' + link + '.html">' + processInstanceLabel + '</a></td>';
	        if (taskLabel != undefined)
	            html += '<td>' + taskLabel + '</td>';
	        if (processDefinitionLabel != undefined)
	            html += '<td>' + processDefinitionLabel + '</td>';

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