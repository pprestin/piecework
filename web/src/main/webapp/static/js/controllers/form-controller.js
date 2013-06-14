define([
  'controllers/base/controller',
  'models/base/model',
  'models/base/collection',
  'models/runtime/form',
  'models/runtime/page',
  'views/form/button-view',
  'views/form/button-link-view',
  'views/form/fields-view',
  'views/form/form-view',
  'views/runtime/head-view',
  'views/form/section-view',
  'views/form/sections-view',
  'views/base/view',
  'text!templates/form/button.hbs',
  'text!templates/form/button-link.hbs'
], function(Controller, Model, Collection, Form, Page, ButtonView, ButtonLinkView, FieldsView, FormView,
            HeadView, SectionView, SectionsView, View) {
  'use strict';

  var FormController = Controller.extend({
    index: function(params) {
        this.compose('formModel', Form, window.piecework.context.resource);
        this.compose('pageModel', Page, window.piecework.context);

        var formModel = this.compose('formModel');
        var pageModel = this.compose('pageModel');
        //        this.compose('headView', HeadView, {model: pageModel});

        var screenModel = formModel.get("screen");
        var screenType = screenModel.type;
        var sections = screenModel.sections;
        var currentScreen;
        var reviewSection = null;

        if (screenType == 'wizard') {
            currentScreen = params.ordinal;
            if (currentScreen == undefined)
                currentScreen = 1;

            // Check to see if there is a review section
            for (var i=0;i<sections.length;i++) {
                var section = sections[i];
                var sectionId = section.sectionId;
                var sectionType = section.type;

                if (section.ordinal == undefined || currentScreen == undefined || section.type == null)
                    continue;

                if (currentScreen == section.ordinal && sectionType != null && sectionType == 'review')
                    reviewSection = section;
            }
        }

        this.compose('formView', {
            compose: function(options) {
                this.model = formModel;
                this.view = FormView;
                var autoRender, disabledAutoRender;
                this.item = new FormView({model: this.model});
                autoRender = this.item.autoRender;
                disabledAutoRender = autoRender === void 0 || !autoRender;
                if (disabledAutoRender && typeof this.item.render === "function") {
                    return this.item.render();
                }
            },
            check: function(options) {
                return true;
            },
        });

        for (var i=0;i<sections.length;i++) {
            var section = sections[i];
            var sectionId = section.sectionId;
            var sectionType = section.type;
            var tagId = section.tagId;
            var fields = section.fields;
            var sectionViewId = tagId != null ? tagId : sectionId;
            var contentSelector = '#' + sectionViewId + " > .section-content";
            var buttonSelector = '#' + sectionViewId + " > .section-buttons";
            var showButtons = true;
            var className = 'section';

            var doHide = currentScreen != undefined && currentScreen != section.ordinal;
            var isNotReview = true;

            if (doHide)
                className += ' hide';
            else
                className += ' selected';

//            if (reviewSection != null) {
//                if (reviewSection.ordinal == section.ordinal) {
//                    isNotReview = false;
//                } else {
//                    showButtons = false;
//                }
//                doHide = false;
//            }

            this.compose(sectionViewId, {
                compose: function(options) {
                    this.model = new Model(section);
                    this.view = SectionView;
                    var autoRender, disabledAutoRender;
                    this.item = new SectionView({id: sectionViewId, className: className, container: 'ul.sections', model: this.model});
                    autoRender = this.item.autoRender;
                    disabledAutoRender = autoRender === void 0 || !autoRender;
                    if (disabledAutoRender && typeof this.item.render === "function") {
                        return this.item.render();
                    }
                },
                check: function(options) {
                    if (options.show) {
                        this.item.$el.addClass('selected');
                        this.item.$el.removeClass('hide');
                    } else {
                        this.item.$el.removeClass('selected');
                        this.item.$el.addClass('hide');
                    }
                    return true;
                },
                options: {
                    show: !doHide
                }
            });

            this.compose('fieldsView_' + sectionId, {
                options: { collection: new Collection(fields), container: contentSelector },
                compose: function(options) {
                   this.collection = new Collection(fields);
                   var autoRender, disabledAutoRender;
                   this.item = new FieldsView({collection: this.collection, container: contentSelector });
                   autoRender = this.item.autoRender;
                   disabledAutoRender = autoRender === void 0 || !autoRender;
                   if (disabledAutoRender && typeof this.item.render === "function") {
                       return this.item.render();
                   }
                },
                check: function(options) {
                    return true;
                }
            });

            if (showButtons) {
                if (section.buttons != undefined && section.buttons.length > 0) {
                    var buttons = section.buttons;
                    for (var b=0;b<buttons.length;b++) {
                        var button = buttons[b];
                        var buttonId = button.buttonId;

                        if (button.value != undefined) {
                            if (button.value == 'next') {
                                button.value = 'step/' + (section.ordinal + 1);
                                button.link = '#' + button.value;
                            } else if (buttons[b].value == 'prev') {
                                button.value = 'step/' + (section.ordinal - 1);
                                button.link = '#' + button.value;
                            }
                        }

                        if (button.type == 'button-link')
                            this.compose('buttons_' + buttonId, ButtonLinkView, {model: new Model(button), container: buttonSelector});
                        else
                            this.compose('buttons_' + buttonId, ButtonView, {model: new Model(button), container: buttonSelector});
                    }
                } else {
                    this.compose('buttons_submitButton', ButtonView, {model: new Model({label: 'Submit'}), container: buttonSelector});
                }
            }
        }
    },
  });

  return FormController;
});