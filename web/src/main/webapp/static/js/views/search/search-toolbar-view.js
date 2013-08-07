define([ 'backbone', 'chaplin', 'models/history', 'models/notification', 'models/runtime/search-filter', 'views/history-view', 'views/form/notification-view', 'views/search/search-filter-view', 'views/base/view', 'text!templates/search/search-toolbar.hbs' ],
		function(Backbone, Chaplin, History, Notification, SearchFilter, HistoryView, NotificationView, SearchFilterView, View, template) {
	'use strict';

	var SearchView = View.extend({
		autoRender : true,
		container: '.main-navbar',
		tagName: 'div',
		className: 'main-toolbar',
	    template: template,
	    events: {
	        'click #activate-button': '_onActivateButton',
	        'click #assign-button': '_onAssignButton',
	        'click #delete-button': '_onDeleteButton',
	        'click #history-dialog-button': '_onHistoryButton',
	        'click #suspend-button': '_onSuspendButton',
	        'shown.bs.modal #assign-dialog': '_onShowAssignDialog',
	        'submit form': '_onFormSubmit',
	        'typeahead:autocompleted': '_onTypeaheadAutocompleted',
	        'typeahead:selected': '_onTypeaheadSelected',
	    },
	    listen: {
            'addedToDOM': '_onAddedToDOM',
            'resultSelected mediator': '_onResultSelected',
            'resultUnselected mediator': '_onResultUnselected',
            'search mediator': '_onSearch',
            'searched mediator': '_onSearched',
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
        _onActivateButton: function() {
            var selected = this.model.get("selected");
            if (selected == null)
                return;

            var toolbar = this;
            var task = selected.get('task');
            if (task != null) {
                if (task.active)
                    return;

                var url = selected.get("activation") + ".json";
                var data = $('#activate-reason').serialize();
                $.post( url, data,
                    function(data, textStatus, jqXHR) {
                        $('#activate-dialog').modal('hide');
                        Chaplin.mediator.publish("search", {status:"open"});
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#activate-dialog > .modal-body', model: notification}));
                 });
            }
        },
        _onAssignButton: function() {
            var assignee = this.$el.find('#assigneeId').val();

            var selected = this.model.get("selected");
            if (selected == null)
                return;

            var toolbar = this;
            var task = selected.get('task');
            if (task != null) {
                if (!task.active) {
                    var notification = new Notification({title: 'Invalid state', message: 'Cannot assign a suspended task', permanent: true})
                    toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog > .modal-body', model: notification}));
                }

                var url = selected.get("assignment") + ".json";
                var data = '{ "assignee": "' + assignee + '"}';
                $.ajax({
                    type: 'POST',
                    url: url,
                    data: data,
                    success: function(data, textStatus, jqXHR) {
                        $('#assign-dialog').modal('hide');
                        Chaplin.mediator.publish("search", {status:"open"});
                    },
                    contentType: "application/json"
                }).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog .modal-body', model: notification}));
                 });
            }
        },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onDeleteButton: function() {
            var data = null;
            var selected = this.model.get("selected");
            if (selected == null)
                return;

            var toolbar = this;
            var data = $('#delete-reason').serialize();
            var task = selected.get('task');
            if (task != null) {
                var url = selected.get("cancellation") + ".json";
                $.post(url, data,
                    function(data, textStatus, jqXHR) {
                        $('#delete-dialog').modal('hide');
                        Chaplin.mediator.publish("search", {status:"open"});
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                      var explanation = $.parseJSON(jqXHR.responseText);
                      var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                      toolbar.subview('notificationView', new NotificationView({container: '#delete-dialog > .modal-body', model: notification}));
                });
            }
        },
	    _onFormSubmit: function(event) {
	        event.preventDefault();
	        event.stopPropagation();
	        var status = this.$(':checkbox[name="processStatus"]:checked').val();
            var keyword = this.$('#keyword').val();
            var processDefinitionKey = $(':checkbox[name="processDefinitionKey"]:checked').val();

            var data = {};
            if (keyword !== undefined && keyword != '')
                data['keyword'] = keyword;
            if (status !== undefined && status != '')
                data['processStatus'] = status;
            if (processDefinitionKey !== undefined && processDefinitionKey != '')
                data['processDefinitionKey'] = processDefinitionKey;

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
            }).fail(function(jqXHR, textStatus, errorThrown) {
                var explanation = $.parseJSON(jqXHR.responseText);
                var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                toolbar.subview('historyView', new NotificationView({container: '#history-dialog > .modal-body', model: notification}));
            });
	    },
        _onResultSelected: function(selected) {
            if (selected == null)
                return;

            var task = selected.get('task');
            if (task.active) {
                this.$el.find('#activate-dialog-button').addClass('hide');
                this.$el.find('#suspend-dialog-button').removeClass('hide');
            } else if (task.taskStatus == 'Suspended') {
                this.$el.find('#activate-dialog-button').removeClass('hide');
                this.$el.find('#suspend-dialog-button').addClass('hide');
            } else {
                this.$el.find('#activate-dialog-button').addClass('hide');
                this.$el.find('#suspend-dialog-button').addClass('hide');
            }

            if (task.taskStatus != 'Cancelled' && task.taskStatus != 'Complete')
                this.$el.find('.incomplete-selected-result-btn').removeClass('hide');
            else
                this.$el.find('.incomplete-selected-result-btn').addClass('hide');

            this.$el.find('.selected-result-btn').removeClass('hide');
            this.model.set("selected", selected);
        },
        _onResultUnselected: function(result) {
            this.$el.find('.selected-result-btn').addClass('hide');
            this.$el.find('.incomplete-selected-result-btn').addClass('hide');
            this.$el.find('#activate-dialog-button').addClass('hide');
            this.$el.find('#suspend-dialog-button').addClass('hide');
            this.$el.find('#delete-dialog-button').addClass('hide');
            this.model.unset("selected");
        },
	    _onSearch: function(data) {
	        $('#instanceSearchButton').button('loading');
	    },
	    _onSearched: function(data) {
	        $('#instanceSearchButton').button('reset');
	    },
	    _onShowAssignDialog: function() {
            $('#assignee').typeahead({
                                        name: 'person-lookup',
                                        remote: {
                                            url: '/piecework/secure/person.json?displayNameLike=%QUERY',
                                            filter: function(parsedResponse) {
                                                var list = parsedResponse.list;
                                                var data = new Array();
                                                for (var i=0;i<list.length;i++) {
                                                    var person = list[i];
                                                    data.push({value: person.userId, displayName: person.displayName, tokens: [ person.displayName ]});
                                                }
                                                return data;
                                            }
                                        },
                                        valueKey: 'displayName'
                                     });
	    },
	    _onSuspendButton: function() {
	        var selected = this.model.get("selected");
	        if (selected == null)
	            return;
            var toolbar = this;
            var data = $('#suspend-reason').serialize();
            var task = selected.get('task');
            if (task != null) {
                var url = selected.get("suspension") + ".json";
                if (!task.active) {
                    url = selected.get("activation") + ".json";
                }
                $.post( url, data,
                    function(data, textStatus, jqXHR) {
                        $('#suspend-dialog').modal('hide');
                        Chaplin.mediator.publish("search", {status:"open"});
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#suspend-dialog > .modal-body', model: notification}));
                 });
            }
	    },
	    _onTypeaheadAutocompleted: function(obj, datum) {
	        this.$el.find('#assigneeId').val(datum.value);
            this.$el.find("#assign-button").removeAttr('disabled');
	    },
	    _onTypeaheadSelected: function(obj, datum) {
            this.$el.find('#assigneeId').val(datum.value);
            this.$el.find("#assign-button").removeAttr('disabled');
	    }
	});

	return SearchView;
});