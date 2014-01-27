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
        scope.$broadcast('event:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfBreadcrumbs' ]});
    });

    it('Should not contain any list-group-item tags if no steps exist', function() {
        expect($element.html()).not.toContain('list-group-item-text');
    });

    it('Should contain a breadcrumb if a step for that breadcrumb exists', function() {
        var scope = $rootScope;
        var form = { container: { activeStepOrdinal: 1 }, steps: [ { isStep: false, leaf: false, breadcrumb: 'My Example Breadcrumb', ordinal: 1, reviewChildIndex: 1} ] };
        $rootScope.$broadcast('event:form-loaded', form);
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


describe('Unit testing wf-container', function() {
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
        scope.$broadcast('event:form-loaded', form);
        expect(form).toEqual({ test : 'ok', loadedBy: [ 'wfContainer', 'wfStatus', 'wfAttachments' ]});
    });

    it('Should contract main div when toggle-attachments true event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('event:toggle-attachments', true);
        scope.$digest();
        expect(element.html()).toContain('class="wf-expanded');
    });

    it('Should expand main div when toggle-attachments false event is broadcast', function() {
        var scope = $rootScope;
        scope.container = {};
        var element = $compile("<div wf-container container=\"container\"></div>")(scope);
        scope.$digest();
        var form = { test: 'ok' };
        scope.$broadcast('event:toggle-attachments', false);
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

describe('Unit testing wf-element', function() {
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
        var element = $compile("<span data-wf-element=\"myElement\"></span>")($rootScope);
        scope.$broadcast('event:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Blam!Kazam!Pow!');
    });

    it('Should add html of all values that exist in data map', function() {
        var scope = $rootScope;
        var form = { data: { 'myElement' : [ '<p></p>', '<br>', '<span>Test</span>'] } };
        var element = $compile("<span data-wf-element=\"myElement\"></span>")($rootScope);
        scope.$broadcast('event:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('<p></p><br><span>Test</span>');
    });

    it('Should add blank text if no values exist in data map', function() {
        var scope = $rootScope;
        var form = { data: {  } };
        var element = $compile("<span data-wf-element=\"myElement\"></span>")($rootScope);
        scope.$broadcast('event:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('');
    });

    it('Should add text of all values that exist as sub fields in data map', function() {
        var scope = $rootScope;
        var form = { data: { 'myElement' : [ { displayName: 'Joe' }] } };
        var element = $compile("<span data-wf-element=\"myElement.displayName\"></span>")($rootScope);
        scope.$broadcast('event:form-loaded', form);
        scope.$digest();
        expect(element.html()).toContain('Joe');
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
        scope.$broadcast('event:form-loaded', form);
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