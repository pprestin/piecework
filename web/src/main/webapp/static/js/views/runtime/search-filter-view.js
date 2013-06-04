define([ 'chaplin', 'views/base/view', 'text!templates/runtime/search-filter.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var SearchFilterView = View.extend({
		autoRender : true,
		className: 'dropdown',
		tagName: 'div',
	    template: template,
	    events: {
	        'click .checkbox-menu-item': '_onClickCheckbox',
	    },
	    constructor: function(args) {
            View.__super__.constructor.apply(this, options);

            var selector = args.model.get('selector');
            var options = args.model.get('options');
            var results = args.model.get('results');

            if (selector != undefined && options != undefined) {
                var selection = results.get(selector);
                for (var i=0;i<options.length;i++) {
                    var key = options[i].key;
                    var value = options[i].value;

                    if (selection[key] == value) {
                        args.model.selected = options[i].label;
                        break;
                    }
                }
            }
            return args.model;
        },
	    _onClickCheckbox: function(event) {
            var $target = $(event.target);
            var $toggle = $target.closest('.dropdown').find('.dropdown-toggle-text');

            $toggle.text($target.text());
	    }

	});

	return SearchFilterView;
});