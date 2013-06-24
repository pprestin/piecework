define([ 'chaplin',
         'views/base/view',
         'text!templates/user.hbs'],
	function(Chaplin, View, template) {
	'use strict';

	var UserView = View.extend({
		autoRender : true,
		container: '#user-information-name',
	    template: template,
	});

	return UserView;
});