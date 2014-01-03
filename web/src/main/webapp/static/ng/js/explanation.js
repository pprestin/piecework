angular.module('Explanation',
    [

    ])
    .directive('wfExplanation', [
         function() {
             return {
                 restrict: 'A',
                 scope: {
                     message : '=',
                     messageDetail : '=',
                 },
                 link: function (scope, element) {
                     scope.deleteAttachment = function(attachment) {
                         attachmentService.deleteAttachment(scope.form, attachment);
                     };
                     scope.editAttachments = function() {
                         scope.state.isEditingAttachments = !scope.state.isEditingAttachments;
                     };
                 }
             }
         }
     ])
