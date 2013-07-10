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
            var data = new FormData();
            var url = this.model.get("cancellation") + ".json";
            $.ajax({
                url : url,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                statusCode : {
                    204 : this._onDeleteSuccess,
                }
            });
	    },
	    _onBackButton: function() {
            Chaplin.mediator.publish("!router:routeByName", "form#search", this.options.params);
	    },
	    _onFileButton: function() {
	        $('.attach-file').click();
	    },
	    _onSuspendButton: function() {
	        var data = new FormData();
            var task = this.model.get("task");
            if (task !== undefined) {
                var url = this.model.get("suspension") + ".json";
                if (!task.active) {
                    url = this.model.get("activation") + ".json";
                }
                $.ajax({
                    url : url,
                    data : data,
                    processData : false,
                    contentType : false,
                    type : 'POST',
                    statusCode : {
                        204 : this._onSuspendSuccess,
                    }
                });
            }

	    },
	    _onBackToSearch: function() {
            Chaplin.mediator.publish("!router:routeByName", "form#search", this.options.params);
	    },
	    _onDeleteSuccess: function() {
	        Chaplin.mediator.publish('backToSearch');
	    },
	    _onSuspendSuccess: function() {
	        Chaplin.mediator.publish('backToSearch');
	    },
	    _onUploadSuccess: function() {
	        Chaplin.mediator.publish('showAttachments');
	        $('#comment-dialog').modal('hide');
	    },
	    _onUploadInvalid: function() {
	        alert("invalid!");
	    },
	    _onUploadFailure: function() {
	        alert("failure!");
	    },
	    _uploadAttachments: function(data) {
	        var url = this.model.get("attachment") + ".json";
            $.ajax({
                url : url,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                statusCode : {
                    200 : this._onUploadSuccess,
                    400 : this._onUploadInvalid,
                    'default' : this._onUploadFailure,
                }
            });
	    }
	});

	return FormToolbarView;
});