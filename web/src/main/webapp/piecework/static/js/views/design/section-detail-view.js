define([ 'chaplin',
         'models/design/field',
         'views/base/collection-view',
         'views/design/field-checkbox-layout-view',
         'views/design/field-detail-view',
         'views/design/field-detail-list-view',
         'views/design/field-listbox-layout-view',
         'views/design/field-radio-layout-view',
         'views/design/field-textarea-layout-view',
         'views/design/field-textbox-layout-view',
         'views/base/view',
         'text!templates/section-detail.hbs' ],
		function(Chaplin, Field, CollectionView, CheckboxLayoutView, FieldDetailView, FieldDetailListView, ListboxLayoutView, RadioLayoutView, TextareaLayoutView, TextboxLayoutView, View, template) {
	'use strict';

	var SectionDetailView = View.extend({
		autoRender : true,
		className: 'group-layout section selectable',
		container: '.screen-content',
		tagName: 'li',
	    template: template,
	    events: {
	    	'keydown .field-layout': '_onKeyFieldLayout',
	    },
	    listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    _onAddedToDOM: function() {
	    	var ordinal = this.model.get("ordinal");
	    	var tabindex;
	    	if (ordinal == undefined)
	    		tabindex = 1000;
	    	else
	    		tabindex = ordinal * 1000;
	    	
	    	this.$el.attr('tabindex', tabindex);
	    	this.$el.attr('id', this.model.cid);
	    	this.$el.attr('data-content', this.model.get("title"));
	    	this.subview('field-list', new FieldDetailListView({container: '.section-content', collection: this.model.attributes.fields}));
		},
		_onKeyFieldLayout: function(event) {
			// If the ctrl key is down then move the selected item
			if (event.ctrlKey) {
				var $target = $('li.selectable.selected');
				var $parent = $target.closest('ul');
				
				switch (event.keyCode) {
				case 38: // Up arrow
					var $oneAbove = $target.prev('li.selectable');
					if ($oneAbove.length == 1) {
						$target.insertBefore($oneAbove);
						$target.focus();
					}
					break;
				case 40: // Down arrow
					var $oneBelow = $target.next('li.selectable');
					if ($oneBelow.length == 1) {
						$target.insertAfter($oneBelow);
						$target.focus();
					}
					break;
				}
			}
		},
	});

	return SectionDetailView;
});