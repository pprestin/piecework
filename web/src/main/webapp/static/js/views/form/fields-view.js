define([
        'chaplin',
        'models/design/field',
        'views/base/collection-view',
        'views/form/field-checkbox-view',
        'views/form/field-date-view',
        'views/form/field-file-view',
        'views/form/field-listbox-view',
        'views/form/field-html-view',
        'views/form/field-person-lookup-view',
        'views/form/field-radio-view',
        'views/form/field-textarea-view',
        'views/form/field-textbox-view'
        ],
    function(Chaplin, Field, CollectionView, CheckboxView, DateView, FileView, ListboxView, HtmlView, PersonLookupView, RadioView, TextareaView,
				TextboxView) {
	'use strict';

	var FieldsView = CollectionView.extend({
		autoRender: false,
		className: "fields",
		container: ".section-content",
		tagName: 'fieldset',
		initItemView: function(field) {
			var fieldId = field.cid;
			var type = field.get("type");
			if (type == 'checkbox')
				return new CheckboxView({id: fieldId, model: field});
	    	else if (type == 'select-one' || type == 'select-multiple')
	    		return new ListboxView({id: fieldId, model: field});
	    	else if (type == 'radio')
	    		return new RadioView({id: fieldId, model: field});
	    	else if (type == 'textarea')
	    		return new TextareaView({id: fieldId, model: field});
	        else if (type == 'html')
	            return new HtmlView({id: fieldId, model: field});
	        else if (type == 'file')
	            return new FileView({id: fieldId, model: field});
            else if (type == 'date')
                return new DateView({id: fieldId, model: field});
            else if (type == 'person')
                return new PersonLookupView({id: fieldId, model: field});

//            var constraints = field.get('constraints');
//            if (constraints != undefined && constraints.length > 0) {
//                for (var i=0;i<constraints.length;i++) {
//                    var constraint = constraints[i];
//
//                    if (constraint != undefined && constraint.type != null) {
//                        if (constraint.type == 'IS_VALID_USER')
//                            return new PersonLookupView({id: fieldId, model: field});
//                    }
//                }
//            }

	    	return new TextboxView({id: fieldId, model: field});
	    },
	});

	return FieldsView;
});