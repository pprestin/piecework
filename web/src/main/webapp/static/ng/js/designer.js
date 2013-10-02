'use strict';

angular.module('ProcessDesigner', ['ngResource'])
//    .factory('Process', function($resource) {
//        return $resource('../process/:processDefinitionKey', {}, {
//            query: {method:'GET', params:{processDefinitionKey:''}, isArray:true},
//            post: {method:'POST'},
//            update: {method:'PUT'},
//            remove: {method:'DELETE'}
//        });
//    })
    .controller('ListController', ['$scope','$http',
        function($scope, $http) {
            $http.get('process.json').success(function(data) {
                $scope.processes = data.list;
            });
        }
    ])
    .controller('DetailController', [
        function($scope) {

        }
    ])
    .config(function($routeProvider) {
       $routeProvider
         .when('/', {controller: 'ListController', templateUrl:'../static/ng/views/process-list.html'})
         .when('/edit/:processDefinitionKey', {controller: 'DetailController', templateUrl:'../static/ng/views/process-detail.html'})
         .otherwise({redirectTo:'/'});
    });


//    value('fbURL', '../process.json').
//    factory('Processes', function(angularFireCollection, fbURL) {
//        return angularFireCollection(fbURL);
//    }).
//  config(function($routeProvider) {
//    $routeProvider.
//      when('/', {controller:ListCtrl, templateUrl:'list.html'}).
//      when('/edit/:projectId', {controller:EditCtrl, templateUrl:'detail.html'}).
//      when('/new', {controller:CreateCtrl, templateUrl:'detail.html'}).
//      otherwise({redirectTo:'/'});
//  });
 
//function ListCtrl($scope, Projects) {
//  $scope.projects = Projects;
//}
//
//function CreateCtrl($scope, $location, $timeout, Projects) {
//  $scope.save = function() {
//    Projects.add($scope.project, function() {
//      $timeout(function() { $location.path('/'); });
//    });
//  }
//}
//
//function EditCtrl($scope, $location, $routeParams, angularFire, fbURL) {
//  angularFire(fbURL + $routeParams.projectId, $scope, 'remote', {}).
//  then(function() {
//    $scope.project = angular.copy($scope.remote);
//    $scope.project.$id = $routeParams.projectId;
//    $scope.isClean = function() {
//      return angular.equals($scope.remote, $scope.project);
//    }
//    $scope.destroy = function() {
//      $scope.remote = null;
//      $location.path('/');
//    };
//    $scope.save = function() {
//      $scope.remote = angular.copy($scope.project);
//      $location.path('/');
//    };
//  });
//}