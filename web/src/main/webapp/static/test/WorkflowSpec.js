'use strict';

//describe('Unit testing wfActive', function() {
//    var $compile;
//    var $rootScope;
//
//    // Load the wf module, which contains the directive
//    beforeEach(module('wf'));
//
//    // Store references to $rootScope and $compile
//    // so they are available to all tests in this describe block
//    beforeEach(inject(function(_$compile_, _$rootScope_){
//      // The injector unwraps the underscores (_) from around the parameter names when matching
//      $compile = _$compile_;
//      $rootScope = _$rootScope_;
//    }));
//
//    it('Should not disable input elements when attribute value is the same as the active step', function() {
//        var scope = $rootScope;
//        var element = $compile("<input data-wf-active=\"1\" name=\"myElement\"/>")(scope);
//        scope.$digest();
//        var form = { activeStepOrdinal: 1 };
//        scope.$broadcast('wfEvent:form-loaded', form);
//        expect(element.attr('disabled')).toBeFalsy();
//    });
//
//    it('Should disable input elements when attribute value is different from the active step', function() {
//        var scope = $rootScope;
//        var element = $compile("<input data-wf-active=\"1\" name=\"myElement\"/>")(scope);
//        scope.$digest();
//        var form = { task: { activeStepOrdinal: 2 } };
//        scope.$broadcast('wfEvent:form-loaded', form);
//        expect(element.attr('disabled')).toBeTruthy();
//    });
//
//    it('Should disable input elements when actionType is view even when the attribute value matches the active step', function() {
//        var scope = $rootScope;
//        var element = $compile("<input data-wf-active=\"1\" name=\"myElement\"/>")(scope);
//        scope.$digest();
//        var form = { task: { activeStepOrdinal: 1, actionType: 'VIEW' } };
//        scope.$broadcast('wfEvent:form-loaded', form);
//        expect(element.attr('disabled')).toBeTruthy();
//    });
//
//});

describe('Unit testing wf-alert', function() {
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

    it('Should display message text if field name matches', function() {
        var scope = $rootScope;
        var element = $compile("<span data-wf-alert=\"myElement\"></span>")(scope);
        var form = { validation: { 'myElement' : [ { text: 'TestMessage' } ] } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        var html = element.html();
        expect(html).toContain('TestMessage');
    });

    it('Should not display message text if field name does not', function() {
        var scope = $rootScope;
        var element = $compile("<span data-wf-alert=\"myElement\"></span>")(scope);
        var form = { validation: { 'anotherElement' : [ { text: 'TestMessage' } ] } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        var html = element.html();
        expect(html).toContain('');
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
        scope.$broadcast('wfEvent:attachments', attachments);
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
        $rootScope.$broadcast('wfEvent:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfAttachments' ]});
    });

    it('Should display "no attachments" if no attachments exist', function() {
        expect($element.html()).toContain('No attachments');
    });

    it('Should display attachments if attachments exist', function() {
        var scope = $rootScope;
        var attachments = [{"attachmentId":"52d6351630042ac6f74be285","name":"comment","description":"Test XYZ","contentType":"text/plain","user":{"userId":"testuser","visibleId":"testuser","displayName":"Jerome P. User","emailAddress":"","phoneNumber":null,"uri":null},"ordinal":0,"lastModified":"2014-01-15T07:13:22.977+0000","link":"http://localhost/workflow/ui/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285","uri":"http://localhost/workflow/api/v1/instance/DEMO/52d09a2d3004c98375b1fd42/attachment/52d6351630042ac6f74be285"}];
        scope.ok = {};
        scope.$broadcast('wfEvent:attachments', attachments);
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
        scope.$broadcast('wfEvent:view-attachments');
        expect(scope.$broadcast).toHaveBeenCalledWith('wfEvent:view-attachments');
        expect(scope.$broadcast).toHaveBeenCalledWith('wfEvent:toggle-attachments', true);
    });
});

describe('Unit testing wf-breadcrumbs', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      $element = $compile("<div data-wf-breadcrumbs></div>")($rootScope);
      $rootScope.$digest();
    }));

    it('Should set form on local scope form-loaded event is broadcast', function() {
        var scope = $rootScope;
        var form = { test: 'ok' };
        scope.$broadcast('wfEvent:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfBreadcrumbs' ]});
    });

    it('Should not contain any list-group-item tags if no steps exist', function() {
        expect($element.html()).not.toContain('list-group-item-text');
    });

    it('Should contain a breadcrumb if a step for that breadcrumb exists', function() {
        var scope = $rootScope;
        var form = { container: { activeStepOrdinal: 1 }, steps: [ { isStep: false, leaf: false, breadcrumb: 'My Example Breadcrumb', ordinal: 1, reviewChildIndex: 1} ] };
        $rootScope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect($element.html()).toContain('My Example Breadcrumb');
    });

});


describe('Unit testing wf-buttonbar', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should not contain any buttons if container is readonly', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok' };
        scope.form.container = { activeStepOrdinal: 1, readonly: true, buttons: [ { name: 'OK', primary: true, value: 'submit', type: 'button', label: 'Yes' }] };
        var element = $compile("<div data-wf-buttonbar form=\"form\" container=\"form.container\"></div>")($rootScope);
        $rootScope.$digest();
        expect(element.html()).not.toContain('<button');
    });

    it('Should not contain any buttons if container has no buttons', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok' };
        scope.form.container = { activeStepOrdinal: 1, readonly: false };
        var element = $compile("<div data-wf-buttonbar form=\"form\" container=\"form.container\"></div>")($rootScope);
        $rootScope.$digest();
        expect(element.html()).not.toContain('<button');
    });

    it('Should contain button if container has button', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok' };
        scope.form.container = { activeStepOrdinal: 1, readonly: false, buttons: [ { name: 'OK', primary: true, value: 'submit', type: 'button', label: 'Yes' }] };
        var element = $compile("<div data-wf-buttonbar form=\"form\" container=\"form.container\"></div>")($rootScope);
        $rootScope.$digest();
        expect(element.html()).toContain('<button');
    });

});


describe('Unit testing wfContainer', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should set form on local scope form-loaded event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('wfEvent:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfContainer', 'wfStatus', 'wfAttachments' ]});
    });

    it('Should contract main div when toggle-attachments true event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('wfEvent:toggle-attachments', true);
        scope.$digest();
        expect(element.html()).toContain('class="wf-expanded');
    });

    it('Should expand main div when toggle-attachments false event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('wfEvent:toggle-attachments', false);
        scope.$digest();
        expect(element.html()).not.toContain('class="wf-expanded');
    });

    it('Should show who is assigned in the nested wf-status when state is assigned', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'assigned', task: { assignee: { displayName : 'Joe Q Tester' } }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('This form is assigned to Joe Q Tester');
    });

});

describe('Unit testing wf-date', function() {
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

    it('Should render correctly when applied to a div', function() {
        var scope = $rootScope;
        var element = $compile('<div data-wf-date data-name="myElement"></div>')(scope);
        scope.$digest();
        var html = element.html();

        expect(html).toContain('datepicker-popup');
        expect(html).toContain('name="myElement"');
    });

});

describe('Unit testing wf-field', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display html if field.type is html', function() {
        var scope = $rootScope;
        var field = { defaultValue: '<span>Some random html</span>', maxInputs : 1, type : 'html' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('<span>Some random html</span>');
    });

    it('Should display html label if field.label is defined', function() {
        var scope = $rootScope;
        var field = { label: 'This is a test label', maxInputs : 1, type : 'html' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('This is a test label');
    });

    it('Should display checkbox if field.type is checkbox and at least one option is provided', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, options: [ { value: 'Value123' }], type : 'checkbox' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="checkbox"');
    });

    it('Should display checkbox label if field.type is checkbox and at least one option with a label is provided', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, options: [ { label : 'Checkbox Label', value: 'Value 123' }], type : 'checkbox' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('Checkbox Label');
    });

    it('Should display local datetime input if field.type is date', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'date' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="datetime-local"');
    });

    it('Should display radio input if field.type is radio and at least one option is provided', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, options: [ { value: 'Value123' }], type : 'radio' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="radio"');
    });

    it('Should display radio label if field.type is radio and at least one option with a label is provided', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, options: [ { label : 'Radio Label', value: 'Value 123' }], type : 'radio' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('Radio Label');
    });

    it('Should display file input if field.type is file', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'file' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="file"');
    });

    it('Should display link if field.type is file and values contains at least one item', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'file', values : [ { link : 'http://localhost/value' } ] };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('href="http://localhost/value');
    });

    it('Should display iframe if field.type is iframe and values contains at least one item', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'iframe', values : [ { link : 'http://localhost/value' } ] };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('iframe');
        expect(element.html()).toContain('src="http://localhost/value?inline=true');
    });

    it('Should display text input if field.type is person', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'person' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="text"');
    });

    it('Should display disabled text input if field.type is person and field is readonly', function() {
        var scope = $rootScope;
        var field = { editable : false, maxInputs : 1, readonly: true, type : 'person' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="text"');
        expect(element.html()).toContain('disabled');
    });

    it('Should display typeahead text input if field.type is person and field is not readonly', function() {
        var scope = $rootScope;
        var field = { editable : true, maxInputs : 1, readonly: false, type : 'person' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="text"');
        expect(element.html()).toContain('typeahead=');
    });

    it('Should display textarea if field.type is textarea', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'textarea' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('<textarea');
    });

    it('Should display specific type input if field.type is anything else', function() {
        var scope = $rootScope;
        var field = { maxInputs : 1, type : 'email' };
        scope.field = field;
        var element = $compile("<div data-wf-field=\"myElement\" data-field=\"field\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('type="email"');
    });

});

describe('Unit testing wf-fieldset', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should set field visible if there are no constraints', function() {
        var scope = $rootScope;
        var field = { name : 'TestField', maxInputs : 1, type : 'textarea' };
        var container = {};
        container.fields = [ ];
        container.fields.push(field);
        scope.form = { 'container' : container, 'fieldMap' : { 'TestField' : field } };
        scope.container = container;
        var element = $compile("<div data-wf-fieldset data-form=\"form\" data-container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('<textarea');
    });

    it('Should set field not visible if there is an unmet visibility constraint', function() {
        var scope = $rootScope;
        var field = { name : 'TestField', constraints: [ { name : 'DependentField', type : 'IS_ONLY_VISIBLE_WHEN', value : 'Yes' } ], maxInputs : 1, type : 'textarea' };
        var dependent = { name : 'DependentField', value : 'No' };
        var container = {};
        container.fields = [ ];
        container.fields.push(field);
        container.fields.push(dependent);
        scope.form = { 'container' : container, 'fieldMap' : { 'DependentField' : dependent } };
        scope.container = container;
        var element = $compile("<div data-wf-fieldset data-form=\"form\" data-container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).not.toContain('<textarea');
    });

});

describe('Unit testing wf-form', function() {
    var $compile;
    var $rootScope;
    var $httpBackend;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));
    beforeEach(module(function ($provide) {
        $provide.value('hostUri', '');
        $provide.value('formResourceUri', '/piecework/ui/form/123');
        $provide.value('formPageUri', '/piecework/ui/form');
    }));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _$httpBackend_){
        // The injector unwraps the underscores (_) from around the parameter names when matching
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;
        // backend definition common for all tests
        $httpBackend.when('GET', '/piecework/ui/form/123.json').respond({ data: { myElement: ['test-value-1'], myOtherElement: ['test-value-2']},  activeStepOrdinal: 1 });
    }));

    it('Should populate text input values', function() {
        var scope = $rootScope;
        var element = $compile("<form data-wf-form><input name=\"myElement\" value=\"\"/></form>")(scope);

//        $httpBackend.flush();
        scope.$digest();
        expect(element.find('input').val()).toContain('test-value-1');
    });

    it('Should populate radio input values', function() {
        var scope = $rootScope;
        var element = $compile("<form data-wf-form><input type=\"radio\" name=\"myOtherElement\" value=\"test-value-2\"/></form>")(scope);

//        $httpBackend.flush();
        scope.$digest();
        expect(element.find(':input').prop('checked')).toBeTruthy();
    });

//    it('Should populate multiple radio input values', function() {
//        var scope = $rootScope;
//        var element = $compile("<form data-wf-form><input id=\"rad-1\" type=\"radio\" name=\"myOtherElement\" value=\"test-value-2\"/><input type=\"radio\" name=\"myOtherElement\" value=\"test-value-2\"/></form>")(scope);
//
//        $httpBackend.flush();
//        scope.$digest();
//        expect(element.find(':input').prop('checked')).toBeTruthy();
//    });

});

describe('Unit testing wf-form-fallback', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should not display content on startup', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-form-fallback>Something invisible</div>")(scope);
        scope.$digest();
        expect(element.attr('style')).toContain('display: none;');
        expect(element.html()).toContain('Something invisible');
    });

    it('Should display content if fallback event is received', function() {
         var scope = $rootScope;
         var element = $compile("<div data-wf-form-fallback>Something invisible</div>")(scope);
         scope.$digest();
         scope.$broadcast('wfEvent:fallback');
         expect(element.attr('style')).not.toContain('display: none;');
         expect(element.html()).toContain('Something invisible');
    });

});

describe('Unit testing wf-form-loading', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display content on startup', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-form-loading>Something invisible</div>")(scope);
        scope.$digest();
        expect(element.attr('style')).toBeUndefined();
        expect(element.html()).toContain('Something invisible');
    });

    it('Should not display content if stop-loading event is received', function() {
         var scope = $rootScope;
         var element = $compile("<div data-wf-form-loading>Something invisible</div>")(scope);
         scope.$digest();
         scope.$broadcast('wfEvent:stop-loading');
         expect(element.attr('style')).toContain('display: none;');
         expect(element.html()).toContain('Something invisible');
    });

});

describe('Unit testing wf-input-mask', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    var hasDatePickerSupport = function() {
         var elem = document.createElement('input');
         elem.setAttribute('type','date');
         elem.value = 'foo';
         return (elem.type == 'date' && elem.value != 'foo');
    }

    it('Should set an input mask on a text input', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-mask=\"99-9999\" type=\"text\"/>")(scope);
        scope.$digest();
        element.val("112222");
        expect(element.val()).toContain('11-2222');
    });

    it('Should set an input mask on a date input if there is no date picker support', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-mask=\"9999-99-99\" type=\"date\"/>")(scope);
        scope.$digest();
        if (hasDatePickerSupport)
            element.val("2014-06-20");
        else
            element.val("20140620");

        expect(element.val()).toContain('2014-06-20');
    });

    it('Should set an input mask on a datetime input if there is no date picker support', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-mask=\"9999-99-99 99:99 a\" type=\"datetime\"/>")(scope);
        scope.$digest();
        if (hasDatePickerSupport)
            element.val("2014-06-20 12:00 A");
        else
            element.val("201406201200A");

        expect(element.val()).toContain('2014-06-20 12:00 A');
        expect(element.val()).not.toContain('2014-06-20 12:00 P');
    });

    it('Should set an input mask on a datetime-local input if there is no date picker support', function() {
        var scope = $rootScope;
        var element = $compile("<input data-wf-mask=\"9999-99-99T99:99:99\" type=\"datetime-local\"/>")(scope);
        scope.$digest();
        if (hasDatePickerSupport)
            element.val("2014-06-20T15:00:00");
        else
            element.val("20140620150000");

        expect(element.val()).toContain('2014-06-20T15:00:00');
        expect(element.val()).not.toContain('2014-06-20 15:00 P');
    });

});

describe('Unit testing wf-login', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display current user name', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-login></div>")(scope);
        scope.$digest();
        var form = { currentUser : { displayName: 'Joe Tester' }};
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Joe Tester');
    });

});

describe('Unit testing wf-namebar', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display current user name', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-namebar></div>")(scope);
        scope.$digest();
        var form = { currentUser : { displayName: 'Joe Tester' }};
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Joe Tester');
    });

});

describe('Unit testing wf-notifications', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should display notification message', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-notifications></div>")(scope);
        scope.$digest();
        var notification = { message : 'Some message', title: 'Some title' };
        scope.$broadcast('wfEvent:notification', notification);
        scope.$digest();
        expect(element.html()).toContain('Some title');
        expect(element.html()).toContain('Some message');
    });

});


describe('Unit testing wf-screen', function() {
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

    it('Should NOT hide content if attribute value is "confirmation", actionType is "COMPLETE" and form has no task', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"confirmation\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'COMPLETE' };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 1);
        expect(element.attr('style')).not.toContain("display: none");
    });

    it('Should NOT hide content if attribute value is "confirmation", actionType is "COMPLETE" and task is not active', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"confirmation\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'COMPLETE', task: { active: false, taskAction: 'COMPLETE' } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 1);
        expect(element.attr('style')).not.toContain("display: none");
    });

    it('Should hide content if attribute value is "rejection", actionType is "COMPLETE" and task is not active', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"rejection\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'COMPLETE', task: { active: false, taskAction: 'COMPLETE' } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 1);
        expect(element.attr('style')).toContain("display: none");
    });

    it('Should NOT hide content if attribute value is "rejection", actionType is "REJECT" and task is not active', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"rejection\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'REJECT', task: { active: false, taskAction: 'REJECT' } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 1);
        expect(element.attr('style')).not.toContain("display: none");
    });

    it('Should NOT hide content if attribute value is "1", step is "1", actionType is "CREATE" and task is active', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"1\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'CREATE', task: { active: true, taskAction: 'CREATE' } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 1);
        expect(element.attr('style')).not.toContain("display: none");
    });

    it('Should hide content if attribute value is "1", step is "2", actionType is "CREATE" and task is active', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-screen=\"1\">Some content</div>")(scope);
        scope.$digest();
        var form = { actionType: 'CREATE', task: { active: true, taskAction: 'CREATE' } };
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$broadcast('wfEvent:step-changed', 2);
        expect(element.attr('style')).toContain("display: none");
    });

});

describe('Unit testing wf-status', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should set form on local scope form-loaded event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-status></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('wfEvent:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfStatus' ]});
    });

    it('Should show who is assigned when state is assigned', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'assigned', task: { assignee: { displayName : 'Joe Q Tester' } }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('This form is assigned to Joe Q Tester');
    });

    it('Should show who completed it when state is completed', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'completed', task: { assignee: { displayName : 'Joe Q Tester' } }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('it was completed by Joe Q Tester');
    });

    it('Should show it was suspended when state is suspended', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'suspended', task: { assignee: { displayName : 'Joe Q Tester' } }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('it has been suspended');
    });

    it('Should show it was cancelled when state is cancelled', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'cancelled', task: { assignee: { displayName : 'Joe Q Tester' } }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('it has been cancelled');
    });

    it('Should show the form applicationStatusExplanation if one is set', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'cancelled', task: { assignee: { displayName : 'Joe Q Tester' } }, applicationStatusExplanation: 'Some special explanation'  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('Some special explanation');
    });

    it('Should not show the form applicationStatusExplanation warning box if one is not set', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'cancelled', task: { assignee: { displayName : 'Joe Q Tester' } } , applicationStatusExplanation: '' };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).not.toContain('alert alert-warning');
    });

    it('Should show the form explanation if one is set', function() {
        var scope = $rootScope;
        scope.form = { test: 'ok', state: 'cancelled', task: { assignee: { displayName : 'Joe Q Tester' } }, explanation: { message: 'Some special explanation', messageDetail: 'Some detail' }  };
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        expect(element.html()).toContain('Some special explanation');
        expect(element.html()).toContain('Some detail');
    });

});

describe('Unit testing wf-toolbar', function() {
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

    it('Should display toolbar', function() {
        var scope = $rootScope;
        var element = $compile("<div data-wf-toolbar></div>")(scope);
        scope.$digest();
        var form = { actionType: 'CREATE' };
        scope.$broadcast('wfEvent:form-loaded', form);
        expect(element.html()).toContain("navbar navbar-default");
    });

});

describe('Unit testing wfVariable', function() {
    var $compile;
    var $rootScope;
    var $element;

    // Load the wf module, which contains the directive
    beforeEach(module('wf'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, _attachmentService_){
      // The injector unwraps the underscores (_) from around the parameter names when matching
      $compile = _$compile_;
      $rootScope = _$rootScope_;
    }));

    it('Should add text of all values that exist in data map', function() {
        var scope = $rootScope;
        var form = { data: { 'myElement' : [ 'Blam!', 'Kazam!', 'Pow!'] } };
        var element = $compile("<span data-wf-variable=\"myElement\"></span>")($rootScope);
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        var html = element.html();
        expect(html).toContain('Blam');
        expect(html).toContain('Kazam!');
        expect(html).toContain('Pow!');
    });

    it('Should add html of all values that exist in data map', function() {
        var scope = $rootScope;
        var form = { data: { 'myElement' : [ '<p></p>', '<br>', '<span>Test</span>'] } };
        var element = $compile("<span data-wf-variable=\"myElement\"></span>")($rootScope);
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        var html = element.html();
        expect(html).toContain('<p></p>');
        expect(html).toContain('<br>');
        expect(html).toContain('<span>Test</span>');
    });

    it('Should add blank text if no values exist in data map', function() {
        var scope = $rootScope;
        var form = { data: {  } };
        var element = $compile("<span data-wf-variable=\"myElement\"></span>")($rootScope);
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('');
    });

    it('Should add text of all values that exist as sub fields in data map', function() {
        var scope = $rootScope;
        var form = { data: { 'myElement' : [ { displayName: 'Joe' }] } };
        var element = $compile("<span data-wf-variable=\"myElement.displayName\"></span>")($rootScope);
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Joe');
    });

    it('Should convert dates correctly', function() {
        var scope = $rootScope;
        var form = { data: { 'myDate' : ['2014-02-28T00:00:00.000-08:00'] } };
        var element = $compile('<span data-wf-variable="myDate" type="date"></span>')($rootScope);
        scope.$broadcast('wfEvent:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Feb 28, 2014');

    });

});