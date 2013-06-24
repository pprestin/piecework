define(['controllers/form-controller'], function() {
  'use strict';

  return function(match) {
    match(':servlet/:access/form', 'form#search');
    match(':servlet/:access/form/:processDefinitionKey/:requestType/:requestId', 'form#index');
    match(':servlet/:access/form/:processDefinitionKey/:requestType/:requestId/step/:ordinal', 'form#step');
  };
});