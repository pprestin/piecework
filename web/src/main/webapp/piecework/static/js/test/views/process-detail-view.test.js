define([ 'models/process', 'views/process-detail-view', 'mocha', 'chai', 'sinon'], 
	function(Process, ProcessDetailView, mocha, chai, sinon) {

	var should = chai.should();
//	var mock = sinon.mock(Backbone);
	
	describe("Process detail view", function() {
		beforeEach(function(){
			this.process = new Process({processDefinitionKey: 'test'});
			this.processDetailView = new ProcessDetailView({model: this.process});
		});

		it("saves model when input changes", function() {		
			
			
//			mock.expects('gapiRequest').once().withArgs(sinon.match.object, 'update');
//
//			this.processDetailView.$el.find(':input[name="processLabel"]').val("Testing").trigger("change");
//			
//			mock.verify();
		})
	});
});