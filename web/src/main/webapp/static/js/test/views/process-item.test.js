define([ 'models/process-view', 'mocha', 'chai' ], function(ProcessView, mocha, chai) {

	var should = chai.should();
	describe("ProcessView", function() {
		beforeEach(function(){
			this.process = new ProcessView({title: "Summary"});
			this.item = new todoApp.TodoListItem({model: this.todo});
		});

		it("has correct default attributes", function() {
			this.process
			
			
			
//			should.exist(model);
//			model.should.be.an('object');
//
//			var label = model.get('label');
//			label.should.be.a("string");
//			label.should.equal("");
//			
//			var summary = model.get('summary');
//			summary.should.be.a("string");
//			summary.should.equal("");
//			
//			var created = model.get('created');
//			created.should.be.a("date");
		})
	});
});