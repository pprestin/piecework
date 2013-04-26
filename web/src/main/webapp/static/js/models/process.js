define([
	'jquery',
	'underscore',
	'backbone'
], function ($, _, Backbone) {
	var ProcessModel = Backbone.Model.extend({
		defaults : function() {
			return {
				label : "",
				summary : "",
				created: ""
			};
		},
	});
	return ProcessModel;
});