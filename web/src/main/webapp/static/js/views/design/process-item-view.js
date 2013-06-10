define([ 'views/base/view', 'text!templates/process-item.hbs' ], function(View, template) {
	'use strict';

	var ProcessDetailView = View.extend({
		tagName: 'li',
	    template: template,
	});

	return ProcessDetailView;
});