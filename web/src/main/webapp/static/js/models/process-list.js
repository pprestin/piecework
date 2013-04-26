define([
	'jquery',
	'underscore',
	'backbone',
	'models/process'
], function ($, _, Backbone, ProcessModel) {
	var ProcessModelList = Backbone.Collection.extend({
		model: ProcessModel,
		comparator: 'created'
	});
	return ProcessModelList;
});