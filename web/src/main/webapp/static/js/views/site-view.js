//define(['views/base/view', 'text!templates/site.hbs'], function(View, template) {
//  'use strict';
//
//  var SiteView = View.extend({
//    container: 'body',
//    id: 'site-container',
//    regions: {
//      '#main-container': 'main'
//    },
//    template: template
//  });
//
//  return SiteView;
//});

define([ 'views/base/view', 'text!templates/process-list.hbs' ], function(View,
		template) {
	'use strict';

	var ProcessListView = View.extend({
		container: '.process-list',
	    template: template
	});

	return ProcessListView;
});
