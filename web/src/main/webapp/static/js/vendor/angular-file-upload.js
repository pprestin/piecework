/**!
 * AngularJS file upload/drop directive with http post and progress
 * @author  Danial  <danial.farid@gmail.com>
 * @version 1.1.0
 */
(function() {
	
var angularFileUpload = angular.module('angularFileUpload', []);

angularFileUpload.service('$upload', ['$http', function($http) {
	this.upload = function(config) {
		config.method = config.method || 'POST';
		config.headers = config.headers || {};
		config.headers['Content-Type'] = undefined;
		config.transformRequest =  angular.identity;
		var formData = new FormData();
		for (key in config.data) {
			formData.append(key, config.data[key]);
		}
		formData.append('file', config.file);
		config.headers['__uploadProgress_'] = function(e) {
			if (e) config.progress(e);
		};
		
		var response = $http.post(config.url, formData, config);
		return response;
	};
}]);

angularFileUpload.directive('ngFileSelect', [ '$parse', '$http', function($parse, $http) {
	return function(scope, elem, attr) {
		var fn = $parse(attr['ngFileSelect']);
		elem.bind('change', function(evt) {
			var files = [], fileList, i;
			fileList = evt.target.files;
			if (fileList != null) {
				for (i = 0; i < fileList.length; i++) {
					files.push(fileList.item(i));
				}
			}
			scope.$apply(function() {
				fn(scope, {
					$files : files,
					$event : evt
				});
			});
		});
	};
} ]);

angularFileUpload.directive('ngFileDropAvailable', [ '$parse', '$http', function($parse, $http) {
	return function(scope, elem, attr) {
		if ('draggable' in document.createElement('span')) {
			var fn = $parse(attr['ngFileDropAvailable']);
			if(!scope.$$phase) {
				scope.$apply(function() {
					fn(scope);
				});
			} else {
				fn(scope)
			}
		}
	};
} ]);

angularFileUpload.directive('ngFileDrop', [ '$parse', '$http', function($parse, $http) {
	return function(scope, elem, attr) {
		if ('draggable' in document.createElement('span')) {
			var fn = $parse(attr['ngFileDrop']);
			elem[0].addEventListener("dragover", function(evt) {
				evt.stopPropagation();
				evt.preventDefault();
				elem.addClass("dragover");
			}, false);
			elem[0].addEventListener("dragleave", function(evt) {
				elem.removeClass("dragover");
			}, false);
			elem[0].addEventListener("drop", function(evt) {
				evt.stopPropagation();
				evt.preventDefault();
				elem.removeClass("dragover");
				var files = [], fileList = evt.dataTransfer.files, i;
				if (fileList != null) {
					for (i = 0; i < fileList.length; i++) {
						files.push(fileList.item(i));
					}
				}
				scope.$apply(function() {
					fn(scope, {
						$files : files,
						$event : evt
					});
				});
			}, false);
		}
	};
} ]);

})();