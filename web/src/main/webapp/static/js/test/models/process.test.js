define([ 'models/process', 'mocha', 'chai' ], function(Process, mocha, chai) {

	var should = chai.should();
	describe("Process model", function() {
		it("has correct default attributes", function() {
			var model = new Process({'processDefinitionKey': 'demo'});
			should.exist(model);
			model.should.be.an('object');

			var label = model.get('processLabel');
			label.should.be.a("string");
			label.should.equal("");
			
			var summary = model.get('processSummary');
			summary.should.be.a("string");
			summary.should.equal("");
			
			var participants = model.get('participantSummary');
			participants.should.be.a("string");
			participants.should.equal("");
			
			var created = model.get('created');
			created.should.be.a("date");
		}),
		it("builds correct url", function() {
			var model = new Process({'processDefinitionKey': 'demo'});
			var url = model.url;
			url.should.be.a("function");
			
			var location = _.result(model, 'url')
			location.should.equal("secure/v1/process/demo");
		}),
		it("correctly decides if model is new", function() {	
			var model = new Process({'processDefinitionKey': 'demo'});
			var isNew = model.isNew();
			isNew.should.equal(false);
			
			model = new Process({'processLabel': 'Demonstration'});
			isNew = model.isNew();
			isNew.should.equal(true);
		})
	});
});