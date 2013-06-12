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
        this.compose('formView', FormView, {model: formModel});

        var screenModel = formModel.get("screen");
        var screenType = screenModel.type;
        var sections = screenModel.sections;
        var currentScreen;

        if  (screenType == 'wizard') {
            currentScreen = params.ordinal;
            if (currentScreen == undefined)
                currentScreen = 1;
        }

        for (var i=0;i<sections.length;i++) {
            var section = sections[i];
            var sectionId = section.sectionId;
            var tagId = section.tagId;
            var fields = section.fields;
            var sectionViewId = tagId != null ? tagId : sectionId;
            var contentSelector = '#' + sectionViewId + " > .section-content";
            var buttonSelector = '#' + sectionViewId + " > .section-buttons";

            if (section.ordinal != undefined && currentScreen != undefined && currentScreen != section.ordinal)
                continue;

            this.compose(sectionViewId, SectionView, {id: sectionViewId, container: 'ul.sections', model: new Model(section)});
            this.compose('fieldsView_' + sectionId, FieldsView, {collection: new Collection(fields), container: contentSelector});

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
    },
  });

  return FormController;
});