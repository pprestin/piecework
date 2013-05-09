// Configure RequireJS
require.config({
	baseUrl: 'js/vendor',
	paths: {
		backbone: 'backbone-amd',
		models: '../models',
		testem: '/testem',
		underscore: 'underscore-amd'
	},
    shim: {
        'chai':{deps: ['mocha']}
    }
});

// Require libraries
require([ 'require', 'chai', 'testem', 'mocha' ], function(require, chai) {

	// Chai
	assert = chai.assert;
	should = chai.should();
	expect = chai.expect;

	// Mocha
	mocha.setup('bdd');

	// Require base tests before starting
	require([ 
			'../test/models/field.test',
			'../test/models/fields.test',
	         '../test/models/process.test',
	         '../test/models/processes.test',
	          
	        ], function(process) {
		// Start runner
		mocha.run();
	});

});