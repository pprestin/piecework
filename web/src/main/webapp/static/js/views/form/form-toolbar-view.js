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
	        'click #attach-button': '_onAttachButton',
	        'click #back-button': '_onBackButton',
	        'change #attach-file': '_onAttachFile',
	    },
	    _onAddedToDOM: function() {
	        $('title').text(window.piecework.context.applicationTitle);
	    },
	    _onAttachButton: function(event) {
            $('#attach-file').click();
	    },
	    _onAttachFile: function(event) {
	        var data = new FormData();

            $('#attach-file').each(function(index, element) {
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

            var url = this.model.get("action");
            $.ajax({
                url : url,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                statusCode : {
                    204 : this._onUploadSuccess,
                    400 : this._onUploadInvalid,
                    default : this._onUploadFailure,
                }
            });
	    },
	    _onBackButton: function() {
            Chaplin.mediator.publish("!router:routeByName", "form#search", this.options.params);
	    },
	    _onUploadSuccess: function() {
	        alert("okay!");
	    },
	    _onUploadInvalid: function() {
	        alert("invalid!");
	    },
	    _onUploadFailure: function() {
	        alert("failure!");
	    }
	});

	return FormToolbarView;
});