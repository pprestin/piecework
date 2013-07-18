define([ 'backbone', 'chaplin', 'models/history', 'models/runtime/search-filter', 'views/history-view', 'views/search/search-filter-view', 'views/base/view', 'text!templates/search/search-toolbar.hbs' ],
		function(Backbone, Chaplin, History, SearchFilter, HistoryView, SearchFilterView, View, template) {
	'use strict';

	var SearchView = View.extend({
		autoRender : true,
		container: '.main-navbar',
		tagName: 'div',
		className: 'main-toolbar',
	    template: template,
	    events: {
	        'click #delete-button': '_onDeleteButton',
	        'click #history-dialog-button': '_onHistoryButton',
	        'click #suspend-button': '_onSuspendButton',
	        'submit form': '_onFormSubmit',
	    },
	    listen: {
            'addedToDOM': '_onAddedToDOM',
            'resultSelected mediator': '_onResultSelected',
            'resultUnselected mediator': '_onResultUnselected',
            'search mediator': '_onSearch',
	    },
	    render: function() {
            View.__super__.render.apply(this);

            var statusFilter = new SearchFilter({
                selector: 'parameters',
                options: [
                    { 'id': "statusOpen", 'label': "Open", 'key': "processStatus", 'value': "open", 'default': true },
                    {id: "statusComplete", label: "Complete", key: "processStatus", value: 'complete'},
                    {id: "statusCancelled", label: "Deleted", key: "processStatus", value: 'cancelled'},
                    {id: "statusSuspended", label: "Suspended", key: "processStatus", value: 'suspended'},
                    {id: "statusAny", label: "Any status", key: "processStatus", value: 'all' }
                ],
                results: this.model
            });

            var processFilter = new SearchFilter({
                selector: 'parameters',
                key: 'definitions',
                results: this.model
            });

            var statusFilterView = this.subview('statusFilterContainer', new SearchFilterView({container: '.status-filter-container', model: statusFilter}))
            var processFilterView = this.subview('processFilterContainer', new SearchFilterView({container: '.process-filter-container', model: processFilter}))

            statusFilterView.render();
            processFilterView.render();

            var $statusFilterContainer = this.$el.find('.status-filter-container');
            var $processFilterContainer = this.$el.find('.process-filter-container');

            $statusFilterContainer.append(statusFilterView.$el);
            $processFilterContainer.append(processFilterView.$el);

            return this;
        },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onDeleteButton: function() {
            var data = null;
            var selected = this.model.get("selected");
            if (selected == null)
                return;

            var data = $('#delete-reason').serialize();
            var task = selected.get('task');
            if (task != null) {
                var url = selected.get("cancellation") + ".json";
                $.post(url, data,
                    function(data, textStatus, jqXHR) {
                        Chaplin.mediator.publish("search", {status:"open"});
                    }
                ).fail(function() {  });
            }
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
	    _onHistoryButton: function(event) {
	        var selected = this.model.get("selected");
            if (selected == null)
                return;

            var url = selected.get('history') + '.json';
            var toolbar = this;
            $.ajax({
                url : url,
                contentType : 'application/json',
                type : 'GET',
                success : function(data, textStatus, jqXHR) {
                    var history = new History(data);
                    toolbar.subview('historyView', new HistoryView({model: history}));
                }
            });
	    },
        _onResultSelected: function(result) {
            this.$el.find('.selected-result-btn').removeClass('hide');
            this.model.set("selected", result);
        },
        _onResultUnselected: function(result) {
            this.$el.find('.selected-result-btn').addClass('hide');
            this.model.unset("selected");
        },
	    _onSearch: function(data) {
            var queryString = '';
            if (data != undefined) {
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
            }

            var pattern = window.location.pathname + '?' + queryString; //'piecework/secure/form?' + queryString;
            Chaplin.mediator.publish('!router:changeURL', pattern);
	    },
	    _onSuspendButton: function() {
	        var selected = this.model.get("selected");
	        if (selected == null)
	            return;

            var data = null;
            var task = selected.get('task');
            if (task != null) {
                var url = selected.get("suspension") + ".json";
                if (!task.active) {
                    url = selected.get("activation") + ".json";
                }
                $.post( url, data,
                    function(data, textStatus, jqXHR) {
                        Chaplin.mediator.publish("search", {status:"open"});
                    }
                ).fail(function() {  });
            }
	    },
	});

	return SearchView;
});