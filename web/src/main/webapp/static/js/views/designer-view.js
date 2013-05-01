define([ 'views/base/view', 'text!templates/designer.hbs' ], function(View, template) {
	'use strict';

	var DesignerView = View.extend({
		container : '#main-screen',
		id : 'designer-view',
		regions : {
			'#left-frame' : 'left',
			'#main-frame' : 'main'
		},
		template : template
	});

	return DesignerView;
});