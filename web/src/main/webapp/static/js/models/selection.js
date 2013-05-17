define([ 'chaplin', 'models/base/model', 'models/process'], function(Chaplin, Model, Process) {
	'use strict';

	var Selection = Model.extend({
		getInteractions: function() {
			return this.get("interactions");
		},
		getProcess: function() {
			var process = this.get("process");
			if (process !== undefined)
				return process;
			
			var collection = this.get("collection");
			var processDefinitionKey = this.get("processDefinitionKey");
			if (collection !== undefined && processDefinitionKey !== undefined) 
				process = collection.findWhere({processDefinitionKey: processDefinitionKey})
			
	   		if (process === undefined) 
	   			process = new Process();
			
	   		this.set("process", process);
	   		
			return process;
		},
		
	});
	return Selection;
});