define([ 'chaplin', 'views/base/view', 'text!templates/search/search-filter.hbs' ],
		function(Chaplin, View, template) {
	'use strict';

	var SearchFilterView = View.extend({
		autoRender : false,
		className: 'dropdown',
		tagName: 'div',
	    template: template,
	    events: {
            'change :checkbox': '_onClickCheckbox'
	    },
	    initialize: function(options) {
            View.__super__.initialize.apply(this);

            var selector = this.model.get('selector');
            var options = this.model.get('options');
            var results = this.model.get('results');
            var key = this.model.get("key");

            if (key != undefined) {
                var definitions = results.get(key);
                options = new Array();
                for (var i=0;i<definitions.length;i++) {
                    var definition = definitions[i].task !== undefined ? definitions[i].task : definitions[i];
                    options.push({ id: 'processDefinitionKey' + i, key: 'processDefinitionKey', label: definition.processDefinitionLabel, value: definition.processDefinitionKey})
                }
                options.push({label: 'All processes', key: 'processDefinitionKey', 'default': true});
                this.model.set('options', options);
            }

            var isSelected = false;
            if (selector != undefined && options != undefined) {
                var selection = results.get(selector);
                if (selection != undefined) {
                    for (var i=0;i<options.length;i++) {
                        var key = options[i].key;
                        var value = options[i].value;

                        if (selection[key] != undefined && selection[key] == value) {
                            options[i].selected = true;
                            this.model.set('selected', options[i].label);
                            isSelected = true;
                            break;
                        }
                    }
                }
            }

            if (!isSelected && options != undefined) {
                for (var i=0;i<options.length;i++) {
                    var key = options[i].key;
                    var value = options[i].value;
                    var matches = new RegExp('[\\?&]' + key + '=([^&#]*)').exec(window.location.href);

                    if (matches != undefined && matches[1] == value) {
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

                    if (options[i]['default'] !== undefined && options[i]['default']) {
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

            $dropdown.closest('form').submit();
	    }
	});

	return SearchFilterView;
});