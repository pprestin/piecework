//define([
//	'models/process'
//], function (Process) {
//	var ProcessItemView = Backbone.View.extend({
//		tagName: "li",
//		
//		initialize: function() {
//			this.listenTo(this.model, 'change', this.render);
//		    this.listenTo(this.model, 'destroy', this.remove);
//		},
//		
//		clear: function() {
//			this.model.destroy();
//		},
//		
//		close: function() {
//			var value = this.input.val();
//			if (!value) {
//				this.clear();
//			} else {
//		        this.model.save({title: value});
//		        this.$el.removeClass("editing");
//			}
//		},
//		    
//		edit: function() {
//			this.$el.addClass("editing");
//			this.input.focus();
//		},
//		
//		render: function() {
//			this.$el.html(this.model.label);
//			this.input = this.$('.edit');
//			return this;
//	    },
//	    
//	    updateOnEnter: function(e) {
//	    	if (e.keyCode == 13) this.close();
//	    },
//	      
//	});
//	return ProcessItemView;
//});