define([ 'chaplin', 'models/buttons', 'models/base/collection', 'models/base/model', 'models/design/sections',
        'views/form/buttons-view', 'views/base/collection-view', 'views/form/fields-view',
        'views/form/grouping-view', 'views/form/section-view', 'views/form/sections-view',
        'views/base/view', 'text!templates/form/form.hbs' ],
		function(Chaplin, Buttons, Collection, Model, Sections, ButtonsView, CollectionView, FieldsView,
		         GroupingView, SectionView, SectionsView, View, template) {
	'use strict';

	var FormView = View.extend({
		autoRender : false,
		container: '.main-content',
		id: 'main-form',
		tagName: 'form',
	    template: template,
	    events: {
	        'submit': '_onFormSubmit',
	        'load': '_onLoaded',
	    },
	    listen: {
             'addedToDOM': '_onAddedToDOM',
             'groupingIndex:change mediator': '_onGroupingIndexChange',
	    },
	    initialize: function(model, options) {
	        View.__super__.initialize.apply(this, options);
            this.params = options.params;

            var screen = this.model.get("screen");
            if (screen != undefined) {
                var action = this.model.get("action");

                var groupings = screen.groupings;

                for (var i=0;i<groupings.length;i++) {
                    groupings[i].breadcrumbLink = action + '/step/' + groupings[i].ordinal;
                }

            }
	        return this;
	    },
        render: function() {
            View.__super__.render.apply(this);

            var action = this.model.get("action");

            if (action != undefined) {
                this.$el.attr('action', action + '.html');
                this.$el.attr('method', 'POST');
                this.$el.attr('enctype', 'multipart/form-data');
            }

            this.$el.attr('novalidate', 'novalidate');

            var screen = this.model.get("screen");

            if (screen == undefined)
                return this;

            var pageLink = this.model.get("link");
            var groupings = screen.groupings;
            var groupingIndex = this.model.get("groupingIndex");
            var grouping = groupings != undefined && groupings.length > groupingIndex ? groupings[groupingIndex] : { sectionIds : []};

            var sectionList = screen.sections;
            if (sectionList != undefined && sectionList.length > 0) {
                var sectionMap = {};
                if (grouping != undefined) {
                    for (var i=0;i<grouping.sectionIds.length;i++) {
                        var sectionId = grouping.sectionIds[i];
                        if (sectionId == undefined)
                            continue;
                        sectionMap[sectionId] = true;
                    }
                }
                for (var i=0;i<sectionList.length;i++) {
                    var section = sectionList[i];
                    var sectionId = section.sectionId;
                    var isSelected = sectionMap[sectionId];
                    section.selected = isSelected != undefined ? isSelected : false;
                }
            }

            var sectionsView = this.subview('sectionsView');
            if (!sectionsView) {
                var sections = new Sections(sectionList);
                sectionsView = new SectionsView({autoRender: false, collection: sections});
                this.subview('sectionsView', sectionsView);
            }

            var groupingView = this.subview('groupingView');
            if (!groupingView && grouping != null) {
                var groupingId = grouping.groupingId;
                groupingView = new GroupingView({autoRender: false, model: new Model(grouping)});
                this.subview('groupingView', groupingView);
            }

            sectionsView.render();
            var $sectionsViewContainer = this.$el.find(sectionsView.container);
            $sectionsViewContainer.addClass(sectionsView.className);
            $sectionsViewContainer.append(sectionsView.$el);

            return this;
        },
	    _doValidate: function() {
	        var data = new FormData();

            $('.generated').remove();
            $('.control-group').removeClass('error');
            $('.control-group').removeClass('warning');

            $('.section:visible').find(':input').each(function(index, element) {
                var name = element.name;
                if (name == undefined || name == null || name == '')
                    return;

                if (element.files !== undefined && element.files != null) {
                    $.each(element.files, function(fileIndex, file) {
                        if (file != null)
                            data.append(name, file);
                    });
                } else {
                    var $element = $(element);
                    var value = $(element).val();

                    if (($element.is(':radio') || $element.is(':checkbox'))) {
                        if ($element.is(":checked")) {
                            if (value != undefined)
                                data.append(name, value);
                        }
                    } else {
                        data.append(name, value);
                    }
                }
            });

            var screen = this.model.get("screen");
            var groupings = screen.groupings;
            var groupingIndex = this.model.get("groupingIndex");
            var grouping = groupings[groupingIndex];
            var groupingId = grouping.groupingId;
            var url = this.model.get("action") + '/' + groupingId + '.json';

            $.ajax({
                url : url,
                data : data,
                processData : false,
                contentType : false,
                type : 'POST',
                statusCode : {
                    204 : this._onFormValid,
                    400 : this._onFormInvalid,
                    default : this._onFailure,
                }
            });
	    },
	    _onAddedToDOM: function(event) {
            var formValues = this.model.get('formData');
            if (formValues != undefined && formValues.length > 0) {
                for (var i=0;i<formValues.length;i++) {
                    var formValue = formValues[i];
                    var name = formValue.name;
                    if (name != undefined) {
                        var selector = ':input[name="' + name + '"]';
                        var $element = $(selector);
                        var values = formValue.values;
                        $element.val(values);

//                        $element.val(function(index, value) {
//                            var values = formValue.values;
//                            if (values == undefined) {
//                                values = list();
//                                values[0] = formValue.value;
//                            }
//                            var retval = values.length > index ? values[index] : '';
//                            return retval;
//                        });
                    }
                }
            }
	    },
	    _onLoaded: function(event) {
            Chaplin.mediator.publish('formAddedToDOM');
//            var sectionsView = this.subview('sections-view');
//            if (sectionsView === undefined || sectionsView.collection.length == 0) {
//                var screen = this.model.get("screen");
//                var collection = new Collection({collection: screen.sections})
//                this.subview('sections-view', new SectionsView({collection: collection}));
//            }
	    },
	    _onFormSubmit: function(event) {
	        var screen = this.model.get("screen");
            var type = screen.type;

            if (type == 'wizard') {
                var validated = $('#main-form').prop("validated");

                if (validated != undefined && validated)
                    return true;

                this._doValidate();

                return false;
            }

	        return true;
	    },
	    _onFormValid: function(data, textStatus, jqXHR) {
            var next = $(':button[type="submit"]:visible').val();

            if (next == 'submit') {
                $('#main-form').prop("validated", true);
                $('#main-form').submit();
            } else {
                var breadcrumbSelector = 'a[href="' + next + '"]';
                var $li = $('ul.breadcrumb').find(breadcrumbSelector).closest('li'); //.prev('li');
                $li.find('span.inactive-text').remove();
                $li.find('a').removeClass('hide');

                Chaplin.mediator.publish('!router:route', next);
            }
	    },
	    _onFormInvalid: function(jqXHR, textStatus, errorThrown) {
            var errors = $.parseJSON(jqXHR.responseText);

            if (errors.items != null) {
                for (var i=0;i<errors.items.length;i++) {
                    var item = errors.items[i];
                    var selector = ':input[name="' + item.propertyName + '"]';
                    var $input = $(selector);
                    var $element = $input;

                    $input.closest('.control-group').addClass(item.type);

                    if ($input.is(':checkbox') || $input.is(':radio')) {
                        $element = $input.closest('.control-group').find('label');
                    }
                    $element.after('<span class="help-inline generated">' + item.message + '</span>')

//                    if ($input.is(':checkbox') || $input.is(':radio')) {
//                        $input.closest('.control-group').after('<div class="generated alert alert-' + item.type + '">' + item.message + '</div>');
//                    } else {
//                        $input.after('<div class="generated alert alert-' + item.type + '">' + item.message + '</div>');
//                    }
                    //$input.closest('.control-group').addClass(item.type);
                }
            }
	    },
	    _onGroupingIndexChange: function(groupingIndex) {
	        var screen = this.model.get("screen");

            if (screen == undefined)
                return this;

            var pageLink = this.model.get("link");
            var groupings = screen.groupings;
            var grouping = groupings != undefined && groupings.length > groupingIndex ? groupings[groupingIndex] : { sectionIds : []};
            if (grouping !== undefined) {
                $(".section").addClass('hide');
                for (var i=0;i<grouping.sectionIds.length;i++) {
                    var selector = '.section#' + grouping.sectionIds[i];
                    this.$(selector).removeClass('hide');
                }
            }

            this.removeSubview('buttonsView');
            var buttonsView;
            if (grouping.buttons != undefined && grouping.buttons.length > 0) {
              var buttonList = grouping.buttons;
              for (var b=0;b<buttonList.length;b++) {
                  var button = buttonList[b];
                  var buttonId = button.buttonId;

                  if (button.value != undefined) {
                      if (button.value == 'next') {
                          button.value = groupings.length > grouping.ordinal ? groupings[groupingIndex + 1].breadcrumbLink : '';
                          button.link = button.value;
                          button.alt = "Next";
                      } else if (buttonList[b].value == 'prev') {
                          if (groupingIndex > 0)
                            button.value = groupings[groupingIndex - 1].breadcrumbLink;
                          else {
                            var rootLink = pageLink;
                            var indexOfLastSlash = rootLink.lastIndexOf('/');
                            if (indexOfLastSlash != -1 && indexOfLastSlash < rootLink.length)
                                rootLink = rootLink.substring(0, indexOfLastSlash);
                            button.value = rootLink;
                          }
                          button.link = button.value;
                          button.alt = "Previous";
                      }
                  }

                  var buttons = new Buttons(buttonList);
                  buttonsView = new ButtonsView({autoRender: false, collection: buttons});
                  this.subview('buttonsView', buttonsView);
              }
            }
            if (buttonsView !== undefined) {
                buttonsView.render();
                var $buttonsViewContainer = this.$el.find(buttonsView.container);
                $buttonsViewContainer.addClass(buttonsView.className);
                $buttonsViewContainer.append(buttonsView.$el);
            }
	    },
	    _onFailure: function(jqXHR, textStatus, errorThrown) {
            alert('Failure!');
	    }
	});

	return FormView;
});