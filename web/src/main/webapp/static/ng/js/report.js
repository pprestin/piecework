angular.module('Form',
    [
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'ui.bootstrap',
        'ui.bootstrap.alert',
        'ui.bootstrap.modal',
        'angularCharts'
    ])
    .config(['$routeProvider', '$locationProvider', '$logProvider','$provide',
        function($routeProvider, $locationProvider, $logProvider, $provide) {
            //$logProvider.debugEnabled(true);

            var context = window.piecework.context;
            var root = context['static'];

            $routeProvider
                .when('/report.html', {controller: 'ReportController', templateUrl: root + '/static/ng/views/report.html'})
                .otherwise({redirectTo:'/report.html'});

            $locationProvider.html5Mode(true).hashPrefix('!');

            $provide.decorator('$sniffer', ['$delegate', function($delegate) {
                $delegate.history = true;
                return $delegate;
            }]);
        }
    ])
    .controller('ReportController', ['$scope', '$window', '$location', '$resource', '$http', '$routeParams',
        function($scope, $window, $location, $resource, $http, $routeParams) {
            var url = '/workflow/ui/report/SupplierRegistration/processes-by-status-monthly';
            return $http.get(url).then(function(response) {
                if (response != null && response.data != null) {
                    var report = response.data;
                    var datasets = [];
                    angular.forEach(report.data.datasets, function(dataset) {
                        var datum = {
                            'x' : dataset.label,
                            'y' : dataset.data
                        };
                        datasets.push(datum);
                    });

                    $scope.data = {
                        "series" : report.data.labels,
                        "data" : datasets
                    }
                }
                $scope.chartType = 'bar';

                $scope.config = {
                    labels: false,
                    title : "Process status by start date",
                    legend : {
                        display: true,
                        position:'right'
                    }
                }
            })
        }
    ])