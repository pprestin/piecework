define([ 'chaplin', 'views/base/view', 'text!templates/history.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var HistoryView = View.extend({
		autoRender : true,
		container: '#history-dialog > .modal-body',
	    template: template,
	});

	return HistoryView;
});