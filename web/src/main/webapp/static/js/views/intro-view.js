define([ 'chaplin', 'views/base/view', 'text!templates/intro.hbs', 'models/process', 'views/process-list-view', ], 
		function(Chaplin, View, template, Process, ProcessListView) {
	'use strict';

	var IntroView = View.extend({
		autoRender : true,
		region: 'main',
	    template: template,
	});

	return IntroView;
});