//define([
//	'jquery',
//	'underscore',
//	'backbone',
//	'models/process-model'
//], function ($, _, Backbone, ProcessModel) {
//	var ProcessDetailView = Backbone.View.extend({
//
//		events: {
//			'click .btn-primary': 'save',
//			'shown': 'shown'
//		},
//		
//		save: function() {
//			this.model.label = this.labelInput.val();
//			this.model.summary = this.summaryInput.val();
//			this.created = new Date();
//			
//			// Add the new process to the list on the left
//			this.trigger('accepted', this.model);
//			
//			// Hide the dialog
//			this.$el.modal('hide');
//		},
//		
//		render: function() {
//			this.model = new ProcessModel();
//			this.labelInput = this.$('.new-process-label');
//			this.summaryInput = this.$('.new-process-summary');
//			
//			this.labelInput.val('');
//			this.summaryInput.val('');
//			
//			return this;
//		},
//		
//		shown: function() {
//			this.render();
//		}
//		
//	});
//	return ProcessDetailView;
//});

define([ 'views/base/view', 'text!templates/process-detail.hbs' ], function(View, template) {
	'use strict';

	var ProcessDetailView = View.extend({
		// Automatically render after initialize
		autoRender : true,

		className : 'process-list',

		// Automatically append to the DOM on render
		region : 'main',

		// Save the template string in a prototype property.
		// This is overwritten with the compiled template function.
		// In the end you might want to used precompiled templates.
		template : template
	});

	return ProcessListView;
});
