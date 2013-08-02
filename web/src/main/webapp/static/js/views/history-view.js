define([ 'chaplin', 'views/base/view', 'text!templates/history.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var HistoryView = View.extend({
		autoRender : true,
		container: '#history-dialog > .modal-body',
	    template: template,
	    initialize: function(model, options) {
	        View.__super__.initialize.apply(this, options);

            var events = this.model.get("events");
            if (events != null) {
                for (var i=0;i<events.length;i++) {
                    if (events[i].operation != undefined) {
                        if (events[i].operation.type == 'cancellation')
                            events[i].operation.icon = 'icon-stop';
                        else if (events[i].operation.type == 'suspension')
                            events[i].operation.icon = 'icon-pause';
                        else if (events[i].operation.type == 'activation')
                            events[i].operation.icon = 'icon-play';
                        else
                           events[i].operation.icon = 'icon-wrench';
                    }
                }
            }



	        return this;
	    }
	});

	return HistoryView;
});