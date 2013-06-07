requirejs.config({
    baseUrl: '..//static/js',
    paths: {
    	backbone: 'vendor/backbone',
    	bootstrap: '../lib/bootstrap/js/bootstrap',
    	chaplin: 'vendor/chaplin',
    	css: 'vendor/css',
    	handlebars: 'vendor/handlebars',
    	jquery: 'vendor/jquery',
    	jqueryui: 'vendor/jquery-ui-1.10.3.custom.min',
    	less: 'vendor/less',
    	normalize: 'vendor/normalize',
    	text: 'vendor/require-text-2.0.3',
        underscore: 'vendor/underscore'
    },
    shim: {
    	'backbone':{deps: ['underscore','jquery'], exports: 'Backbone'},
        'bootstrap':{deps: ['jquery']},
        'handlebars':{exports: 'Handlebars'},
        'underscore':{exports: '_'}
    }
});

require(['applications/form', 'routes/search-routes', 'bootstrap'], function(Form, routes, Bootstrap) {
    (new Form).initialize();
});