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
    .directive('wfKeypressEvents', ['$compile', '$document', '$rootScope',
        function($compile, $document, $rootScope) {
              return {
                  restrict: 'A',
                  link: function() {
                      window.document.compiler = $compile;
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
              var displayName = matchItem.displayName != null ? matchItem.displayName : matchItem;
              return query ? displayName.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : displayName;
          };

    })
    .value('hostUri', '{{HOST_URI}}')
    .value('formResourceUri', '{{FORM_RESOURCE_URI}}')
    .value('formPageUri', '{{FORM_PAGE_URI}}');