define([ 'chaplin', 'models/field', 'models/screen', 'views/base/view', 'text!templates/screen-configure.hbs'], 
		function(Chaplin, Field, Screen, View, template) {
	'use strict';

	var ScreenConfigureView = View.extend({
		autoRender : true,
		container: 'body',
		listen: {
	        addedToDOM: '_onAddedToDOM'
	    },
	    //template: template,
	    _onAddedToDOM: function() {
	    	
	    	$('head').append('<link href="/static/css/screen-designer.css" rel="stylesheet"/>')
	    	
	    	var view = this;
			$(':input').each(function(i, element) {
				var $input = $(element);
				var name = element.name;
//				var fieldView = new FieldConfigureView({model: new Field({name:name})});
//				view.subview(name, fieldView);
				
				$input.popover({ 
				    html : true,
				    placement: 'bottom',
				    trigger: 'click',
				    title: function() {
						return 'Test';
//				      return $("#popover-head").html();
				    },
				    content: function() {
				    	return '<form><input type="checkbox"></form>';
//				      return $("#popover-content").html();
				    },
				    template: '<div class="piecework-popover"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
				});
				
//				$input.mousemove(function(event) {
//					var left = event.pageX - $(this).offset().left;
//			        var top = event.pageY - $(this).offset().top;
//			        var editor = view.subview(name);
//			        if (editor != undefined) {
//			        	var position = editor.$el.position();
//			        	var visible = editor.$el.is(':visible');
//			        	if (!visible || (Math.abs(top - position.top) > 150 && Math.abs(left - position.left) > 150))
//			        		editor.$el.css({top: top,left: left}).show();
//			        }
//				});
//				
////				$input.mouseover(function() {
////					$input.addClass('piecework-field-edit');
////					var editor = view.subview(name);
////					editor.$el.position();
////				});
//				
//				$input.mouseout(function() {
//					var editor = view.subview(name);
//					if (editor != undefined)
//						editor.$el.hide();
////					$input.removeClass('piecework-field-edit');
//				});
//				
			});
		},
	});

	return ScreenConfigureView;
});