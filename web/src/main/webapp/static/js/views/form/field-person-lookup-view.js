define([ 'views/form/base-field-view', 'text!templates/form/field-person-lookup.hbs'],
		function(View, template) {
	'use strict';

	var FieldTextboxView = View.extend({
		template: template,
		events: {
		    'change input[type="text"]': '_onTypeaheadChanged',
		    'click button.add-input-button': '_onAddInputButtonClick',
		    'typeahead:autocompleted': '_onTypeaheadAutocompleted',
		    'typeahead:closed': '_onTypeaheadClosed',
		    'typeahead:opened': '_onTypeaheadOpened',
            'typeahead:selected': '_onTypeaheadSelected',
		},
		render: function(options) {
            View.__super__.render.apply(this, options);

            var editable = this.model.get('editable');
            var root = this.model.get('root');
            // strip off 'form' path param from root url
            var indexOf = root.indexOf('/form');
            if (indexOf != -1) {
                root = root.substring(0, indexOf)
            }
            var url = root + '/person.json?displayNameLike=%QUERY';

            if (editable) {
                var $textbox = this.$el.find('input[type="text"]');
                $textbox.attr('data-process-user-lookup', 'true');
                $textbox.typeahead({
                    name: 'person-lookup',
                    remote: {
                        url: url,
                        filter: function(parsedResponse) {
                            var list = parsedResponse.list;
                            var data = new Array();
                            if (list != null) {
                                for (var i=0;i<list.length;i++) {
                                    var person = list[i];
                                    data.push({value: person.userId, displayName: person.displayName, tokens: [ person.displayName ]});
                                }
                            }
                            return data;
                        }
                    },
                    valueKey: 'displayName'
                });
            }

            return this;
        },
		_onAddInputButtonClick: function(event) {
            var $controlGroup = this.$(event.target).closest('.form-group');
            var $input = $controlGroup.find(':input[type="text"]:last');
            if ($input.length == 0)
                $input = $controlGroup.find(':input[type="email"]:last');

            var $clone = $input.clone();

            $controlGroup.find('span.generated').remove();
            $controlGroup.removeClass('has-error');
            $clone.val();
            $controlGroup.append("<br/>");
            $controlGroup.append($clone);
		},
        _onTypeaheadAutocompleted: function(obj, datum) {
            if (datum != null) {
                this.$el.find('input[type="text"]').addClass('pw-person-found');
                this.$el.find(':input:hidden').val(datum.value);
            }
        },
        _onTypeaheadClosed: function(obj, datum) {
            this._onTypeaheadAutocompleted(obj, datum);
        },
        _onTypeaheadChanged: function(event) {
            this.$el.find('input[type="text"]').removeClass('pw-person-found');
            this.$el.find(':input:hidden').val(event.target.value);
        },
        _onTypeaheadOpened: function(obj, datum) {

        },
        _onTypeaheadSelected: function(obj, datum) {
            this._onTypeaheadAutocompleted(obj, datum);
        }
	});

	return FieldTextboxView;
});