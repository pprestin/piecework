	$(document).ready(function() {		
	  	
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
  				_.each(this.model.sections, function (section) {
  					$content.append(new SectionView({model:section}).render().el);
  		        }, this);
  				
	  		    return this;
	  		}
	  	
	  	});
	  		  	
	  	var formView = new FormView({el: $('#main-form'), model:app.resource});	  
	  	formView.render();
	  	
	  	
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
  				
				if (this.model.sections != null && this.model.sections.length > 0) {
					
				}
				
				_.each(this.model.dialogs, function(dialog) {
					$(this.el).append(new DialogView({model:dialog}).render().el);
				}, this);
				
				$(this.el).find(':button').button();
  				
	  		    return this;
	  		}
	  		
	  	});
	  	
	  	var taskToolbarView = new TaskToolbarView({el: $('#task-toolbar'), model:app.resource});	  
	  	taskToolbarView.render();
	  	
	  	
// 	 	// Router
// 	  	var AppRouter = Backbone.Router.extend({
	  	 
// 	  	    routes:{
// 	  	        "":"list",
// 	  	        "fields/:id":"fieldDetail"
// 	  	    },
	  	 
// 	  	    list:function () {
// 	  	        this.fieldList = new FieldListCollection();
// 	  	        this.fieldListView = new WineListView({model:this.fieldList});
// 	  	        this.fieldList.fetch();
// 	  	        $('#sidebar').html(this.fieldListView.render().el);
// 	  	    },
	  	 
// 	  	    fieldDetail:function (id) {
// 	  	        this.field = this.fieldList.get(id);
// 	  	        this.fieldView = new FormFieldView({model:this.field});
// 	  	        $('#content').html(this.fieldView.render().el);
// 	  	    }
// 	  	});
	  	 
// 	  	var app = new AppRouter();
// 	  	Backbone.history.start();

	  	
		// Parse the javascript object and create models 
	  	
	  	
// 		$('#form-placeholder').html(html);
	});