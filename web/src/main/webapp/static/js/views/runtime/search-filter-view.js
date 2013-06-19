define([ 'chaplin', 'views/base/view', 'text!templates/runtime/search-filter.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var SearchFilterView = View.extend({
		autoRender : true,
		className: 'dropdown',
		tagName: 'div',
	    template: template,
	    events: {
//	        'click .checkbox-menu-item': '_onClickCheckbox',
            'change :checkbox': '_onClickCheckbox'
	    },
	    initialize: function(options) {
            View.__super__.initialize.apply(this, options);

            var selector = this.model.get('selector');
            var options = this.model.get('options');
            var results = this.model.get('results');
            var key = this.model.get("key");

            if (key != undefined) {
                var definitions = results.get(key);
                options = new Array();
                for (var i=0;i<definitions.length;i++) {
                    options.push({ id: 'processDefinitionKey_' + definitions[i].processDefinitionKey, key: 'process', label: definitions[i].processDefinitionLabel, value: definitions[i].processDefinitionKey})
                }
                options.push({label: 'All processes', key: 'process'});
                this.model.set('options', options);
            }

            var isSelected = false;
            if (selector != undefined && options != undefined) {
                var selection = results.get(selector);
                for (var i=0;i<options.length;i++) {
                    var key = options[i].key;
                    var value = options[i].value;

                    if (selection[key] == value) {
                        options[i].selected = true;
                        this.model.set('selected', options[i].label);
                        isSelected = true;
                        break;
                    }
                }
            }

            if (!isSelected) {
                for (var i=0;i<options.length;i++) {
                    var key = options[i].key;
                    var value = options[i].value;

                    if (options[i].default !== undefined && options[i].default) {
                        options[i].selected = true;
                        this.model.set('selected', options[i].label);
                        isSelected = true;
                        break;
                    }
                }
            }

            return this;
        },
	    _onClickCheckbox: function(event) {
            var $target = $(event.target);
            var $dropdown = $target.closest('.dropdown');
            var $toggle = $dropdown.find('.dropdown-toggle-text');
            var $checkbox = $target.next('.hidden-checkbox');
            var isChecked = $checkbox.is(':checked');
            $toggle.text($target.closest('label').text());
            var id = $target.attr('id');
            $dropdown.find(':checkbox').each(function(i, element) {
                if (element.id != id)
                    element.checked = false;
            });
            Chaplin.mediator.publish('search');
	    }

	});

	return SearchFilterView;
});