define([ 'chaplin', 'views/base/view' ],
		function(Chaplin, View) {
	'use strict';

	var SearchResultView = View.extend({
		autoRender : true,
		className: 'search-result',
		container: '.search-results',
		tagName: 'tr',
		events: {
		    'change .result-checkbox': '_onChangeResultSelection'
		},
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

            html += '<td><input type="checkbox" class="result-checkbox"/></td>';

            if (processInstanceLabel != undefined)
                html += '<td><a href="' + link + '.html" target="_blank">' + processInstanceLabel + '</a></td>';
	        if (taskLabel != undefined)
	            html += '<td class="hide-narrow">' + taskLabel + '</td>';
	        if (processDefinitionLabel != undefined)
	            html += '<td class="hide-narrow">' + processDefinitionLabel + '</td>';

            if (startTime != undefined) {
                var startTimeDate = new Date(startTime);
                html += '<td>' + startTimeDate.toLocaleString() + '</td>';
            }
            this.$el.html(html);
            return this;
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