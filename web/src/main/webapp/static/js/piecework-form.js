requirejs.config({
    baseUrl: 'static/js',
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
	
	var FormFieldView = Backbone.View.extend({
  		
  		render: function() {
  			var selector = '#field-' + this.model.typeAttr + '-template';
  			var source   = $(selector).html();
  			if (source != null) {
  				var template = Handlebars.compile(source);
  				this.$el.attr('class', 'field');
  				this.$el.html(template(this.model));
  			}
  		    return this;
  		}
  	
  	});
  		
  	var SectionView = Backbone.View.extend({	  	
  		
			render: function() {
				var selector = '#section-template';
	  		var source   = $(selector).html();
		  		if (source != null) {
  		  		var template = Handlebars.compile(source);
  				this.$el.html(template(this.model));	
		  		}
				_.each(this.model.fields, function (field) {
		            $(this.el).find('.section-content').append(new FormFieldView({model:field}).render().el);
		        }, this);
  		    return this;
  		}
  	
  	});
  	
  	var FormView = Backbone.View.extend({	  	
  		
  		events: {
  			'submit' : 'onSubmit'
  		},
  		
  		onSubmit: function(event) {
  			
  			
  		},
  	
			render: function() {
				$content = $(this.el).find('.form-content-placeholder');
				if (this.model != null) {
					_.each(this.model.sections, function (section) {
						$content.append(new SectionView({model:section}).render().el);
			        }, this);
				}
  		    return this;
  		}
  	
  	});
  		  	
	var DialogView = Backbone.View.extend({	  	
  		
			render: function() {
	  		var source   = $('#dialog-template').html();
		  		if (source != null) {
  		  		var template = Handlebars.compile(source);
  				this.$el.html(template(this.model));	
		  		}
  		    return this;
  		}
  	
  	});
  	
  	var TaskToolbarView = Backbone.View.extend({
  		
		render: function() {
			
			if (this.model != null) {
				if (this.model.sections != null && this.model.sections.length > 0) {
					
				}
				
				_.each(this.model.dialogs, function(dialog) {
					$(this.el).append(new DialogView({model:dialog}).render().el);
				}, this);
				
				$(this.el).find(':button').button();
			}
  		    return this;
  		}
  		
  	});
  	
  	$(function() {
  		var model = null;
  		if (typeof(piecework) !== 'undefined' && piecework != null && piecework.context != null) {
  			model = piecework.context;
  		}
  		var formView = new FormView({el: $('#main-form'), model:model});	  
  	  	formView.render();
  	  	
	  	var taskToolbarView = new TaskToolbarView({el: $('#task-toolbar'), model:model});	  
	  	taskToolbarView.render();
  	});
	
});