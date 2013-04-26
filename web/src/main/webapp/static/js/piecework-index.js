requirejs.config({
    baseUrl: 'static/js',
    paths: {
    	backbone: 'vendor/backbone-amd',
    	bootstrap: '../lib/bootstrap/js/bootstrap',
    	handlebars: 'vendor/handlebars',
    	jquery: 'vendor/jquery',
        underscore: 'vendor/underscore-amd'
    },
    shim: {
    	'backbone':{deps: ['underscore']},
        'bootstrap':{deps: ['jquery']},
        'underscore':{deps: []}
    }
});

requirejs([
	'jquery',
	'underscore',
	'backbone',
	'bootstrap',
	'models/process',
	'models/process-list',
	'views/process',
	'views/process-description-dialog'
], function ($, _, Backbone, bootstrap, Process, ProcessList, ProcessView, ProcessDialog) {
	
		
	var AppView = Backbone.View.extend({
		
		add: function(process) {
			var view = new ProcessView({model: process});
			this.$el.append(view.render().el);
		},
		
		initialize: function() {
			this.processDescriptionDialog = new ProcessDialog({el: $('#process-description-dialog')});
			this.processDescriptionDialog.app = this;
		}
		
	});

	$(function() {
		var processes = new ProcessList;
		var app = new AppView({el: $('#process-list')});
		
		
	});
});
