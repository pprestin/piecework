function ProcessesController($scope) {
    $scope.processes = [
        {processDefinitionLabel:'Demonstration', processDefinitionKey: 'Demo', selected: false},
        {processDefinitionLabel:'Another', processDefinitionKey: 'AltDemo', selected: false}];

    $scope.addProcess = function() {
        $scope.processes.push({processDefinitionLabel:$scope.processDefinitionLabel, done:false});
        $scope.processDefinitionLabel = '';
    };

//    $scope.remaining = function() {
//        var count = 0;
//        angular.forEach($scope.processes, function(todo) {
//            count += todo.done ? 0 : 1;
//        });
//        return count;
//    };

//    $scope.archive = function() {
//    var oldTodos = $scope.processes;
//    $scope.todos = [];
//    angular.forEach(oldTodos, function(todo) {
//    if (!todo.done) $scope.todos.push(todo);
//    });
//    };
}