define([ 'backbone', 'chaplin', 'views/base/view', 'text!templates/form/form-toolbar.hbs' ],
		function(Backbone, Chaplin, View, template) {
	'use strict';

	var FormToolbarView = View.extend({
		autoRender : true,
		container: '.main-navbar',
		tagName: 'div',
		className: 'main-toolbar',
	    template: template,
	    events: {
	        'click #activate-button': '_onActivateButton',
	        'click #assign-button': '_onAssignButton',
	        'click #attachments-button': '_onAttachmentsButton',
	        'click #attach-button': '_onAttachComment',
	        'click #delete-button': '_onDeleteButton',
	        'click #file-button': '_onFileButton',
	        'click #suspend-button': '_onSuspendButton',
	        'click .candidate-assignee': '_onAssignToCandidate',
	        'change .attach-file': '_onAttachFile',
	        'shown.bs.modal #assign-dialog': '_onShowAssignDialog',
	        'typeahead:autocompleted': '_onTypeaheadAutocompleted',
            'typeahead:selected': '_onTypeaheadSelected',
	    },
	    listen: {
	        'attachmentCountChanged mediator': '_onAttachmentCountChanged',
	        'backToSearch mediator': '_onBackToSearch',
	    },
	    _onActivateButton: function() {
            var toolbar = this;
            var task = this.model.get("task");
            if (task != null) {
                if (task.active)
                    return;

                var url = this.model.get("activation") + ".json";
                var data = $('#activate-reason').serialize();
                $.post( url, data,
                    function(data, textStatus, jqXHR) {
                        $('#activate-dialog').modal('hide');
                        Chaplin.mediator.publish("search", {processStatus: "open"});
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#activate-dialog > .modal-body', model: notification}));
                 });
            }
        },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onAssignButton: function() {
            var assignee = this.$el.find('#assigneeId').val();

            var toolbar = this;
            var task = this.model.get("task");
            if (task != null) {
                if (!task.active) {
                    var notification = new Notification({title: 'Invalid state', message: 'Cannot assign a suspended task', permanent: true})
                    toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog > .modal-body', model: notification}));
                }

                var url = this.model.get("assignment") + ".json";
                var data = '{ "assignee": "' + assignee + '"}';
                $.ajax({
                    type: 'POST',
                    url: url,
                    data: data,
                    success: function(data, textStatus, jqXHR) {
                        $('#assign-dialog').modal('hide');
                        window.location.reload();
                    },
                    contentType: "application/json"
                }).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog .modal-body', model: notification}));
                 });
            }
        },
        _onAssignToCandidate: function(event) {
            var assignee = event.target.id;

            var toolbar = this;
            var task = this.model.get("task");
            if (task != null) {
                if (!task.active) {
                    var notification = new Notification({title: 'Invalid state', message: 'Cannot assign a suspended task', permanent: true})
                    toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog > .modal-body', model: notification}));
                }

                var url = this.model.get("assignment") + ".json";
                var data = '{ "assignee": "' + assignee + '"}';
                $.ajax({
                    type: 'POST',
                    url: url,
                    data: data,
                    success: function(data, textStatus, jqXHR) {
                        window.location.reload();
                    },
                    contentType: "application/json"
                }).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#assign-dialog .modal-body', model: notification}));
                 });
            }
        },
	    _onAttachmentCountChanged: function(count) {
	        this.$el.find('#attachment-count').text(count);
	    },
	    _onAttachmentsButton: function(event) {
            Chaplin.mediator.publish('showAttachments');
	    },
	    _onAttachComment: function(event) {
            var data = new FormData();

            var comment = $('#attach-comment').val();
            data.append("comment", comment);

            this._uploadAttachments(data);
	    },
	    _onAttachFile: function(event) {
	        var data = new FormData();

            $('.attach-file').each(function(index, element) {
                var name = element.name;
                if (name == undefined || name == null || name == '')
                    return;

                if (element.files !== undefined && element.files != null) {
                    $.each(element.files, function(fileIndex, file) {
                        if (file != null)
                            data.append(name, file);
                    });
                }
            });

            this._uploadAttachments(data);
	    },
	    _onDeleteButton: function() {
            var toolbar = this;
            var data = $('#delete-reason').serialize();
            var task = this.model.get('task');
            if (task != null) {
                var url = this.model.get("cancellation") + ".json";
                $.post(url, data,
                    function(data, textStatus, jqXHR) {
                        $('#delete-dialog').modal('hide');
                        Chaplin.mediator.publish('backToSearch');
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                      var explanation = $.parseJSON(jqXHR.responseText);
                      var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                      toolbar.subview('notificationView', new NotificationView({container: '#delete-dialog > .modal-body', model: notification}));
                });
            }
	    },
	    _onBackButton: function() {
	        var root = this.model.get("root");
            Chaplin.mediator.publish("!router:route", root);
	    },
	    _onFileButton: function() {
	        $('.attach-file').click();
	    },
	    _onSuspendButton: function() {
            var toolbar = this;
            var data = $('#suspend-reason').serialize();
            var task = this.model.get("task");
            if (task != null) {
                var url = this.model.get("suspension") + ".json";
                if (!task.active) {
                    url = this.model.get("activation") + ".json";
                }
                $.post( url, data,
                    function(data, textStatus, jqXHR) {
                        $('#suspend-dialog').modal('hide');
                        Chaplin.mediator.publish('backToSearch');
                    }
                ).fail(function(jqXHR, textStatus, errorThrown) {
                     var explanation = $.parseJSON(jqXHR.responseText);
                     var notification = new Notification({title: explanation.message, message: explanation.messageDetail, permanent: true})
                     toolbar.subview('notificationView', new NotificationView({container: '#suspend-dialog > .modal-body', model: notification}));
                 });
            }
	    },
	    _onBackToSearch: function() {
            var root = this.model.get("root") + ".html";
            Chaplin.mediator.publish("!router:route", root);
	    },
	    _onDeleteSuccess: function() {
	        Chaplin.mediator.publish('backToSearch');
	    },
        _onShowAssignDialog: function() {
            var url = this.model.get("link");
            var indexOf = url.indexOf('form');
            url = url.substring(0,indexOf) + 'person.json?displayNameLike=%QUERY';
            $('#assignee').typeahead({
                                        name: 'person-lookup',
                                        remote: {
                                            url: url,
                                            filter: function(parsedResponse) {
                                                var list = parsedResponse.list;
                                                var data = new Array();
                                                if (list != null) {
                                                    for (var i=0;i<list.length;i++) {
                                                        var person = list[i];
                                                        data.push({value: person.userId, displayName: person.displayName, tokens: [ person.displayName ]});
                                                    }
                                                }
                                                return data;
                                            }
                                        },
                                        valueKey: 'displayName'
                                     });
        },
	    _onSuspendSuccess: function() {
	        Chaplin.mediator.publish('backToSearch');
	    },
        _onTypeaheadAutocompleted: function(obj, datum) {
            this.$el.find('#assigneeId').val(datum.value);
            this.$el.find("#assign-button").removeAttr('disabled');
        },
        _onTypeaheadSelected: function(obj, datum) {
            this.$el.find('#assigneeId').val(datum.value);
            this.$el.find("#assign-button").removeAttr('disabled');
        },
	    _uploadAttachments: function(data) {
	        var url = this.model.get("attachment") + ".json";
            $.ajax({
                url : url,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                success : function() {
                    Chaplin.mediator.publish('refreshAttachments');
                    $('#comment-dialog').modal('hide');
                }
            }).fail(function() {

            });
	    }
	});

	return FormToolbarView;
});