define([ 'chaplin', 'views/base/view', 'text!templates/runtime/limit-dropdown.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var LimitDropdownView = View.extend({
		autoRender : true,
		className: 'dropdown',
		container: '.limit-dropdown-container',
		tagName: 'div',
	    template: template,
	    events: {
//	        'click .checkbox-menu-item': '_onClickCheckbox',
	    },
	    _onClickCheckbox: function(event) {
//	        event.preventDefault();
            event.stopPropagation();
	        var $target = $(event.target);
	        var $checkbox = $target.find(':input[type="checkbox"]');
	        var checked = $checkbox.prop('checked');
	        checked = (checked !== undefined && checked)
	        var selector = '.checkbox-group';
            if ($checkbox.hasClass('checkbox-group1'))
                selector += '1[name!="';
            else if ($checkbox.hasClass('checkbox-group2'))
                selector += '2[name!="';
            selector += $checkbox.attr('name');
            selector += '"]';
            $(selector).removeProp('checked');
//            if (checked)
//                $target.removeProp('checked');
//            else
//                $target.prop('checked', true);
	    }
	});

	return LimitDropdownView;
});