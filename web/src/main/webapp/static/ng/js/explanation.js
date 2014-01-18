angular.module('Explanation',
    [

    ])
    .directive('wfExplanation', [
         function() {
             return {
                 restrict: 'A',
                 scope: {

                 },
                 link: function (scope, element) {
                     scope.message = element.attr('data-message');
                     scope.messageDetail = element.attr('data-messagedetail');
                     element.find('[data-wf-message]').text(scope.message);
                     element.find('[data-wf-message-detail]').text(scope.messageDetail);
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
