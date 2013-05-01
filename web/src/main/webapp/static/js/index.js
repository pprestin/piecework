requirejs.config({
    baseUrl: 'static/js',
    paths: {
    	backbone: 'vendor/backbone-amd',
    	bootstrap: '../lib/bootstrap/js/bootstrap',
    	chaplin: 'vendor/chaplin',
    	handlebars: 'vendor/handlebars',
    	jquery: 'vendor/jquery',
    	text: 'vendor/require-text-2.0.3',
        underscore: 'vendor/underscore-amd'
    },
    shim: {
    	'backbone':{deps: ['underscore','jquery'], exports: 'Backbone'},
        'bootstrap':{deps: ['jquery']},
        'handlebars':{exports: 'Handlebars'},
        'underscore':{exports: '_'}
    }
});

require(['designer', 'routes'], function(Designer, routes) {
    (new Designer).initialize();
});

/*requirejs([
	'views/process-item-list',
	'bootstrap'
], function (ProcessItemList) {
		
	$(function() {
		var app = new ProcessItemList({el: $('#process-list')});
		
		//app.render();
	});
});
*/