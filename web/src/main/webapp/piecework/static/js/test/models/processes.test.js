define([ 'models/process', 'models/processes', 'mocha', 'chai' ], function(Process, Processes, mocha, chai) {

	var should = chai.should();
	describe("Process collection", function() {
		it("sorts by ordinal", function() {
			var first = new Process({'processDefinitionKey': 'first', ordinal: 1});
			var second = new Process({'processDefinitionKey': 'second', ordinal: 2});
			var third = new Process({'processDefinitionKey': 'third', ordinal: 3});
		
			var collection = new Processes();
			
			collection.add(second);
			collection.add(third);
			collection.add(first);
			
			var checkFirst = collection.shift();
			should.exist(checkFirst);
			checkFirst.should.be.an('object');
			checkFirst.get("processDefinitionKey").should.equal("first");
			
			var checkSecond = collection.shift();
			should.exist(checkSecond);
			checkSecond.should.be.an('object');
			checkSecond.get("processDefinitionKey").should.equal("second");
			
			var checkThird = collection.shift();
			should.exist(checkThird);
			checkThird.should.be.an('object');
			checkThird.get("processDefinitionKey").should.equal("third");
		}),
		it("has correct url", function() {
			var collection = new Processes();
			var url = collection.url;
			url.should.be.a("string");
			url.should.equal("secure/v1/process");
		});
	});
});