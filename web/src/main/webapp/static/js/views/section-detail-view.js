define([ 'chaplin', 'models/field', 'views/field-detail-view', 'views/base/view', 'text!templates/section-detail.hbs' ], 
		function(Chaplin, Field, FieldDetailView, View, template) {
	'use strict';

	var SectionDetailView = View.extend({
		autoRender : true,
		className: 'section-layout selectable',
		container: '.screen-content',
	    template: template,
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    addField: function(type) {
	    	if (this.$el.hasClass('selected')) {
		    	var sectionId = this.$el.prop('id');
	    		var field = new Field({type: type});
		    	var fieldId = 'field-' + field.cid;
		    	var container = '#' + sectionId + ' .section-content';		    	
		    	this.subview(fieldId, new FieldDetailView({id: fieldId, container: container, model: field}));
	    	}
	    },
	    _onAddedToDOM: function() {
	    	var sectionId = this.model.cid;
	    	var ordinal = this.model.ordinal;
	    	if (ordinal == undefined)
	    		ordinal = "-1";
	    	
	    	this.$el.attr('tabindex', ordinal);
		}
	});

	return SectionDetailView;
});