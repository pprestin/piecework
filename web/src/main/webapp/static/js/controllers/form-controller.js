define([
  'controllers/base/controller',
  'models/base/model',
  'models/base/collection',
  'models/runtime/form',
  'models/runtime/page',
  'views/form/fields-view',
  'views/form/form-view',
  'views/runtime/head-view',
  'views/form/section-view',
  'views/form/sections-view'
], function(Controller, Model, Collection, Form, Page, FieldsView, FormView, HeadView, SectionView, SectionsView) {
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
        var sections = screenModel.sections;

        for (var i=0;i<sections.length;i++) {
           var section = sections[i];
           var sectionId = section.sectionId.replace(/-/g, '');
           var fields = section.fields;
           var sectionViewId = 'sectionView_' + sectionId;

           this.compose(sectionViewId, SectionView, {id: sectionViewId, container: 'ul.sections', model: new Model(section)});

           var selector = '#' + sectionViewId + " > .section-content";

           this.compose('fieldsView_' + sectionId, FieldsView, {collection: new Collection(fields), container: selector});
        }
    },
  });

  return FormController;
});