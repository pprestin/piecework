({
 	baseUrl: 'js',
    paths: {
        backbone: 'vendor/backbone',
        bootstrap: 'vendor/bootstrap.min',
        //bootstrap: '../lib/bootstrap/js/bootstrap',
        chaplin: 'vendor/chaplin',
        css: 'vendor/css',
        handlebars: 'vendor/handlebars',
        jquery: 'vendor/jquery-1.10.2.min',
        jqueryui: 'vendor/jquery-ui-1.10.3.custom.min',
        jquerymask: 'vendor/jquery-mask.min',
        less: 'vendor/less',
        moment: 'vendor/moment.min',
        normalize: 'vendor/normalize',
        text: 'vendor/require-text-2.0.3',
        typeahead: 'vendor/typeahead',
        underscore: 'vendor/underscore'
    },
    shim: {
        'backbone':{deps: ['underscore','jquery'], exports: 'Backbone'},
        'bootstrap':{deps: ['jquery']},
        'handlebars':{exports: 'Handlebars'},
        'jquerymask':{deps: ['jquery']},
        'typeahead':{deps: ['jquery']},
        'underscore':{exports: '_'}
    },
    name: "main",
    out: "js/main-built.js"
})
