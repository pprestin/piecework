//define([
//	'views/process-item-view',
//	'views/process-detail-view',
//	'models/process-model-list'
//], function (ProcessItem, ProcessDialog, ProcessModelList) {
//	
//define([
//	        'views/base/view',
//	        'text!templates/hello-world.hbs'
//	      ], function(View, template) {
//	        'use strict';
//	
//	var ProcessItemList = Backbone.View.extend({
//				
//		add: function(process) {
//			var view = new ProcessItem({model: process});
//			this.processes.add(process);
//			this.$el.append(view.render().el);
//		},
//		
//		initialize: function() {
//			this.processes = new ProcessModelList();
//			this.dialog = new ProcessDialog({el: $('#process-description-dialog')});
//			this.listenTo(this.dialog, 'accepted', this.add)
//		}
//		
//	});
//	
//	return ProcessItemList;
//	
//});

//define([ 'views/base/view', 'text!templates/process-list.hbs' ], function(View, template) {
//	'use strict';
//
//	var ProcessListView = View.extend({
//		// Automatically render after initialize
//		autoRender : true,
//
//		className : 'process-list',
//
//		// Automatically append to the DOM on render
//		region : 'main',
//
//		// Save the template string in a prototype property.
//		// This is overwritten with the compiled template function.
//		// In the end you might want to used precompiled templates.
//		template : template
//	});
//
//	return ProcessListView;
//});


define([ 'views/base/view', 'text!templates/process-list.hbs' ], function(View,
		template) {
	'use strict';

	var ProcessListView = View.extend({
		autoRender: true,
	    className: 'process-list',
	    template: template
	});

	return ProcessListView;
});



