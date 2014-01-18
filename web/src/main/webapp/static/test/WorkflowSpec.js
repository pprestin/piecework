'use strict';

describe('Unit testing wf-attachments', function() {
    var $compile;
    var $rootScope;

    // Load the myApp module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display "no attachments" if no attachments exist', function() {
        // Compile a piece of HTML containing the directive
        var element = $compile("<div wf-attachments></div>")($rootScope);
        // fire all the watches
        $rootScope.$digest();
        expect(element.html()).toContain('No attachments');
    });

    it('Should display attachments if attachments exist', function() {
        var scope = $rootScope;
        // Compile a piece of HTML containing the directive
        var element = $compile("<div wf-attachments></div>")($rootScope);
        var attachments = [{"attachmentId":"52d6351630042ac6f74be285","name":"comment","description":"Test","contentType":"text/plain","user":{"userId":"testuser","visibleId":"testuser","displayName":"testuser","emailAddress":"","phoneNumber":null,"uri":null},"ordinal":0,"lastModified":"2014-01-15T07:13:22.977+0000","link":"http://localhost/workflow/ui/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285","uri":"http://localhost/workflow/api/v1/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285"}];
        spyOn(scope, "$broadcast");
        spyOn(scope, "$emit")
        scope.$broadcast('event:attachments', attachments);
        expect(scope.$broadcast).toHaveBeenCalledWith("event:attachments", attachments);
        expect(scope.$emit).toHaveBeenCalledWith("event:attachments", attachments);

        // fire all the watches
        $rootScope.$digest();
        //expect(element.html()).toContain('deleteAttachment');
    });
});
