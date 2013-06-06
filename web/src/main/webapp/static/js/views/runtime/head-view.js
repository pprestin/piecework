define([ 'chaplin', 'views/base/view', 'text!templates/form/head.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var HeadView = View.extend({
		autoRender : true,
//		el: function() {
//		    return 'head';
//		},
//		containerMethod: 'prepend',
	    template: template,
	    _ensureElement: function() {
	        var $el = $('head');
            this.setElement($el, false);
        }
	});

	return HeadView;
});