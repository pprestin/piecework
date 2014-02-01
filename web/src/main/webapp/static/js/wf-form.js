angular.module('wf',
    [
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'blueimp.fileupload',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'wf.directives',
        'wf.services'
    ])
    .config(['$httpProvider', '$routeProvider', '$locationProvider', '$logProvider','$provide','$sceDelegateProvider',
        function($httpProvider, $routeProvider, $locationProvider, $logProvider, $provide, $sceDelegateProvider) {
            {{DYNAMIC_CONFIGURATION}}
        }
    ])
    .directive('wfKeypressEvents', ['$document', '$rootScope',
        function($document, $rootScope) {
              return {
                  restrict: 'A',
                  link: function() {
                      $document.bind('keyup', function(e) {
                          $rootScope.$broadcast('keyup:' + e.which, e);
                      });
                  }
              };
        }
    ])
    .filter('typeaheadHighlight', function() {

          function escapeRegexp(queryToEscape) {
            return queryToEscape.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
          }

          return function(matchItem, query) {
              var displayName = matchItem.displayName;
              return query ? displayName.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : displayName;
          };

    })
    .value('hostUri', '{{HOST_URI}}')
    .value('formResourceUri', '{{FORM_RESOURCE_URI}}')
    .value('formPageUri', '{{FORM_PAGE_URI}}');