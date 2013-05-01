define([ 'models/process-model', 'mocha', 'chai' ], function(ProcessModel, mocha, chai) {

	var should = chai.should();
	describe("ProcessModel", function() {
		it("has correct default attributes", function() {
			var model = new ProcessModel();
			should.exist(model);
			model.should.be.an('object');

			var label = model.get('label');
			label.should.be.a("string");
			label.should.equal("");
			
			var summary = model.get('summary');
			summary.should.be.a("string");
			summary.should.equal("");
			
			var created = model.get('created');
			created.should.be.a("date");
		})
	});
});