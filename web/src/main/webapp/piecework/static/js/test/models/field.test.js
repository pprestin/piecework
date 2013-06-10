define([ 'models/field', 'models/process', 'mocha', 'chai' ], function(Field, Process, mocha, chai) {

	var should = chai.should();
	describe("Field model", function() {
		it("has correct default attributes", function() {
			var model = new Field();
			should.exist(model);
			model.should.be.an('object');

			var name = model.get('name');
			name.should.be.a("string");
			name.should.equal("");
			
			var type = model.get('type');
			type.should.be.a("string");
			type.should.equal("");
			
			var required = model.get('required');
			required.should.be.a("boolean");
			required.should.equal(false);
			
			var ordinal = model.get('ordinal');
			ordinal.should.be.a("number");
			ordinal.should.equal(1);
			
			var created = model.get('created');
			created.should.be.a("date");
		}),
		it("builds correct url", function() {
			var process = new Process({'processDefinitionKey': 'demo'});
			var model = new Field({'id': '123', 'process': process});
			var url = model.url;
			url.should.be.a("function");
			
			var location = _.result(model, 'url');
			location.should.equal("secure/v1/process/demo/field/123");
		}),
		it("correctly decides if model is new", function() {	
			var model = new Field({'id': '123'});
			var isNew = model.isNew();
			isNew.should.equal(false);
			
			model = new Field({'name': 'EmployeeName'});
			isNew = model.isNew();
			isNew.should.equal(true);
		})
	});
});