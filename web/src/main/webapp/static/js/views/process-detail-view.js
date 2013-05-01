define([ 'views/base/view', 'text!templates/process-detail.hbs', 'models/process' ], function(View, template, Process) {
	'use strict';

	var ProcessDetailView = View.extend({
		autoRender : true,
		container: '#main-frame',
	    template: template,
	   	events: {
	   		'click .btn-primary': 'save'
	   	},
	    save: function() {
	    	
			var attributes = {
				shortName : this.$('.process-short-name').val(),
				formalName : this.$('.process-formal-name').val(),
				summary : this.$('.process-summary').val(),
				participants : this.$('.process-participants').val()
			};
	    	
	    	// The model is actually a collection, owned by the ProcessListView
	    	this.model.add(new Process(attributes));
	    	
	    	// Add the new process to the list on the left
//	    	this.trigger('saved', process);
	    }

	});

	return ProcessDetailView;
});
