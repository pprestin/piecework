requirejs.config({
    paths: {
    	backbone: 'vendor/backbone',
    	bootstrap: '../lib/bootstrap/js/bootstrap',
    	chaplin: 'vendor/chaplin',
    	css: 'vendor/css',
    	handlebars: 'vendor/handlebars',
    	jquery: 'vendor/jquery',
    	jqueryui: 'vendor/jquery-ui-1.10.3.custom.min',
    	jquerymask: 'vendor/jquery-mask.min',
    	less: 'vendor/less',
    	normalize: 'vendor/normalize',
    	text: 'vendor/require-text-2.0.3',
        underscore: 'vendor/underscore'
    },
    shim: {
    	'backbone':{deps: ['underscore','jquery'], exports: 'Backbone'},
        'bootstrap':{deps: ['jquery']},
        'handlebars':{exports: 'Handlebars'},
        'jquerymask':{deps: ['jquery']},
        'underscore':{exports: '_'}
    }
});

require(['application', 'routes', 'bootstrap', 'jquerymask'], function(Piecework, routes, Bootstrap) {
    (new Piecework).initialize();
});