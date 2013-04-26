define([ 'models/process', 'mocha', 'chai' ], function(ProcessModel, mocha, chai) {

	var ProcessModel = require('models/process');

	var should = chai.should();
	describe("ProcessModel", function() {
		it("have correct default attributes", function() {
			var model = new ProcessModel();
			should.exist(model);
			model.should.be.an('object');

			var label = model.get('label');
			label.should.be.a("string");
			label.should.equal("");
		})
	});
});