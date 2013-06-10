define([ 'models/process', 'models/processes', 'views/process-list-view', 'mocha', 'chai' ], 
	function(Process, Processes, ProcessListView, mocha, chai) {

	var should = chai.should();
	describe("Process list view", function() {
		beforeEach(function(){
			this.processes = new Processes();
			this.processListView = new ProcessListView({collection: this.processes, test: true});
		});

		it("displays process that is added", function() {			
			this.processes.add(new Process({processDefinitionKey: 'test'}));
			this.processListView.collection.length.should.equal(1);
			this.processListView.subviews.length.should.equal(1);
		}),
		it("hides process that is removed", function() {			
			var process = new Process({processDefinitionKey: 'test'});
			this.processes.add(process);
			this.processes.remove(process);
			this.processListView.collection.length.should.equal(0);
			this.processListView.subviews.length.should.equal(0);
		})
	});
});