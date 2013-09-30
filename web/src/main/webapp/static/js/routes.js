define(['controllers/form-controller', 'controllers/designer-controller'], function() {
  'use strict';

  return function(match) {
//    match(':servlet/:access/designer', 'designer#index');
    match(':servlet/:access/form', 'form#search');
    match(':servlet/:access/form.html', 'form#search');
    match(':servlet/:access/form/', 'form#search');
    match(':servlet/:access/form/:processDefinitionKey', 'form#index');
    match(':servlet/:access/form/:processDefinitionKey/:requestId', 'form#index');
    match(':servlet/:access/form/:processDefinitionKey/:requestId/step/:ordinal', 'form#step');
    match(':servlet/:access/form/:processDefinitionKey/submission/:requestId', 'form#step');
    match(':servlet/:access/form/:processDefinitionKey/status/:status/keyword/:keyword', 'form#search');

    match(':servlet/:access/instance', 'form#search');
    match(':servlet/:access/instance/:processDefinitionKey/:processInstanceId', 'form#index');
  };
});