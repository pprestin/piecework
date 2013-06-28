define([
        'chaplin',
        'models/buttons',
        'views/form/button-view',
        'views/form/button-link-view',
        'views/base/collection-view',
        ],
    function(Chaplin, Buttons, ButtonView, ButtonLinkView, CollectionView) {
	'use strict';

	var ButtonsView = CollectionView.extend({
		autoRender: false,
		className: "screen-buttons",
		container: ".screen-footer",
		tagName: 'ul',
		initItemView: function(button) {
			var type = button.get("type");
			if (type == 'button-link')
				return new ButtonLinkView({model: button});
	    	else
	    		return new ButtonView({model: button});
	    },
	});

	return ButtonsView;
});