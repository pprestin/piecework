define([ 'backbone', 'chaplin', 'views/base/view', 'text!templates/form/form-toolbar.hbs' ],
		function(Backbone, Chaplin, View, template) {
	'use strict';

	var FormToolbarView = View.extend({
		autoRender : true,
		container: '.main-navbar',
		tagName: 'div',
		className: 'main-toolbar narrow',
	    template: template,
	    events: {
	        'click #activate-button': '_onActivateButton',
	        'click #attachments-button': '_onAttachmentsButton',
	        'click #attach-button': '_onAttachComment',
	        'click #back-button': '_onBackButton',
	        'click #delete-button': '_onDeleteButton',
	        'click #file-button': '_onFileButton',
	        'click #suspend-button': '_onSuspendButton',
	        'change .attach-file': '_onAttachFile',
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
	    _onSuspendSuccess: function() {
	        Chaplin.mediator.publish('backToSearch');
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