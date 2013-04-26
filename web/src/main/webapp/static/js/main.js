requirejs.config({
    //By default load any module IDs from js/vendor
    baseUrl: 'static/js/vendor',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
        app: '../app'
    }
});

// Start the main app logic.
requirejs(['jquery', 'underscore', 'backbone', 'handlebars'],
function   ($, _, backbone, handlebars) {
    //jQuery, canvas and the app/sub module are all
    //loaded and can be used here now.
});