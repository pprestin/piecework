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
    .config(['$routeProvider', '$locationProvider', '$logProvider','$provide',
        function($routeProvider, $locationProvider, $logProvider, $provide) {
//            $locationProvider.html5Mode(true).hashPrefix('!');
//
//            $provide.decorator('$sniffer', ['$delegate', function($delegate) {
//                $delegate.history = true;
//                return $delegate;
//            }]);

//            $routeProvider
//                .when('/form.html', {controller: 'FormController', templateUrl: 'templates/form-list.html'})
//                .when('/form/:processDefinitionKey', {controller: 'FormController', templateUrl: 'templates/form.html'})
//                .when('/form/:processDefinitionKey/:requestId', {controller: 'FormController', templateUrl: 'templates/form.html'})
//                .when('/form/:processDefinitionKey/:state/:requestId', {controller: 'FormController', templateUrl: 'templates/form.html'})
//                .otherwise({redirectTo:'/form.html'});
        }
    ])
//    .controller('FormController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams', 'attachmentService', 'notificationService', 'personService', 'taskService', 'wizardService', 'dialogs',
//        function($scope, $window, $location, $resource, $http, $routeParams, attachmentService, notificationService, personService, taskService, wizardService, dialogs) {
//            console.log('started', 'Default form controller started');
//        }
//    ])
//    .controller('ListController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams', 'attachmentService', 'notificationService', 'personService', 'taskService', 'wizardService', 'dialogs',
//        function($scope, $window, $location, $resource, $http, $routeParams, attachmentService, notificationService, personService, taskService, wizardService, dialogs) {
//            console.log('started', 'List controller started');
//        }
//    ])
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

    });