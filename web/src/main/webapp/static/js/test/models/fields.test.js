define([ 'models/field', 'models/fields', 'models/process', 'mocha', 'chai' ], function(Field, Fields, Process, mocha, chai) {

	var should = chai.should();
	describe("Field collection", function() {
		it("sorts by ordinal", function() {
			var first = new Field({'name': 'first', ordinal: 1});
			var second = new Field({'name': 'second', ordinal: 2});
			var third = new Field({'name': 'third', ordinal: 3});
		
			var collection = new Fields();
			
			collection.add(second);
			collection.add(third);
			collection.add(first);
			
			var checkFirst = collection.shift();
			should.exist(checkFirst);
			checkFirst.should.be.an('object');
			checkFirst.get("name").should.equal("first");
			
			var checkSecond = collection.shift();
			should.exist(checkSecond);
			checkSecond.should.be.an('object');
			checkSecond.get("name").should.equal("second");
			
			var checkThird = collection.shift();
			should.exist(checkThird);
			checkThird.should.be.an('object');
			checkThird.get("name").should.equal("third");
		})
	});
});