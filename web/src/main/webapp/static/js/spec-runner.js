// Configure RequireJS
require.config({
	baseUrl: 'js/vendor',
	paths: {
		backbone: 'backbone-amd',
		models: '../models',
		lib: '../lib',
		jqueryui: 'jquery-ui-1.10.3.custom.min',
		templates: '../templates',
		testem: '/testem',
		text: 'require-text-2.0.3',
		underscore: 'underscore-amd',
		views: '../views',
	},
    shim: {
        'chai':{deps: ['mocha']},
        'backbone':{deps: ['underscore','jquery'], exports: 'Backbone'},
        'bootstrap':{deps: ['jquery']},
        'handlebars':{exports: 'Handlebars'},
        'sinon':{exports: 'sinon'},
        'sinon-chai':{},
        'underscore':{exports: '_'}
    }
});

// Require libraries
require([ 'require', 'chai', 'testem', 'mocha', 'sinon' ], function(require, chai) {

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
	         '../test/views/process-detail-view.test',
	         '../test/views/process-list-view.test'
	        ], function(process) {
		// Start runner
		mocha.run();
	});

});