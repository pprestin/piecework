angular.module('Explanation',
    [

    ])
    .config(['$routeProvider', '$locationProvider', '$logProvider','$provide',
        function($routeProvider, $locationProvider, $logProvider, $provide) {
            var context = window.piecework.context;
            var root = context['static'];

            $routeProvider
                .when('/', {controller: 'ExplanationController', templateUrl: root + '/static/ng/views/explanation.html'})
                .otherwise({redirectTo:'/'});

            $locationProvider.html5Mode(true).hashPrefix('!');

            $provide.decorator('$sniffer', ['$delegate', function($delegate) {
                $delegate.history = true;
                return $delegate;
            }]);
        }
    ])
    .controller('ExplanationController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams', 'attachmentService', 'personService', 'taskService', 'wizardService', 'dialogs',
        function($scope, $window, $location, $resource, $http, $routeParams, attachmentService, personService, taskService, wizardService, dialogs) {
            console.log('started', 'Explanation controller started');
            $scope.explanation = $window.piecework.explanation.messageDetail;
            alert($scope.explanation);
        }
    ])
