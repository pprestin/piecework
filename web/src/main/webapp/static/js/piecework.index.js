requirejs.config({
    //By default load any module IDs from js/vendor
    baseUrl: 'static/js',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
    	backbone: 'vendor/backbone',
    	bootstrap: '../lib/bootstrap/js/bootstrap',
    	jquery: 'vendor/jquery',
        underscore: 'vendor/underscore'
    },
    shim: {
    	'backbone':{deps: ['underscore']},
        'bootstrap':{deps: ['jquery']}
    }
});

// Start the main app logic.
requirejs([
	'jquery',
	'underscore',
	'backbone',
	'bootstrap'
], function ($, _, Backbone, bootstrap) {
	
	var Process = Backbone.Model.extend({
		defaults : function() {
			return {
				label : "",
				summary : "",
				created: ""
			};
		},
	});
	
	var ProcessList = Backbone.Collection.extend({
		model: Process,
		comparator: 'created'
	});
	
	var Processes = new ProcessList;
	
	var ProcessView = Backbone.View.extend({
		tagName: "li",
		
		initialize: function() {
			this.listenTo(this.model, 'change', this.render);
		    this.listenTo(this.model, 'destroy', this.remove);
		},
		
		clear: function() {
			this.model.destroy();
		},
		
		close: function() {
			var value = this.input.val();
			if (!value) {
				this.clear();
			} else {
		        this.model.save({title: value});
		        this.$el.removeClass("editing");
			}
		},
		    
		edit: function() {
			this.$el.addClass("editing");
			this.input.focus();
		},
		
		render: function() {
			this.$el.html(this.model.label);
			this.input = this.$('.edit');
			return this;
	    },
	    
	    updateOnEnter: function(e) {
	    	if (e.keyCode == 13) this.close();
	    },
	      
	});
	
	var ProcessDescriptionDialog = Backbone.View.extend({

		events: {
			'click .btn-primary': 'accept',
			'shown': 'shown'
		},
		
		accept: function() {
			this.model.label = this.labelInput.val();
			this.model.summary = this.summaryInput.val();
			this.created = new Date();
			
			// Add the new process to the list on the left
			this.app.add(this.model);
			
			// Hide the dialog
			this.$el.modal('hide');
		},
		
		render: function() {
			this.model = new Process();
			this.labelInput = this.$('.new-process-label');
			this.summaryInput = this.$('.new-process-summary');
			
			this.labelInput.val('');
			this.summaryInput.val('');
			
			return this;
		},
		
		shown: function() {
			this.render();
		}
		
	});
	
	var AppView = Backbone.View.extend({
		
		add: function(process) {
			var view = new ProcessView({model: process});
			this.$el.append(view.render().el);
		},
		
		initialize: function() {
			this.processDescriptionDialog = new ProcessDescriptionDialog({el: $('#process-description-dialog')});
			this.processDescriptionDialog.app = this;
		}
		
	});
	
	

	$(function() {
		var app = new AppView({el: $('#process-list')});
		
		
	});
});
