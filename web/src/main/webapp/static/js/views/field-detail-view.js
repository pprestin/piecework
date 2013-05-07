define([ 'views/base/view', 'text!templates/field-detail.hbs' ], function(View, template) {
	'use strict';

	var FieldDetailView = View.extend({
		autoRender: true,
		className: 'field-layout selectable',
		tagName: 'div',
	    template: template,
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    _onAddedToDOM: function() {
	    	var fieldId = this.model.cid;
	    	var ordinal = this.model.ordinal;
	    	if (ordinal == undefined)
	    		ordinal = "-1";
	    	
	    	this.$el.attr('id', fieldId);
	    	this.$el.attr('tabindex', ordinal);
	    	this.$el.addClass(this.model.attributes.type + '-lo');
		}
	});

	return FieldDetailView;
});