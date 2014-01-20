'use strict';

describe('Unit testing wf-active', function() {
    var $compile;
    var $rootScope;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should not disable input elements when attribute value is start and there is no task definition key', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-active=\"start\" name=\"myElement\"/>")(scope);
        scope.$digest();
        var form = { task: null };
        scope.$broadcast('event:form-loaded', form);
        expect(element.attr('disabled')).toBeFalsy();
    });

    it('Should disable input elements when attribute value is start and there is a task definition key', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-active=\"start\" name=\"myElement\"/>")(scope);
        scope.$digest();
        var form = { task: { taskDefinitionKey: 'reviewIt' } };
        scope.$broadcast('event:form-loaded', form);
        expect(element.attr('disabled')).toBeTruthy();
    });

    it('Should not disable input elements when attribute value matches task definition key', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-active=\"reviewIt\" name=\"myElement\"/>")(scope);
        scope.$digest();
        var form = { task: { taskDefinitionKey: 'reviewIt' } };
        scope.$broadcast('event:form-loaded', form);
        expect(element.attr('disabled')).toBeFalsy();
    });

    it('Should disable input elements when attribute value does not match task definition key', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-active=\"start\" name=\"myElement\"/>")(scope);
        scope.$digest();
        var form = { task: { taskDefinitionKey: 'reviewIt' } };
        scope.$broadcast('event:form-loaded', form);
        expect(element.attr('disabled')).toBeTruthy();
    });

});

describe('Unit testing wf-attachments', function() {
    var $compile;
    var $rootScope;
    var $element;
    var $attachmentService;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      $element = $compile("<div wf-attachments></div>")($rootScope);
      $attachmentService = _attachmentService_;
      $rootScope.$digest();
    }));

    it('Should call deleteAttachment on the attachmentService', function() {
        var scope = $rootScope;
        var attachment = {"attachmentId":"52d6351630042ac6f74be285","name":"comment","description":"Test XYZ","contentType":"text/plain","user":{"userId":"testuser","visibleId":"testuser","displayName":"Jerome P. User","emailAddress":"","phoneNumber":null,"uri":null},"ordinal":0,"lastModified":"2014-01-15T07:13:22.977+0000","link":"http://localhost/workflow/ui/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285","uri":"http://localhost/workflow/api/v1/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285"};
        var attachments = [ attachment ];
        scope.$broadcast('event:attachments', attachments);
        scope.$digest();
        spyOn($attachmentService, 'deleteAttachment');
        // Click edit attachments button
        $element.find('button')[0].click();
        // Click delete attachment button
        $element.find('button')[1].click();
        expect($attachmentService.deleteAttachment).toHaveBeenCalled();
    });

    it('Should set form on local scope form-loaded event is broadcast', function() {
        var scope = $element.scope;
        var form = { test: 'ok' };
        $rootScope.$broadcast('event:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfAttachments' ]});
    });

    it('Should display "no attachments" if no attachments exist', function() {
        expect($element.html()).toContain('No attachments');
    });

    it('Should display attachments if attachments exist', function() {
        var scope = $rootScope;
        var attachments = [{"attachmentId":"52d6351630042ac6f74be285","name":"comment","description":"Test XYZ","contentType":"text/plain","user":{"userId":"testuser","visibleId":"testuser","displayName":"Jerome P. User","emailAddress":"","phoneNumber":null,"uri":null},"ordinal":0,"lastModified":"2014-01-15T07:13:22.977+0000","link":"http://localhost/workflow/ui/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285","uri":"http://localhost/workflow/api/v1/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285"}];
        scope.ok = {};
        scope.$broadcast('event:attachments', attachments);
        $rootScope.$digest();
        var html = $element.html();
        expect(html).toContain('Test XYZ');
        expect(html).toContain('Jerome P. User');
        // Only will work if tested in locale PST
        expect(html).toContain('Jan 14, 2014 23:13');
    });

    it('Should broadcast toggle-attachments event if view-attachments event is broadcast', function() {
        var scope = $rootScope;
        spyOn(scope, '$broadcast').and.callThrough();
        scope.$broadcast('event:view-attachments');
        expect(scope.$broadcast).toHaveBeenCalledWith('event:view-attachments');
        expect(scope.$broadcast).toHaveBeenCalledWith('event:toggle-attachments', true);
    });
});
