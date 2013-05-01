// Configure RequireJS
require.config({
	baseUrl: 'js',
	paths: {
		backbone: 'vendor/backbone-amd',
		chai : 'vendor/chai',
		jquery: 'vendor/jquery',
		mocha: 'vendor/mocha',
		testem: '/testem',
		underscore: 'vendor/underscore-amd'
	},
    shim: {
        'chai':{deps: ['mocha']}
    }
});

// Require libraries
require([ 'require', 'chai', 'testem' ], function(require, chai) {

	// Chai
	assert = chai.assert;
	should = chai.should();
	expect = chai.expect;

	// Mocha
	mocha.setup('bdd');

	// Require base tests before starting
	require([ '../test/models/process-model.test' ], function(process) {
		// Start runner
		mocha.run();
	});

});