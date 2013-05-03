define([ 'views/base/view', 'text!templates/process-detail.hbs', 'models/process' ], function(View, template, Process) {
	'use strict';

	var ProcessDetailView = View.extend({
		autoRender : true,
		container: '#main-frame',
	    template: template,
	   	events: {
	   		'change input': 'fieldValueChanged',
	   		'click .edit-button': 'toggleEditing',
	   		'hide .accordion-group': 'toggleSection',
	   		'show .accordion-group': 'toggleSection',
	   	},
	   		   	
	   	fieldValueChanged: function(event) {
	    	var attributeName = event.target.name;
	    	var attributes = {};
	    	var $controlGroup = this.$(event.target).closest('.control-group');
	    	var isNotEmpty = event.target.value != null && event.target.value != '';
	    	
	    	if (attributeName === undefined) 
	    		return;
				    	
	    	attributes[attributeName] = event.target.value;
	    	this.model.set(attributes);	
	    	$controlGroup.toggleClass('hasData', isNotEmpty);
	    },
	    	    
	    toggleEditing: function(event) {
	    	var $editButton = this.$(event.currentTarget);
	    	var $accordionGroup = $editButton.closest('.accordion-group');
	    	$editButton.html(($accordionGroup.hasClass('editing') ? 'Done' : 'Edit'));
	    	
	    	$accordionGroup.toggleClass('editing');
	    }, 
	    
	    toggleSection: function(event) {
	    	var $accordionGroup = this.$(event.currentTarget);
	    	$accordionGroup.find('.section-open-indicator').toggleClass('icon-chevron-right icon-chevron-down');
	    	$accordionGroup.find('.edit-button').toggle();
	    },
	   	
	});

	return ProcessDetailView;
});
