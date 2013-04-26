define([
	'jquery',
	'underscore',
	'backbone',
	'models/process',
	'models/process-list'
], function ($, _, Backbone, ProcessModel, ProcessModelList) {
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
			this.model = new ProcessModel();
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
	return ProcessDescriptionDialog;
});